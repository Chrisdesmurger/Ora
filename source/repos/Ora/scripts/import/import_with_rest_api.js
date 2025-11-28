#!/usr/bin/env node

/**
 * Import Onboarding Config using Firestore REST API
 * Uses Firebase CLI access token
 */

const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');
const { promisify } = require('util');
const execAsync = promisify(exec);

const PROJECT_ID = 'ora-wellbeing';
const COLLECTION = 'onboarding_configs';

console.log('═'.repeat(60));
console.log('  ORA Onboarding Configuration Import (REST API)');
console.log('═'.repeat(60));
console.log();

async function getAccessToken() {
  try {
    const { stdout } = await execAsync('firebase login:ci --no-localhost 2>&1 || gcloud auth print-access-token 2>&1 || firebase --token "$(firebase login:ci)"');
    return stdout.trim();
  } catch (error) {
    // Try alternative method
    try {
      const { stdout } = await execAsync('gcloud auth application-default print-access-token');
      return stdout.trim();
    } catch (err) {
      throw new Error('Could not get access token. Please run: firebase login');
    }
  }
}

async function checkExistingActiveConfig(accessToken) {
  const url = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents:runQuery`;

  const query = {
    structuredQuery: {
      from: [{ collectionId: COLLECTION }],
      where: {
        fieldFilter: {
          field: { fieldPath: 'status' },
          op: 'EQUAL',
          value: { stringValue: 'active' }
        }
      },
      limit: 1
    }
  };

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(query)
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data[0]?.document ? data[0].document : null;
  } catch (error) {
    console.warn('⚠️  Could not check for existing config:', error.message);
    return null;
  }
}

async function archiveConfig(docPath, accessToken) {
  const url = `https://firestore.googleapis.com/v1/${docPath}?updateMask.fieldPaths=status&updateMask.fieldPaths=archived_at&updateMask.fieldPaths=updated_at`;

  const updateData = {
    fields: {
      status: { stringValue: 'archived' },
      archived_at: { timestampValue: new Date().toISOString() },
      updated_at: { timestampValue: new Date().toISOString() }
    }
  };

  try {
    const response = await fetch(url, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    console.log('✅ Existing config archived');
  } catch (error) {
    console.error('❌ Failed to archive existing config:', error.message);
  }
}

function convertToFirestoreDocument(configData) {
  const convertValue = (value) => {
    if (value === null || value === undefined) {
      return { nullValue: null };
    }
    if (typeof value === 'string') {
      return { stringValue: value };
    }
    if (typeof value === 'number') {
      return { integerValue: String(Math.floor(value)) };
    }
    if (typeof value === 'boolean') {
      return { booleanValue: value };
    }
    if (Array.isArray(value)) {
      return {
        arrayValue: {
          values: value.map(convertValue)
        }
      };
    }
    if (typeof value === 'object') {
      return {
        mapValue: {
          fields: Object.entries(value).reduce((acc, [k, v]) => {
            acc[k] = convertValue(v);
            return acc;
          }, {})
        }
      };
    }
    return { stringValue: String(value) };
  };

  return {
    fields: Object.entries(configData).reduce((acc, [key, value]) => {
      acc[key] = convertValue(value);
      return acc;
    }, {})
  };
}

async function importConfig(accessToken) {
  // Read config file
  const configPath = path.join(__dirname, 'onboarding_personalization_config.json');

  if (!fs.existsSync(configPath)) {
    throw new Error(`Config file not found: ${configPath}`);
  }

  const configData = JSON.parse(fs.readFileSync(configPath, 'utf8'));
  console.log(`📖 Loaded: ${configData.title}`);
  console.log(`   Version: ${configData.version}`);
  console.log(`   Questions: ${configData.questions.length}\n`);

  // Add timestamps
  configData.created_at = new Date().toISOString();
  configData.updated_at = new Date().toISOString();

  if (configData.status === 'active') {
    configData.published_at = new Date().toISOString();
    configData.published_by = configData.created_by || 'admin';
  }

  // Check for existing active config
  const existingDoc = await checkExistingActiveConfig(accessToken);

  if (existingDoc) {
    console.warn('⚠️  Active config exists, archiving...');
    await archiveConfig(existingDoc.name, accessToken);
  }

  // Convert to Firestore format
  const firestoreDoc = convertToFirestoreDocument(configData);

  // Import to Firestore
  const url = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/${COLLECTION}`;

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(firestoreDoc)
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(`HTTP ${response.status}: ${error}`);
  }

  const result = await response.json();
  const docId = result.name.split('/').pop();

  console.log(`\n✅ Successfully imported!`);
  console.log(`   Document ID: ${docId}`);
  console.log(`   Collection: ${COLLECTION}`);
  console.log(`   Status: ${configData.status}`);

  // Summary
  console.log('\n📊 Summary:');
  console.log(`   - Total questions: ${configData.questions.length}`);

  const byCategory = configData.questions.reduce((acc, q) => {
    acc[q.category] = (acc[q.category] || 0) + 1;
    return acc;
  }, {});

  console.log(`   - By category:`);
  Object.entries(byCategory).forEach(([cat, count]) => {
    console.log(`     • ${cat}: ${count}`);
  });

  console.log('\n🎉 Import completed!');
  console.log('\n📱 Test in Android app by creating a new account.');

  return docId;
}

// Main
(async () => {
  try {
    console.log('🔑 Getting access token...');
    const accessToken = await getAccessToken();
    console.log('✅ Access token obtained\n');

    await importConfig(accessToken);
    process.exit(0);
  } catch (error) {
    console.error('\n❌ Error:', error.message);
    console.error('\nPlease ensure:');
    console.error('1. You are logged in: firebase login');
    console.error('2. You have Firestore write access');
    console.error('3. The config file exists: onboarding_personalization_config.json');
    process.exit(1);
  }
})();
