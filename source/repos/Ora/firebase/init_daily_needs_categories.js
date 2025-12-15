/**
 * Firebase Migration Script: Initialize daily_needs_categories collection
 * Issue #33: "Ton besoin du jour" section on HomeScreen
 *
 * This script creates the default daily need categories in Firestore.
 * Categories are read-only for users (admin write only via OraWebApp).
 *
 * Usage:
 *   node firebase/init_daily_needs_categories.js
 *
 * Prerequisites:
 *   - Firebase Admin SDK configured
 *   - serviceAccountKey.json present in firebase/ directory
 */

const admin = require("firebase-admin");
const path = require("path");

// Initialize Firebase Admin SDK
const serviceAccountPath = path.join(__dirname, "serviceAccountKey.json");

try {
  const serviceAccount = require(serviceAccountPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
  console.log("Firebase Admin SDK initialized successfully");
} catch (error) {
  console.error("Error initializing Firebase Admin SDK:", error.message);
  console.log("Make sure serviceAccountKey.json exists in the firebase/ directory");
  process.exit(1);
}

const db = admin.firestore();

/**
 * Default daily need categories for "Ton besoin du jour" section
 * These categories help users quickly find content matching their current needs.
 */
const DEFAULT_CATEGORIES = [
  {
    id: "anti-stress",
    name_fr: "Anti-stress",
    name_en: "Anti-stress",
    description_fr: "Calme ton esprit et reduis ton anxiete",
    description_en: "Calm your mind and reduce anxiety",
    color_hex: "#A78BFA", // Lavender
    icon_url: null,
    order: 0,
    is_active: true,
    filter_tags: ["relaxation", "breathing", "meditation", "stress-relief", "calm"],
    lesson_ids: [],
  },
  {
    id: "energie-matinale",
    name_fr: "Energie matinale",
    name_en: "Morning Energy",
    description_fr: "Demarre ta journee avec vitalite",
    description_en: "Start your day with vitality",
    color_hex: "#FCD34D", // Yellow
    icon_url: null,
    order: 1,
    is_active: true,
    filter_tags: ["yoga", "energizing", "morning", "wake-up", "matin"],
    lesson_ids: [],
  },
  {
    id: "relaxation",
    name_fr: "Relaxation",
    name_en: "Relaxation",
    description_fr: "Detends-toi et libere les tensions",
    description_en: "Relax and release tension",
    color_hex: "#86EFAC", // Green
    icon_url: null,
    order: 2,
    is_active: true,
    filter_tags: ["stretching", "pilates", "gentle", "detente", "relax"],
    lesson_ids: [],
  },
  {
    id: "pratique-du-soir",
    name_fr: "Pratique du soir",
    name_en: "Evening Practice",
    description_fr: "Prepare ton corps et ton esprit au sommeil",
    description_en: "Prepare your body and mind for sleep",
    color_hex: "#7C3AED", // Purple
    icon_url: null,
    order: 3,
    is_active: true,
    filter_tags: ["bedtime", "meditation", "sleep", "sommeil", "evening", "soir"],
    lesson_ids: [],
  },
];

/**
 * Creates or updates the daily_needs_categories collection
 */
async function initializeDailyNeedCategories() {
  console.log("\n=== Initializing daily_needs_categories collection ===\n");

  const batch = db.batch();

  for (const category of DEFAULT_CATEGORIES) {
    const docRef = db.collection("daily_needs_categories").doc(category.id);
    console.log(`  - Creating category: ${category.id} (${category.name_fr})`);

    // Add created_at and updated_at timestamps
    const dataWithTimestamps = {
      ...category,
      created_at: admin.firestore.FieldValue.serverTimestamp(),
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    };

    batch.set(docRef, dataWithTimestamps, { merge: true });
  }

  try {
    await batch.commit();
    console.log(`\nSuccessfully created ${DEFAULT_CATEGORIES.length} categories`);
  } catch (error) {
    console.error("Error creating categories:", error.message);
    throw error;
  }
}

/**
 * Verifies the created categories
 */
async function verifyCategories() {
  console.log("\n=== Verifying created categories ===\n");

  const snapshot = await db
    .collection("daily_needs_categories")
    .orderBy("order")
    .get();

  console.log(`Found ${snapshot.size} categories:\n`);

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    console.log(`  [${doc.id}]`);
    console.log(`    name_fr: ${data.name_fr}`);
    console.log(`    color: ${data.color_hex}`);
    console.log(`    order: ${data.order}`);
    console.log(`    is_active: ${data.is_active}`);
    console.log(`    filter_tags: [${data.filter_tags.join(", ")}]`);
    console.log("");
  });
}

