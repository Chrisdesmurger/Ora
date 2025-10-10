#!/usr/bin/env node

/**
 * Script d'import des données seed dans Firestore
 * Usage: node firebase/import-seed-data.js
 *
 * Importe:
 * - Programs (collection: programs)
 * - Content (collection: content)
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin SDK
const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || './serviceAccountKey.json';

if (!fs.existsSync(serviceAccountPath)) {
  console.error(`❌ Service account key not found at: ${serviceAccountPath}`);
  console.error('');
  console.error('To fix this:');
  console.error('1. Go to Firebase Console > Project Settings > Service Accounts');
  console.error('2. Click "Generate New Private Key"');
  console.error('3. Save the file as serviceAccountKey.json in the firebase/ directory');
  console.error('4. Or set GOOGLE_APPLICATION_CREDENTIALS env variable');
  process.exit(1);
}

try {
  const serviceAccount = require(serviceAccountPath);

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });

  console.log('✅ Firebase Admin SDK initialized');
} catch (error) {
  console.error('❌ Error initializing Firebase Admin SDK:', error.message);
  process.exit(1);
}

const db = admin.firestore();

// Enable Firestore settings for better performance
db.settings({
  ignoreUndefinedProperties: true
});

/**
 * Import programs from seed-data/programs.json
 */
