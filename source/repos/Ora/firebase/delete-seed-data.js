#!/usr/bin/env node

/**
 * Script pour supprimer les données seed de Firestore
 * Usage: node firebase/delete-seed-data.js
 */

const admin = require('firebase-admin');
const fs = require('fs');

// Initialize Firebase Admin SDK
const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || './serviceAccountKey.json';

if (!fs.existsSync(serviceAccountPath)) {
  console.error(`❌ Service account key not found at: ${serviceAccountPath}`);
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

/**
 * Supprime tous les documents d'une collection
 */
async function deleteCollection(collectionName) {
  console.log(`\n🗑️  Deleting collection: ${collectionName}`);

  const collectionRef = db.collection(collectionName);
  const snapshot = await collectionRef.get();

  if (snapshot.empty) {
    console.log(`  ℹ️  Collection "${collectionName}" is already empty`);
    return 0;
  }

  const batchSize = 500;
  let deletedCount = 0;

  while (true) {
    const snapshot = await collectionRef.limit(batchSize).get();

    if (snapshot.empty) {
      break;
    }

    const batch = db.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
      console.log(`  ✓ Deleting: ${doc.id}`);
    });

    await batch.commit();
    deletedCount += snapshot.size;

    if (snapshot.size < batchSize) {
      break;
    }
  }

  console.log(`✅ Deleted ${deletedCount} documents from "${collectionName}"`);
  return deletedCount;
}

/**
 * Main deletion function
 */
async function main() {
  console.log('🗑️  Ora Wellbeing - Delete Firestore Seed Data');
  console.log('==============================================\n');

  try {
    // Demander confirmation
    console.log('⚠️  WARNING: This will delete ALL seed data from Firestore!');
    console.log('   Collections to delete:');
    console.log('   - content');
    console.log('   - programs');
    console.log('');

    // Supprimer les collections
    const contentDeleted = await deleteCollection('content');
    const programsDeleted = await deleteCollection('programs');

    console.log('\n✅ Deletion completed!');
    console.log(`   - ${contentDeleted} content items deleted`);
    console.log(`   - ${programsDeleted} programs deleted`);
    console.log('\n💡 You can now re-import clean data with: node import-seed-data.js');

    process.exit(0);
  } catch (error) {
    console.error('\n❌ Deletion failed:', error.message);
    console.error(error.stack);
    process.exit(1);
  }
}

// Run the deletion
main();
