// Fix information screens order based on their position
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Load service account from .env.temp file
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
  process.exit(1);
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

async function fixInformationScreensOrder() {
  console.log('\n🔧 Fixing Information Screens Order...\n');

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

    console.log('✅ Active configuration found: ' + doc.id + '\n');

    const informationScreens = data.informationScreens || [];

    if (informationScreens.length === 0) {
      console.log('❌ No information screens found!');
      process.exit(1);
    }

    console.log('📋 Current information screens order:\n');
    informationScreens.forEach((screen, index) => {
      console.log(`   ${index + 1}. [position: ${screen.position}, order: ${screen.order}] "${screen.titleFr || screen.title}"`);
    });

    // Define the correct order mapping based on position
    // Position 0 (Bienvenue) should be at the very beginning → order: -2
    // Position 1 (Construisons ton profil) should be just before question 0 → order: -1
    // Position 6 (C'est noté !) should be after question 5 (order: 5) → order: 5.5 or better: 50
    // Position 13 (Merci !) should be after last question (order: 13) → order: 14
    // Position 100 (Ton espace ORA est prêt) should be at the very end → order: 999

    const orderMapping = {
      0: -2,    // Bienvenue (before everything)
      1: -1,    // Construisons ton profil ORA (before first question)
      6: 50,    // C'est noté ! (after question 5, let's use 50 to be safe)
      13: 140,  // Merci ! (after question 13)
      100: 999  // Ton espace ORA est prêt (at the very end)
    };

    console.log('\n🔄 Applying new order mapping:\n');
    console.log('   Position → New Order');
    Object.entries(orderMapping).forEach(([pos, ord]) => {
      console.log(`   ${pos} → ${ord}`);
    });

    // Update information screens with new order
    const updatedScreens = informationScreens.map(screen => {
      const newOrder = orderMapping[screen.position];
      if (newOrder !== undefined) {
        console.log(`\n   ✓ Updating "${screen.titleFr || screen.title}" from order ${screen.order} to ${newOrder}`);
        return { ...screen, order: newOrder };
      }
      return screen;
    });

    console.log('\n📝 Updating Firestore document...\n');

    await doc.ref.update({
      informationScreens: updatedScreens,
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log('✅ Information screens order updated successfully!\n');

    // Verify the update
    console.log('📋 New information screens order:\n');
    updatedScreens.forEach((screen, index) => {
      console.log(`   ${index + 1}. [position: ${screen.position}, order: ${screen.order}] "${screen.titleFr || screen.title}"`);
    });

    // Show final merged order
    console.log('\n📱 FINAL MERGED ORDER (after fix):\n');

    const questions = data.questions || [];
    const allItems = [
      ...questions.map(q => ({
        order: q.order,
        type: q.type?.kind || 'unknown',
        title: q.titleFr || q.title
      })),
      ...updatedScreens.map(s => ({
        order: s.order,
        type: 'information_screen',
        title: s.titleFr || s.title,
        position: s.position
      }))
    ].sort((a, b) => a.order - b.order);

    allItems.forEach((item, index) => {
      const posStr = item.position !== undefined ? `, position: ${item.position}` : '';
      console.log(`   ${index + 1}. [order: ${item.order}${posStr}] [${item.type}] "${item.title}"`);
    });

    console.log('\n✅ Fix complete!\n');

  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }

  process.exit(0);
}

fixInformationScreensOrder();
