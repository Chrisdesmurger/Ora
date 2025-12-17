/**
 * Cloud Functions pour migrer les need_tags aux lessons
 * Analyse automatiquement le contenu et attribue les tags appropriés
 */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Définition des catégories avec leurs mots-clés
const CATEGORIES = {
  "anti-stress": {
    name: "Anti-stress",
    keywords: [
      "stress", "anxiété", "anxieux", "angoisse", "tension", "détente",
      "calme", "apaisant", "relaxant", "sérénité", "zen", "tranquillité",
      "apaisement", "nervosité", "relâcher", "relâchement", "décompresser",
    ],
    tags: ["stress_relief", "anxiety_relief", "calm"],
  },
  "energie-matinale": {
    name: "Énergie matinale",
    keywords: [
      "matin", "réveil", "énergie", "dynamique", "vitalité", "tonique",
      "énergisant", "stimulant", "activation", "éveil", "boost", "vivifiant",
      "matinale", "journée", "commencer", "démarrer",
    ],
    tags: ["morning_energy", "energizing", "wake_up"],
  },
  "relaxation": {
    name: "Relaxation",
    keywords: [
      "relaxation", "détente", "décontracter", "repos", "lâcher-prise",
      "douceur", "souplesse", "étirement", "étirer", "respiration",
      "profonde", "méditation", "pleine conscience", "mindfulness",
    ],
    tags: ["relaxation", "deep_relaxation", "unwind"],
  },
  "pratique-du-soir": {
    name: "Pratique du soir",
    keywords: [
      "soir", "soirée", "coucher", "sommeil", "dormir", "nuit",
      "avant de dormir", "fin de journée", "endormissement", "repos",
      "préparer au sommeil", "calme du soir", "transition",
    ],
    tags: ["evening_practice", "sleep_preparation", "bedtime"],
  },
};

interface CategoryMatch {
  id: string;
  name: string;
  tags: string[];
  matchCount: number;
}

interface AnalysisResult {
  tags: string[];
  categories: CategoryMatch[];
}

interface LessonAnalysisResult {
  id: string;
  title: string;
  currentTags: string[];
  suggestedTags: string[];
  categories: Array<{
    name: string;
    matchCount: number;
  }>;
}

interface MigrationResult {
  id: string;
  title: string;
  tags: string[];
  categories: string[];
}

/**
 * Analyse le contenu et retourne les tags appropriés
 */
function analyzeLessonContent(
  title: string,
  description: string,
  category: string
): AnalysisResult {
  const text = `${title || ""} ${description || ""} ${category || ""}`.toLowerCase();
  const matchedCategories: CategoryMatch[] = [];

  // Compter les matches pour chaque catégorie
  for (const [categoryId, categoryData] of Object.entries(CATEGORIES)) {
    const matchCount = categoryData.keywords.filter((keyword) =>
      text.includes(keyword.toLowerCase())
    ).length;

    if (matchCount > 0) {
      matchedCategories.push({
        id: categoryId,
        name: categoryData.name,
        tags: categoryData.tags,
        matchCount,
      });
    }
  }

  // Trier par nombre de matches
  matchedCategories.sort((a, b) => b.matchCount - a.matchCount);

  // Collecter les tags (max 2 catégories)
  const tags = new Set<string>();
  const maxCategories = 2;

  matchedCategories.slice(0, maxCategories).forEach((cat) => {
    cat.tags.forEach((tag) => tags.add(tag));
  });

  return {
    tags: Array.from(tags),
    categories: matchedCategories.slice(0, maxCategories),
  };
}

/**
 * Cloud Function pour analyser les lessons (DRY-RUN)
 */