async function importPrograms() {
  console.log('\n📦 Importing programs...');

  const programsPath = path.join(__dirname, 'seed-data', 'programs.json');
  const programsData = JSON.parse(fs.readFileSync(programsPath, 'utf8'));

  const batch = db.batch();
  let count = 0;

  for (const program of programsData.programs) {
    const docRef = db.collection('programs').doc(program.id);

    // Add created_at and updated_at timestamps (camelCase)
    const programWithTimestamps = {
      ...program,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    // Remove old snake_case fields if they exist
    delete programWithTimestamps.created_at;
    delete programWithTimestamps.updated_at;

    batch.set(docRef, programWithTimestamps);
    count++;

    console.log(`  ✓ ${program.id} (${program.category})`);
  }

  await batch.commit();
  console.log(`✅ Imported ${count} programs`);

  return count;
}

/**
 * Import content from seed-data/content.json
 */
async function importContent() {
  console.log('\n📦 Importing content...');

  const contentPath = path.join(__dirname, 'seed-data', 'content.json');
  const contentData = JSON.parse(fs.readFileSync(contentPath, 'utf8'));

  const batch = db.batch();
  let count = 0;

  for (const item of contentData.content) {
    const docRef = db.collection('content').doc(item.id);

    // Convert Unix timestamp to Firestore Timestamp
    let publishedAt = null;
    if (item.publishedAt) {
      publishedAt = admin.firestore.Timestamp.fromMillis(item.publishedAt * 1000);
    }

    // Add created_at and updated_at timestamps
    const contentWithTimestamps = {
      ...item,
      publishedAt: publishedAt,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    // Remove old snake_case fields if they exist
    delete contentWithTimestamps.created_at;
    delete contentWithTimestamps.updated_at;
    delete contentWithTimestamps.published_at;

    batch.set(docRef, contentWithTimestamps);
    count++;

    console.log(`  ✓ ${item.id} (${item.category})`);
  }

  await batch.commit();
  console.log(`✅ Imported ${count} content items`);

  return count;
}

/**
 * Create sample user data for testing
 */
async function createSampleUserData(uid = 'rgzkId7TdvXHoyzDZD7feFFOxAR2') {
  console.log('\n📦 Creating sample user data...');
/**
  // Create user profile
  const profileRef = db.collection('users').doc(uid);
  await profileRef.set({
    uid: uid,
    first_name: 'Demo',
    last_name: 'User',
    email: 'demo@ora-wellbeing.com',
    photo_url: null,
    plan_tier: 'premium',
    motto: 'Vivre en pleine conscience',
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });
  console.log(`  ✓ Created user profile: ${uid}`);

  // Create user stats
  const statsRef = db.collection('stats').doc(uid);
  await statsRef.set({
    uid: uid,
    total_minutes: 245,
    total_sessions: 18,
    current_streak: 5,
    longest_streak: 12,
    favorite_category: 'Méditation',
    last_practice_at: admin.firestore.FieldValue.serverTimestamp(),
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });
  console.log(`  ✓ Created user stats: ${uid}`);
 */
  // Enroll user in 2 programs
  const enrollments = [
    {
      program_id: 'meditation-debutant-7j',
      current_day: 3,
      total_days: 7,
      progress_percentage: 42,
      started_at: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)),
      last_session_at: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 1 * 60 * 60 * 1000)),
      completed_sessions: ['med-respiration-consciente', 'med-scan-corporel'],
      is_completed: false
    },
    {
      program_id: 'defi-gratitude-21j',
      current_day: 5,
      total_days: 21,
      progress_percentage: 24,
      started_at: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 4 * 24 * 60 * 60 * 1000)),
      last_session_at: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 3 * 60 * 60 * 1000)),
      completed_sessions: ['gratitude-introduction', 'gratitude-petit-plaisirs', 'gratitude-relations'],
      is_completed: false
    }
  ];

  const batch = db.batch();
  for (const enrollment of enrollments) {
    const enrollRef = db.collection('user_programs').doc(uid).collection('enrolled').doc(enrollment.program_id);
    batch.set(enrollRef, {
      ...enrollment,
      uid: uid,
      created_at: enrollment.started_at,
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log(`  ✓ Enrolled in: ${enrollment.program_id} (${enrollment.progress_percentage}% complete)`);
  }
  await batch.commit();

  // Create gratitude entries
  const gratitudeEntries = [
    {
      date: new Date().toISOString().split('T')[0], // Today
      gratitudes: [
        'Ma famille et leurs encouragements',
        'Le soleil ce matin',
        'Cette application de méditation'
      ],
      mood: 'joyful',
      notes: 'Journée productive et énergisante !',
      created_at: admin.firestore.FieldValue.serverTimestamp()
    },
    {
      date: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString().split('T')[0], // Yesterday
      gratitudes: [
        'Mon travail épanouissant',
        'Une bonne nuit de sommeil',
        'Ma santé'
      ],
      mood: 'peaceful',
      notes: 'Journée calme et reposante',
      created_at: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 24 * 60 * 60 * 1000))
    },
    {
      date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 2 days ago
      gratitudes: [
        'Mes amis proches',
        'La nature autour de moi',
        'Les moments de calme'
      ],
      mood: 'grateful',
      created_at: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 2 * 24 * 60 * 60 * 1000))
    }
  ];

  const gratitudeBatch = db.batch();
  for (const entry of gratitudeEntries) {
    const entryRef = db.collection('gratitudes').doc(uid).collection('entries').doc(entry.date);
    gratitudeBatch.set(entryRef, {
      ...entry,
      uid: uid,
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log(`  ✓ Gratitude entry: ${entry.date}`);
  }
  await gratitudeBatch.commit();

  console.log(`✅ Created sample user data for: ${uid}`);
}

/**
 * Display import summary
 */
async function displaySummary() {
  console.log('\n📊 Firestore Collection Summary:');

  const collections = ['programs', 'content', 'users', 'stats', 'user_programs', 'gratitudes'];

  for (const collectionName of collections) {
    try {
      const snapshot = await db.collection(collectionName).limit(1).get();
      if (!snapshot.empty) {
        const count = (await db.collection(collectionName).count().get()).data().count;
        console.log(`  ✓ ${collectionName}: ${count} documents`);
      } else {
        console.log(`  - ${collectionName}: 0 documents`);
      }
    } catch (error) {
      console.log(`  ⚠ ${collectionName}: Error getting count`);
    }
  }
}

/**
 * Main import function
 */
async function main() {
  console.log('🚀 Ora Wellbeing - Firestore Data Import');
  console.log('==========================================\n');

  try {
    // Import programs and content
    const programCount = await importPrograms();
    const contentCount = await importContent();

    // Create sample user data
    await createSampleUserData('test-user-123');

    // Display summary
    await displaySummary();

    console.log('\n✅ Import completed successfully!');
    console.log(`   - ${programCount} programs imported`);
    console.log(`   - ${contentCount} content items imported`);
    console.log(`   - 1 sample user created (test-user-123)`);
    console.log('\n💡 You can now test the app with this data!');
    console.log('   Test user UID: test-user-123');

    process.exit(0);
  } catch (error) {
    console.error('\n❌ Import failed:', error.message);
    console.error(error.stack);
    process.exit(1);
  }
}

// Run the import
main();
