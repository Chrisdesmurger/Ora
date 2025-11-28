#!/usr/bin/env node

/**
 * Firebase Import Script for Onboarding Configuration
 *
 * This script imports the onboarding_personalization_config.json
 * into Firebase Firestore.
 *
 * Prerequisites:
 * 1. Install Firebase Admin SDK: npm install firebase-admin
 * 2. Set GOOGLE_APPLICATION_CREDENTIALS environment variable or place serviceAccountKey.json in project root
 * 3. Ensure you have write access to Firestore
 *
 * Usage:
 *   node import_onboarding_to_firebase.js
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin SDK
try {
  // Try to initialize with service account key file
  const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');

  if (fs.existsSync(serviceAccountPath)) {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    console.log('✅ Firebase Admin SDK initialized with service account key');
  } else {
    // Fall back to GOOGLE_APPLICATION_CREDENTIALS environment variable
    admin.initializeApp();
    console.log('✅ Firebase Admin SDK initialized with GOOGLE_APPLICATION_CREDENTIALS');
  }
} catch (error) {
  console.error('❌ Failed to initialize Firebase Admin SDK:', error.message);
  console.error('\nPlease ensure you have either:');
  console.error('1. A serviceAccountKey.json file in the project root, OR');
  console.error('2. GOOGLE_APPLICATION_CREDENTIALS environment variable set');
  process.exit(1);
}

const db = admin.firestore();

/**
 * Import onboarding configuration to Firestore
 */
async function importOnboardingConfig() {
  try {
    // Read the JSON configuration file
    const configPath = path.join(__dirname, 'onboarding_personalization_config.json');

    if (!fs.existsSync(configPath)) {
      throw new Error(`Configuration file not found: ${configPath}`);
    }

    const configData = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    console.log(`📖 Loaded configuration: ${configData.title}`);
    console.log(`   Version: ${configData.version}`);
    console.log(`   Questions: ${configData.questions.length}`);

    // Add server timestamps
    const now = admin.firestore.FieldValue.serverTimestamp();
    configData.created_at = now;
    configData.updated_at = now;

    // If status is active, mark as published
    if (configData.status === 'active') {
      configData.published_at = now;
      configData.published_by = configData.created_by || 'admin';
    }

    // Check if an active configuration already exists
    const existingActiveQuery = await db.collection('onboarding_configs')
      .where('status', '==', 'active')
      .limit(1)
      .get();

    if (!existingActiveQuery.empty) {
      console.warn('\n⚠️  Warning: An active onboarding configuration already exists!');
      console.warn('   Existing config ID:', existingActiveQuery.docs[0].id);
      console.warn('   Archiving existing config before importing new one...\n');

      // Archive the existing active config
      await existingActiveQuery.docs[0].ref.update({
        status: 'archived',
        updated_at: now,
        archived_at: now,
        archived_by: configData.created_by || 'admin'
      });

      console.log('✅ Existing config archived');
    }

    // Import the new configuration
    const docRef = await db.collection('onboarding_configs').add(configData);
    console.log(`\n✅ Successfully imported onboarding configuration!`);
    console.log(`   Document ID: ${docRef.id}`);
    console.log(`   Collection: onboarding_configs`);
    console.log(`   Status: ${configData.status}`);

    // Display summary
    console.log('\n📊 Configuration Summary:');
    console.log(`   - Total questions: ${configData.questions.length}`);

    const questionsByCategory = configData.questions.reduce((acc, q) => {
      acc[q.category] = (acc[q.category] || 0) + 1;
      return acc;
    }, {});

    console.log(`   - Questions by category:`);
    Object.entries(questionsByCategory).forEach(([category, count]) => {
      console.log(`     • ${category}: ${count}`);
    });

    const questionsByType = configData.questions.reduce((acc, q) => {
      acc[q.type.kind] = (acc[q.type.kind] || 0) + 1;
      return acc;
    }, {});

    console.log(`   - Questions by type:`);
    Object.entries(questionsByType).forEach(([type, count]) => {
      console.log(`     • ${type}: ${count}`);
    });

    // Display required questions
    const requiredCount = configData.questions.filter(q => q.required).length;
    console.log(`   - Required questions: ${requiredCount}`);
    console.log(`   - Optional questions: ${configData.questions.length - requiredCount}`);

    console.log('\n🎉 Import completed successfully!');
    console.log('\n📱 Next steps:');
    console.log('   1. Open the Android app');
    console.log('   2. Create a new account or log in');
    console.log('   3. Complete the onboarding flow');
    console.log('   4. Check Firestore users/{uid}.onboarding for responses');

    return docRef.id;
  } catch (error) {
    console.error('\n❌ Error importing configuration:', error.message);
    throw error;
  }
}

/**
 * Verify the import by fetching the configuration
 */
async function verifyImport(docId) {
  try {
    console.log('\n🔍 Verifying import...');
    const doc = await db.collection('onboarding_configs').doc(docId).get();

    if (!doc.exists) {
      throw new Error('Document not found after import!');
    }

    const data = doc.data();
    console.log('✅ Verification passed!');
    console.log(`   - Document exists: ✓`);
    console.log(`   - Questions count: ${data.questions.length} ✓`);
    console.log(`   - Status: ${data.status} ✓`);

    return true;
  } catch (error) {
    console.error('❌ Verification failed:', error.message);
    return false;
  }
}

/**
 * Main execution
 */
async function main() {
  console.log('═'.repeat(60));
  console.log('  ORA Onboarding Configuration Import');
  console.log('═'.repeat(60));
  console.log();

  try {
    const docId = await importOnboardingConfig();
    await verifyImport(docId);

    process.exit(0);
  } catch (error) {
    console.error('\n💥 Import failed!');
    process.exit(1);
  }
}

// Run the script
main();
