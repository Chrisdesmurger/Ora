# Grouper les questions de profil dans l'onboarding de personnalisation

## 🎯 Objectif

Regrouper les 3 questions de profil utilisateur (prénom, date de naissance, genre) dans un seul écran d'onboarding au lieu de 3 écrans séparés, et synchroniser automatiquement ces données avec la collection `users` dans Firestore.

## 📋 Description

Actuellement, l'onboarding de personnalisation comprend 3 questions séparées pour collecter les informations de profil :
- **Question 1** (order: 0): Prénom (text_input)
- **Question 2** (order: 1): Date de naissance (text_input)
- **Question 3** (order: 2): Genre (multiple_choice)

Ces questions doivent être regroupées en une seule question de type `profile_group` qui affichera les 3 champs dans le même écran.

## 🎨 Design requis

### Nouveau type de question : `profile_group`

Un nouvel écran qui affiche :
- **Titre** : "Construisons ton profil"
- **Sous-titre** : "Pour personnaliser ton expérience ORA"
- **3 champs** :
  1. Prénom (Input text)
  2. Date de naissance (Date picker ou input avec format JJ/MM/AAAA)
  3. Genre (Radio buttons ou Dropdown)

### Structure de la question

```json
{
  "id": "user_profile",
  "category": "personalization",
  "order": 0,
  "title": "Construisons ton profil",
  "titleFr": "Construisons ton profil",
  "titleEn": "Let's build your profile",
  "subtitle": "Pour personnaliser ton expérience ORA",
  "subtitleFr": "Pour personnaliser ton expérience ORA",
  "subtitleEn": "To personalize your ORA experience",
  "type": {
    "kind": "profile_group",
    "fields": [
      {
        "id": "firstName",
        "label": "Prénom",
        "labelFr": "Prénom",
        "labelEn": "First name",
        "inputType": "text",
        "placeholder": "Ton prénom",
        "maxLength": 50,
        "required": true
      },
      {
        "id": "birthDate",
        "label": "Date de naissance",
        "labelFr": "Date de naissance",
        "labelEn": "Date of birth",
        "inputType": "date",
        "placeholder": "JJ/MM/AAAA",
        "required": true
      },
      {
        "id": "gender",
        "label": "Genre",
        "labelFr": "Genre",
        "labelEn": "Gender",
        "inputType": "radio",
        "required": true,
        "options": [
          {
            "id": "female",
            "label": "Femme",
            "labelFr": "Femme",
            "labelEn": "Female",
            "icon": "♀️"
          },
          {
            "id": "male",
            "label": "Homme",
            "labelFr": "Homme",
            "labelEn": "Male",
            "icon": "♂️"
          },
          {
            "id": "non_binary",
            "label": "Non binaire",
            "labelFr": "Non binaire",
            "labelEn": "Non-binary",
            "icon": "⚧"
          },
          {
            "id": "prefer_not_say",
            "label": "Je préfère ne pas le dire",
            "labelFr": "Je préfère ne pas le dire",
            "labelEn": "I prefer not to say",
            "icon": "🙅"
          }
        ]
      }
    ]
  },
  "required": true
}
```

## 🔧 Implémentation technique

### 1. Android - Modèles de données

**Fichier** : `app/src/main/java/com/ora/wellbeing/data/model/onboarding/QuestionType.kt`

Ajouter le nouveau type de question :

```kotlin
enum class QuestionTypeKind {
    // ... types existants
    PROFILE_GROUP  // Nouveau type
}

data class ProfileGroupType(
    val fields: List<ProfileField>
) : QuestionType() {
    override val kind = QuestionTypeKind.PROFILE_GROUP
}

data class ProfileField(
    val id: String,
    val label: String,
    val labelFr: String,
    val labelEn: String,
    val inputType: ProfileFieldInputType,
    val placeholder: String? = null,
    val maxLength: Int? = null,
    val required: Boolean = false,
    val options: List<ProfileFieldOption>? = null
)

enum class ProfileFieldInputType {
    TEXT,
    DATE,
    RADIO
}

data class ProfileFieldOption(
    val id: String,
    val label: String,
    val labelFr: String,
    val labelEn: String,
    val icon: String? = null
)
```

### 2. Android - UI Composable

**Fichier** : `app/src/main/java/com/ora/wellbeing/presentation/screens/onboarding/OnboardingScreen.kt`

Créer un nouveau composable `ProfileGroupContent` :