/**
 * Updates existing lessons to add need_tags field (optional)
 * This derives need_tags from existing tags for backward compatibility
 */
async function updateLessonsWithNeedTags() {
  console.log("\n=== Updating lessons with need_tags ===\n");

  const lessonsSnapshot = await db.collection("lessons").get();

  if (lessonsSnapshot.empty) {
    console.log("No lessons found in Firestore");
    return;
  }

  console.log(`Found ${lessonsSnapshot.size} lessons to update`);

  const batch = db.batch();
  let updatedCount = 0;

  lessonsSnapshot.docs.forEach((doc) => {
    const data = doc.data();
    const existingTags = data.tags || [];

    // Skip if lesson already has need_tags
    if (data.need_tags && data.need_tags.length > 0) {
      console.log(`  - ${doc.id}: Already has need_tags, skipping`);
      return;
    }

    // Derive need_tags from regular tags
    const needTags = deriveNeedTags(existingTags);

    if (needTags.length > 0) {
      console.log(`  - ${doc.id}: Adding need_tags [${needTags.join(", ")}]`);
      batch.update(doc.ref, { need_tags: needTags });
      updatedCount++;
    } else {
      console.log(`  - ${doc.id}: No matching tags found`);
    }
  });

  if (updatedCount > 0) {
    try {
      await batch.commit();
      console.log(`\nUpdated ${updatedCount} lessons with need_tags`);
    } catch (error) {
      console.error("Error updating lessons:", error.message);
    }
  } else {
    console.log("\nNo lessons needed updating");
  }
}

/**
 * Derives need_tags from regular tags
 * @param {string[]} tags - Existing lesson tags
 * @returns {string[]} - Derived need_tags
 */
function deriveNeedTags(tags) {
  const needTags = new Set();

  tags.forEach((tag) => {
    const lowerTag = tag.toLowerCase();

    // Morning/energizing
    if (["morning", "matin", "energizing", "wake-up", "reveil"].includes(lowerTag)) {
      needTags.add("morning");
      needTags.add("energizing");
    }

    // Evening/bedtime
    if (["evening", "soir", "bedtime", "sleep", "sommeil"].includes(lowerTag)) {
      needTags.add("evening");
      needTags.add("bedtime");
      needTags.add("sleep");
    }

    // Relaxation
    if (["relaxation", "relax", "detente", "calm", "stress", "anti-stress"].includes(lowerTag)) {
      needTags.add("relaxation");
      needTags.add("stress-relief");
    }

    // Breathing
    if (["breathing", "respiration", "breathwork"].includes(lowerTag)) {
      needTags.add("breathing");
      needTags.add("relaxation");
    }

    // Stretching/gentle
    if (["stretching", "etirement", "gentle", "doux"].includes(lowerTag)) {
      needTags.add("stretching");
      needTags.add("gentle");
    }

    // Meditation
    if (["meditation"].includes(lowerTag)) {
      needTags.add("meditation");
      needTags.add("relaxation");
    }
  });

  return Array.from(needTags);
}

/**
 * Main execution
 */
async function main() {
  console.log("=========================================");
  console.log("  Daily Need Categories Migration Script");
  console.log("  Issue #33: 'Ton besoin du jour' Section");
  console.log("=========================================");

  try {
    // Step 1: Create categories
    await initializeDailyNeedCategories();

    // Step 2: Verify categories
    await verifyCategories();

    // Step 3: Update lessons with need_tags (optional)
    const args = process.argv.slice(2);
    if (args.includes("--update-lessons")) {
      await updateLessonsWithNeedTags();
    } else {
      console.log("\nTip: Run with --update-lessons to add need_tags to existing lessons");
    }

    console.log("\n=========================================");
    console.log("  Migration completed successfully!");
    console.log("=========================================\n");

    process.exit(0);
  } catch (error) {
    console.error("\nMigration failed:", error);
    process.exit(1);
  }
}

// Run the script
main();
