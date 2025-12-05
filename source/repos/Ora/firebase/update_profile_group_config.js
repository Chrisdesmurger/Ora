const fs = require('fs');

// Read the current configuration
const currentConfig = JSON.parse(fs.readFileSync('onboarding_personalization_config.json', 'utf8'));

// Create the new profile_group question
const profileGroupQuestion = {
  id: 'user_profile',
  category: 'personalization',
  order: 0,
  title: 'Construisons ton profil',
  titleFr: 'Construisons ton profil',
  titleEn: 'Let\'s build your profile',
  subtitle: 'Pour personnaliser ton expérience ORA',
  subtitleFr: 'Pour personnaliser ton expérience ORA',
  subtitleEn: 'To personalize your ORA experience',
  type: {
    kind: 'profile_group',
    fields: [
      {
        id: 'firstName',
        label: 'Prénom',
        labelFr: 'Prénom',
        labelEn: 'First name',
        inputType: 'text',
        placeholder: 'Ton prénom',
        maxLength: 50,
        required: true,
        order: 0
      },
      {
        id: 'birthDate',
        label: 'Date de naissance',
        labelFr: 'Date de naissance',
        labelEn: 'Date of birth',
        inputType: 'date',
        placeholder: 'JJ/MM/AAAA',
        required: true,
        order: 1
      },
      {
        id: 'gender',
        label: 'Genre',
        labelFr: 'Genre',
        labelEn: 'Gender',
        inputType: 'radio',
        required: true,
        order: 2,
        options: [
          {
            id: 'female',
            label: 'Femme',
            labelFr: 'Femme',
            labelEn: 'Female',
            icon: '♀️',
            order: 0
          },
          {
            id: 'male',
            label: 'Homme',
            labelFr: 'Homme',
            labelEn: 'Male',
            icon: '♂️',
            order: 1
          },
          {
            id: 'non_binary',
            label: 'Non binaire',
            labelFr: 'Non binaire',
            labelEn: 'Non-binary',
            icon: '⚧',
            order: 2
          },
          {
            id: 'prefer_not_say',
            label: 'Je préfère ne pas le dire',
            labelFr: 'Je préfère ne pas le dire',
            labelEn: 'I prefer not to say',
            icon: '🙅',
            order: 3
          }
        ]
      }
    ]
  },
  options: [],
  required: true
};

// Filter out the old profile questions (first_name, birth_date, gender)
const filteredQuestions = currentConfig.questions.filter(q =>
  !['first_name', 'birth_date', 'gender'].includes(q.id)
);

// Insert the new profile_group question at the beginning
const newQuestions = [profileGroupQuestion, ...filteredQuestions];

// Renumber all questions
newQuestions.forEach((q, index) => {
  q.order = index;
});

// Create the new configuration
const newConfig = {
  ...currentConfig,
  version: '1.1',
  questions: newQuestions
};

// Save the new configuration
fs.writeFileSync('onboarding_personalization_config_v1.1.json', JSON.stringify(newConfig, null, 2));

console.log('✅ Configuration mise à jour créée!');
console.log(`   Version: ${newConfig.version}`);
console.log(`   Questions: ${currentConfig.questions.length} → ${newConfig.questions.length}`);
console.log(`   Information Screens: ${newConfig.informationScreens.length}`);
console.log('\n📋 Changements:');
console.log('   - Suppression de 3 questions séparées (first_name, birth_date, gender)');
console.log('   - Ajout de 1 question profile_group avec 3 champs');
console.log('   - Total: 16 → 14 questions\n');
console.log('📄 Fichier créé: onboarding_personalization_config_v1.1.json');
console.log('\n🚀 Pour importer dans Firebase:');
console.log('   1. Vérifier le fichier: onboarding_personalization_config_v1.1.json');
console.log('   2. Copier vers: onboarding_personalization_config.json');
console.log('   3. Lancer: node import_final.js');