```kotlin
@Composable
fun ProfileGroupContent(
    question: OnboardingQuestion,
    profileData: Map<String, String>,
    onProfileDataChange: (Map<String, String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titre et sous-titre
        Text(
            text = question.title,
            style = MaterialTheme.typography.headlineMedium
        )

        question.subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Champs dynamiques
        (question.type as? ProfileGroupType)?.fields?.forEach { field ->
            when (field.inputType) {
                ProfileFieldInputType.TEXT -> {
                    OutlinedTextField(
                        value = profileData[field.id] ?: "",
                        onValueChange = { value ->
                            onProfileDataChange(profileData + (field.id to value))
                        },
                        label = { Text(field.label) },
                        placeholder = field.placeholder?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ProfileFieldInputType.DATE -> {
                    // Date picker implementation
                    DateInputField(
                        value = profileData[field.id] ?: "",
                        onValueChange = { value ->
                            onProfileDataChange(profileData + (field.id to value))
                        },
                        label = field.label,
                        placeholder = field.placeholder
                    )
                }
                ProfileFieldInputType.RADIO -> {
                    // Radio buttons for gender
                    RadioGroupField(
                        value = profileData[field.id] ?: "",
                        options = field.options ?: emptyList(),
                        onValueChange = { value ->
                            onProfileDataChange(profileData + (field.id to value))
                        },
                        label = field.label
                    )
                }
            }
        }
    }
}
```

### 3. Firestore - Synchronisation

**Fichier** : `app/src/main/java/com/ora/wellbeing/data/repository/UserProfileRepository.kt`

Ajouter une méthode pour mettre à jour le profil utilisateur :

```kotlin
suspend fun updateUserProfile(
    userId: String,
    firstName: String?,
    birthDate: String?,
    gender: String?
): Result<Unit> {
    return try {
        val updates = mutableMapOf<String, Any>()

        firstName?.let { updates["firstName"] = it }
        birthDate?.let { updates["birthDate"] = it }
        gender?.let { updates["gender"] = it }
        updates["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection("users")
            .document(userId)
            .update(updates)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 4. ViewModel - Gestion d'état

**Fichier** : `app/src/main/java/com/ora/wellbeing/presentation/screens/onboarding/OnboardingViewModel.kt`

Gérer les données du profil groupé :

```kotlin
private val _profileData = MutableStateFlow<Map<String, String>>(emptyMap())
val profileData: StateFlow<Map<String, String>> = _profileData.asStateFlow()

fun updateProfileData(data: Map<String, String>) {
    _profileData.value = data
}

fun saveProfileGroup(userId: String) {
    viewModelScope.launch {
        val data = _profileData.value
        userProfileRepository.updateUserProfile(
            userId = userId,
            firstName = data["firstName"],
            birthDate = data["birthDate"],
            gender = data["gender"]
        )
    }
}
```

## 📊 Impacts

### Collections Firestore modifiées

**Collection** : `users`

Champs mis à jour :
- `firstName` (String)
- `birthDate` (String, format JJ/MM/AAAA)
- `gender` (String: "female", "male", "non_binary", "prefer_not_say")
- `updatedAt` (Timestamp)

### Configuration onboarding

**Fichier** : `firebase/onboarding_personalization_config.json`

- ✅ Remplacer les 3 questions séparées par 1 question `profile_group`
- ✅ Réduire le nombre total de questions de 16 à 14
- ✅ Conserver les 5 information screens

## ✅ Critères d'acceptation

- [ ] Le nouveau type `PROFILE_GROUP` est ajouté à `QuestionType.kt`
- [ ] Le composable `ProfileGroupContent` affiche les 3 champs correctement
- [ ] La validation des champs fonctionne (champs requis, format date)
- [ ] Les données sont synchronisées avec Firestore `users` collection
- [ ] La configuration d'onboarding est mise à jour dans Firebase
- [ ] Les 3 questions séparées sont supprimées
- [ ] L'écran est responsive et accessible
- [ ] Les traductions FR/EN fonctionnent
- [ ] Tests unitaires ajoutés pour le nouveau type
- [ ] Tests UI pour le composable ProfileGroupContent

## 🧪 Plan de test

1. **Test de l'UI** :
   - Vérifier l'affichage des 3 champs
   - Tester la validation (champs vides)
   - Tester le format de date (JJ/MM/AAAA)
   - Tester les radio buttons pour le genre

2. **Test de synchronisation** :
   - Vérifier que les données sont envoyées à Firestore
   - Vérifier que `updatedAt` est mis à jour
   - Tester le comportement en cas d'erreur réseau

3. **Test de navigation** :
   - Vérifier la transition vers la question suivante
   - Vérifier le bouton "Retour" conserve les données

## 📚 Références

- Design guide : `docs/CANVA_INFORMATION_SCREENS_DESIGN_GUIDE.md`
- Firestore mapping : `docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md`
- Onboarding architecture : `app/src/main/java/com/ora/wellbeing/presentation/screens/onboarding/`

## 🔗 Liens connexes

- Configuration actuelle : `firebase/onboarding_personalization_config.json`
- Collection Firestore : `users` (ora-wellbeing project)
- OraWebApp admin : https://ora-admin-6cc2d.web.app/onboarding

---

**Labels** : `enhancement`, `onboarding`, `firebase`, `ui`
**Milestone** : Onboarding Personnalisation v1.0
**Assignee** : @chrisdesmurger
**Branch** : `feature/onboarding-profile-group`
