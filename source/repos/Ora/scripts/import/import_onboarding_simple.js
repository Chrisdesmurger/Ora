#!/usr/bin/env node

/**
 * Simple Firebase Import Script for Onboarding Configuration
 * Uses Firebase CLI credentials (no additional setup needed)
 *
 * Usage: node import_onboarding_simple.js
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, Timestamp } = require('firebase-admin/firestore');
const fs = require('fs');
const path = require('path');

console.log('═'.repeat(60));
console.log('  ORA Onboarding Configuration Import');
console.log('═'.repeat(60));
console.log();

// Initialize Firebase Admin with Application Default Credentials
try {
  initializeApp({
    projectId: 'ora-wellbeing'
  });
  console.log('✅ Firebase initialized with project: ora-wellbeing');
} catch (error) {
  console.error('❌ Failed to initialize Firebase:', error.message);
  process.exit(1);
}

const db = getFirestore();

async function importOnboardingConfig() {
  try {
    // Read configuration file
    const configPath = path.join(__dirname, 'onboarding_personalization_config.json');

    if (!fs.existsSync(configPath)) {
      throw new Error(`Configuration file not found: ${configPath}`);
    }

    const configData = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    console.log(`📖 Loaded configuration: ${configData.title}`);
    console.log(`   Version: ${configData.version}`);
    console.log(`   Questions: ${configData.questions.length}`);

    // Add server timestamps
    configData.created_at = Timestamp.now();
    configData.updated_at = Timestamp.now();

    if (configData.status === 'active') {
      configData.published_at = Timestamp.now();
      configData.published_by = configData.created_by || 'admin';
    }

    // Check for existing active config
    const existingActiveQuery = await db.collection('onboarding_configs')
      .where('status', '==', 'active')
      .limit(1)
      .get();

    if (!existingActiveQuery.empty) {
      console.warn('\n⚠️  Warning: Active config exists, archiving...');

      const existingDoc = existingActiveQuery.docs[0];
      await existingDoc.ref.update({
        status: 'archived',
        updated_at: Timestamp.now(),
        archived_at: Timestamp.now(),
        archived_by: configData.created_by || 'admin'
      });

      console.log('✅ Existing config archived:', existingDoc.id);
    }

    // Import new configuration
    const docRef = await db.collection('onboarding_configs').add(configData);
    console.log(`\n✅ Successfully imported onboarding configuration!`);
    console.log(`   Document ID: ${docRef.id}`);
    console.log(`   Collection: onboarding_configs`);
    console.log(`   Status: ${configData.status}`);

    // Summary
    console.log('\n📊 Configuration Summary:');
    console.log(`   - Total questions: ${configData.questions.length}`);

    const byCategory = configData.questions.reduce((acc, q) => {
      acc[q.category] = (acc[q.category] || 0) + 1;
      return acc;
    }, {});

    console.log(`   - By category:`);
    Object.entries(byCategory).forEach(([cat, count]) => {
      console.log(`     • ${cat}: ${count}`);
    });

    const byType = configData.questions.reduce((acc, q) => {
      acc[q.type.kind] = (acc[q.type.kind] || 0) + 1;
      return acc;
    }, {});

    console.log(`   - By type:`);
    Object.entries(byType).forEach(([type, count]) => {
      console.log(`     • ${type}: ${count}`);
    });

    const required = configData.questions.filter(q => q.required).length;
    console.log(`   - Required: ${required}`);
    console.log(`   - Optional: ${configData.questions.length - required}`);

    console.log('\n🎉 Import completed successfully!');

    return docRef.id;
  } catch (error) {
    console.error('\n❌ Error:', error.message);
    throw error;
  }
}

// Run import
importOnboardingConfig()
  .then(() => process.exit(0))
  .catch(() => process.exit(1));
