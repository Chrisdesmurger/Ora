const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'ora-wellbeing'
});

const db = admin.firestore();

/**
 * Script pour inscrire un utilisateur existant à des programmes sample
 * et simuler une utilisation active
 *
 * Usage: node firebase/enroll-user-programs.js <USER_ID>
 */

async function enrollUserInPrograms(userId) {
  try {
    console.log(`\n📋 Inscription de l'utilisateur ${userId} aux programmes...\n`);

    // 1. Vérifier que l'utilisateur existe
    const userDoc = await db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      console.error(`❌ Utilisateur ${userId} introuvable dans Firestore`);
      console.log(`Créez d'abord le profil utilisateur avec import-seed-data.js`);
      return;
    }

    const userData = userDoc.data();
    console.log(`✅ Utilisateur trouvé: ${userData.first_name || 'Sans nom'}`);
    console.log(`   Plan: ${userData.plan_tier}\n`);

    // 2. Récupérer tous les programmes disponibles
    const programsSnapshot = await db.collection('programs')
      .where('is_active', '==', true)
      .get();

    const programs = [];
    programsSnapshot.forEach(doc => {
      programs.push({ id: doc.id, ...doc.data() });
    });

    console.log(`📚 ${programs.length} programmes trouvés\n`);

    // 3. Sélectionner 3 programmes à inscrire (avec différents statuts)
    const selectedPrograms = [
      {
        programId: 'meditation-debutant-7j',
        status: 'in_progress',
        currentDay: 4,
        sessionsCompleted: 3,
        description: 'Programme en cours - jour 4/7'
      },
      {
        programId: 'defi-gratitude-21j',
        status: 'in_progress',
        currentDay: 8,
        sessionsCompleted: 7,
        description: 'Programme actif - jour 8/21'
      },
      {
        programId: 'yoga-matinal-14j',
        status: 'completed',
        currentDay: 14,
        sessionsCompleted: 14,
        description: 'Programme terminé - 100%'
      }
    ];

    // 4. Créer les inscriptions
    const batch = db.batch();
    const now = admin.firestore.Timestamp.now();
    const today = new Date();

    for (const enrollment of selectedPrograms) {
      const program = programs.find(p => p.id === enrollment.programId);

      if (!program) {
        console.log(`⚠️  Programme ${enrollment.programId} introuvable, ignoré`);
        continue;
      }

      // Calculer les dates en fonction du statut
      const startedDate = new Date(today);
      startedDate.setDate(startedDate.getDate() - enrollment.currentDay);

      const lastSessionDate = new Date(today);
      lastSessionDate.setDate(lastSessionDate.getDate() - 1); // Dernière session hier

      const enrollmentData = {
        uid: userId,
        programId: enrollment.programId,
        programTitle: program.title,
        programCategory: program.category,
        currentDay: enrollment.currentDay,
        totalDays: program.duration,
        isCompleted: enrollment.status === 'completed',
        isPremiumOnly: program.is_premium_only || false,
        startedAt: admin.firestore.Timestamp.fromDate(startedDate),
        lastSessionAt: admin.firestore.Timestamp.fromDate(lastSessionDate),
        completedAt: enrollment.status === 'completed'
          ? admin.firestore.Timestamp.fromDate(lastSessionDate)
          : null,
        createdAt: now,
        updatedAt: now
      };

      const enrollmentRef = db
        .collection('user_programs')
        .doc(userId)
        .collection('enrolled')
        .doc(enrollment.programId);

      batch.set(enrollmentRef, enrollmentData);

      console.log(`✅ ${enrollment.description}`);
      console.log(`   Programme: ${program.title}`);
      console.log(`   Progression: ${enrollment.currentDay}/${program.duration} jours`);
      console.log(`   Sessions: ${enrollment.sessionsCompleted} complétées\n`);
    }

    // 5. Mettre à jour les statistiques utilisateur
    const statsData = {
      uid: userId,
      totalMinutes: 180, // 3h de pratique
      sessions: 24, // 24 sessions au total
      streakDays: 8, // 8 jours consécutifs
      lastPracticeAt: admin.firestore.Timestamp.fromDate(new Date(today.getTime() - 24 * 60 * 60 * 1000)).toMillis(), // Hier en epoch ms
      updatedAt: now.toMillis() // epoch ms
    };

    const statsRef = db.collection('stats').doc(userId);
    batch.set(statsRef, statsData, { merge: true });

    console.log(`📊 Statistiques mises à jour:`);
    console.log(`   • ${statsData.totalMinutes} minutes de pratique`);
    console.log(`   • ${statsData.sessions} sessions complétées`);
    console.log(`   • ${statsData.streakDays} jours de streak actuel\n`);

    // 6. Ajouter des entrées de gratitude récentes
    const gratitudeEntries = [];
    for (let i = 0; i < 5; i++) {
      const entryDate = new Date(today);
      entryDate.setDate(entryDate.getDate() - i);
      const dateStr = entryDate.toISOString().split('T')[0]; // Format YYYY-MM-DD

      const gratitudeData = {
        uid: userId,
        date: dateStr,
        gratitudes: [
          i === 0 ? "Ma pratique quotidienne de méditation" : "Ma famille et mes proches",
          i === 0 ? "Les progrès que je fais chaque jour" : "Ma santé et mon bien-être",
          i === 0 ? "Ce moment de calme pour moi" : "Les belles rencontres de la journée"
        ],
        mood: ['joyful', 'peaceful', 'grateful', 'calm', 'energized'][i % 5],
        notes: i === 0 ? "Excellente session de méditation ce matin, je me sens vraiment ancré." : null,
        createdAt: admin.firestore.Timestamp.fromDate(entryDate),
        updatedAt: admin.firestore.Timestamp.fromDate(entryDate)
      };

      const gratitudeRef = db
        .collection('gratitudes')
        .doc(userId)
        .collection('entries')
        .doc(dateStr);

      batch.set(gratitudeRef, gratitudeData);
      gratitudeEntries.push(dateStr);
    }

    console.log(`📝 ${gratitudeEntries.length} entrées de gratitude ajoutées`);
    console.log(`   Dates: ${gratitudeEntries.join(', ')}\n`);

    // 7. Commit du batch
    await batch.commit();

    console.log(`\n✅ ✅ ✅ Inscription terminée avec succès! ✅ ✅ ✅`);
    console.log(`\nRésumé pour l'utilisateur ${userId}:`);
    console.log(`  • 3 programmes (2 en cours, 1 terminé)`);
    console.log(`  • 24 sessions complétées`);
    console.log(`  • 180 minutes de pratique`);
    console.log(`  • 8 jours de streak`);
    console.log(`  • 5 jours de gratitude\n`);

  } catch (error) {
    console.error('❌ Erreur lors de l\'inscription:', error);
    process.exit(1);
  }
}

// Point d'entrée
const userId = process.argv[2];

if (!userId) {
  console.error('\n❌ Usage: node firebase/enroll-user-programs.js <USER_ID>');
  console.log('\nExemple:');
  console.log('  node firebase/enroll-user-programs.js rgzkId7TdvXHoyzDZD7feFFOxAR2\n');
  process.exit(1);
}

enrollUserInPrograms(userId)
  .then(() => {
    console.log('✅ Script terminé');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Erreur fatale:', error);
    process.exit(1);
  });
