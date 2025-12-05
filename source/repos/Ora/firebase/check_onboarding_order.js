// Check onboarding configuration order in Firestore
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Try to load service account from .env.temp file
let serviceAccount;
try {
  const envPath = path.join(__dirname, '.env.temp');
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    const match = envContent.match(/FIREBASE_SERVICE_ACCOUNT_JSON=(.+)/);
    if (match) {
      serviceAccount = JSON.parse(match[1]);
    }
  }
} catch (error) {
  console.error('❌ Error loading service account:', error.message);
}

if (!serviceAccount) {
  console.error('❌ FIREBASE_SERVICE_ACCOUNT_JSON not found');
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'ora-wellbeing'
});

const db = admin.firestore();

async function checkOnboardingOrder() {
  console.log('\n🔍 Checking Onboarding Configuration Order...\n');

  try {
    const snapshot = await db.collection('onboarding_configs')
      .where('status', '==', 'active')
      .get();

    if (snapshot.empty) {
      console.log('❌ No active configuration found!');
      process.exit(1);
    }

    const doc = snapshot.docs[0];
    const data = doc.data();

    console.log('✅ Active configuration found!');
    console.log(`   ID: ${doc.id}`);
    console.log(`   Title: ${data.title}`);
    console.log(`   Version: ${data.version}\n`);

    // Check information_screens array
    console.log('📋 INFORMATION_SCREENS Array:');
    console.log(`   Count: ${data.information_screens?.length || 0}\n`);

    if (data.information_screens && data.information_screens.length > 0) {
      data.information_screens.forEach((screen, index) => {
        console.log(`   ${index + 1}. [position: ${screen.position}, order: ${screen.order}] "${screen.titleFr || screen.title}"`);
      });
    }

    // Check informationScreens array (camelCase)
    console.log('\n📋 informationScreens Array (camelCase):');
    console.log(`   Count: ${data.informationScreens?.length || 0}\n`);

    if (data.informationScreens && data.informationScreens.length > 0) {
      data.informationScreens.forEach((screen, index) => {
        console.log(`   ${index + 1}. [position: ${screen.position}, order: ${screen.order}] "${screen.titleFr || screen.title}"`);
      });
    }

    // Check questions array
    console.log('\n📋 QUESTIONS Array:');
    console.log(`   Count: ${data.questions?.length || 0}\n`);

    // Sort questions by order and display
    const sortedQuestions = (data.questions || []).sort((a, b) => a.order - b.order);

    sortedQuestions.forEach((q, index) => {
      const type = q.type?.kind || 'unknown';
      const title = q.titleFr || q.title || 'Untitled';
      console.log(`   ${index + 1}. [order: ${q.order}] [${type}] "${title}"`);
    });

    // Find information_screen types in questions
    const infoScreensInQuestions = sortedQuestions.filter(q => q.type?.kind === 'information_screen');

    if (infoScreensInQuestions.length > 0) {
      console.log('\n⚠️  WARNING: Information screens found in QUESTIONS array!');
      console.log('   These should be in information_screens array instead:\n');
      infoScreensInQuestions.forEach((q, index) => {
        console.log(`   ${index + 1}. [order: ${q.order}] "${q.titleFr || q.title}"`);
      });
    }

    // Show the merged order that Android app would use
    console.log('\n📱 ANDROID APP MERGED ORDER (sorted by "order" field):');
    console.log('   This is how items will appear in the app:\n');

    const allItems = [
      ...sortedQuestions.map(q => ({
        order: q.order,
        type: q.type?.kind || 'unknown',
        title: q.titleFr || q.title,
        source: 'questions'
      })),
      ...(data.information_screens || []).map(s => ({
        order: s.order,
        type: 'information_screen',
        title: s.titleFr || s.title,
        source: 'information_screens',
        position: s.position
      })),
      ...(data.informationScreens || []).map(s => ({
        order: s.order,
        type: 'information_screen',
        title: s.titleFr || s.title,
        source: 'informationScreens',
        position: s.position
      }))
    ].sort((a, b) => a.order - b.order);

    allItems.forEach((item, index) => {
      const posStr = item.position !== undefined ? `, position: ${item.position}` : '';
      console.log(`   ${index + 1}. [order: ${item.order}${posStr}] [${item.type}] "${item.title}" (from ${item.source})`);
    });

    console.log('\n✅ Check complete!\n');

  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }

  process.exit(0);
}

checkOnboardingOrder();
