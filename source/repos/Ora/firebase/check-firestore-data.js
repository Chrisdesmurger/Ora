#!/usr/bin/env node

/**
 * Script de diagnostic Firestore
 * Vérifie que les données ont été importées correctement
 */

const admin = require('firebase-admin');

const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || './serviceAccountKey.json';

try {
  const serviceAccount = require(serviceAccountPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  console.log('✅ Firebase Admin SDK initialized\n');
} catch (error) {
  console.error('❌ Error initializing Firebase Admin SDK:', error.message);
  process.exit(1);
}

const db = admin.firestore();

async function checkCollection(name) {
  console.log(`📦 Checking collection: ${name}`);
  try {
    const snapshot = await db.collection(name).limit(5).get();

    if (snapshot.empty) {
      console.log(`   ❌ Collection is EMPTY\n`);
      return 0;
    }

    const count = (await db.collection(name).count().get()).data().count;
    console.log(`   ✅ ${count} documents found`);

    console.log(`   📄 Sample documents:`);
    snapshot.forEach(doc => {
      const data = doc.data();
      console.log(`      - ${doc.id}: ${data.title || data.first_name || 'N/A'}`);
    });
    console.log('');

    return count;
  } catch (error) {
    console.log(`   ❌ Error: ${error.message}\n`);
    return 0;
  }
}

async function checkSecurityRules() {
  console.log('🔒 Security Rules Check');
  console.log('   Note: This script uses Admin SDK (bypasses security rules)');
  console.log('   Make sure your app\'s Firebase Auth is working!\n');
}

async function main() {
  console.log('🔍 Ora Firestore Diagnostic\n');
  console.log('='.repeat(50) + '\n');

  await checkSecurityRules();

  const programsCount = await checkCollection('programs');
  const contentCount = await checkCollection('content');
  const usersCount = await checkCollection('users');
  const statsCount = await checkCollection('stats');

  console.log('='.repeat(50));
  console.log('\n📊 Summary:');
  console.log(`   Programs: ${programsCount}`);
  console.log(`   Content: ${contentCount}`);
  console.log(`   Users: ${usersCount}`);
  console.log(`   Stats: ${statsCount}`);

  if (programsCount === 0 || contentCount === 0) {
    console.log('\n❌ PROBLEM DETECTED:');
    console.log('   Collections are empty! Run: npm run import');
  } else {
    console.log('\n✅ Firestore data looks good!');
    console.log('\nNext steps:');
    console.log('   1. Make sure you are signed in with Firebase Auth in the app');
    console.log('   2. Check Android logcat for permission errors');
    console.log('   3. Verify Firebase project ID in google-services.json');
  }

  process.exit(0);
}

main().catch(error => {
  console.error('\n❌ Error:', error.message);
  process.exit(1);
});