export const analyzeLessonTags = functions
  .runWith({
    timeoutSeconds: 540,
    memory: "1GB",
  })
  .https.onRequest(async (req, res) => {
    const db = admin.firestore();

    try {
      functions.logger.info("Analyse des lessons (DRY-RUN)");

      const lessonsSnapshot = await db.collection("lessons").get();
      functions.logger.info(`${lessonsSnapshot.size} lessons trouvées`);

      const results: LessonAnalysisResult[] = [];

      lessonsSnapshot.forEach((doc) => {
        const lesson = doc.data();
        const analysis = analyzeLessonContent(
          lesson.title || "",
          lesson.description || "",
          lesson.category || ""
        );

        results.push({
          id: doc.id,
          title: lesson.title || "Sans titre",
          currentTags: lesson.need_tags || [],
          suggestedTags: analysis.tags,
          categories: analysis.categories.map((c) => ({
            name: c.name,
            matchCount: c.matchCount,
          })),
        });
      });

      // Statistiques
      const categoryStats: Record<string, number> = {};
      results.forEach((result) => {
        result.categories.forEach((cat) => {
          categoryStats[cat.name] = (categoryStats[cat.name] || 0) + 1;
        });
      });

      const withTags = results.filter((r) => r.suggestedTags.length > 0);
      const withoutTags = results.filter((r) => r.suggestedTags.length === 0);

      res.status(200).json({
        success: true,
        totalLessons: lessonsSnapshot.size,
        lessonsWithTags: withTags.length,
        lessonsWithoutTags: withoutTags.length,
        categoryStats,
        results,
      });
    } catch (error: unknown) {
      functions.logger.error("Erreur lors de l'analyse:", error);
      res.status(500).json({
        success: false,
        error: error instanceof Error ? error.message : "Unknown error",
      });
    }
  });

/**
 * Cloud Function pour migrer les tags (APPLIQUE LES MODIFICATIONS)
 */
export const migrateLessonTags = functions
  .runWith({
    timeoutSeconds: 540,
    memory: "1GB",
  })
  .https.onRequest(async (req, res) => {
    const db = admin.firestore();

    try {
      functions.logger.info("Début de la migration des need_tags");

      // 1. Récupérer toutes les lessons
      const lessonsSnapshot = await db.collection("lessons").get();
      functions.logger.info(`${lessonsSnapshot.size} lessons trouvées`);

      const updates: Array<{ docRef: FirebaseFirestore.DocumentReference; tags: string[] }> = [];
      const results: MigrationResult[] = [];

      // 2. Analyser chaque lesson
      lessonsSnapshot.forEach((doc) => {
        const lesson = doc.data();
        const analysis = analyzeLessonContent(
          lesson.title || "",
          lesson.description || "",
          lesson.category || ""
        );

        if (analysis.tags.length > 0) {
          results.push({
            id: doc.id,
            title: lesson.title || "Sans titre",
            tags: analysis.tags,
            categories: analysis.categories.map((c) => c.name),
          });

          updates.push({
            docRef: doc.ref,
            tags: analysis.tags,
          });
        }
      });

      functions.logger.info(`${updates.length} lessons à mettre à jour`);

      // 3. Appliquer les updates par batch (500 max)
      let batchCount = 0;
      let batch = db.batch();

      for (let i = 0; i < updates.length; i++) {
        const update = updates[i];
        batch.update(update.docRef, {need_tags: update.tags});
        batchCount++;

        // Commit tous les 500 documents
        if (batchCount >= 500 || i === updates.length - 1) {
          await batch.commit();
          functions.logger.info(`Batch de ${batchCount} opérations committé`);
          batch = db.batch();
          batchCount = 0;
        }
      }

      // 4. Statistiques
      const categoryStats: Record<string, number> = {};
      results.forEach((result) => {
        result.categories.forEach((cat: string) => {
          categoryStats[cat] = (categoryStats[cat] || 0) + 1;
        });
      });

      const response = {
        success: true,
        totalLessons: lessonsSnapshot.size,
        updatedLessons: updates.length,
        categoryStats,
        results: results.slice(0, 20), // Retourner les 20 premiers pour debug
      };

      functions.logger.info("Migration terminée avec succès");
      res.status(200).json(response);
    } catch (error: unknown) {
      functions.logger.error("Erreur lors de la migration:", error);
      res.status(500).json({
        success: false,
        error: error instanceof Error ? error.message : "Unknown error",
        stack: error instanceof Error ? error.stack : undefined,
      });
    }
  });
