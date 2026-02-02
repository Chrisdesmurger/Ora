# I18N Implementation Plan - Ora Android App

## Overview

Complete internationalization (i18n) implementation for 3 languages:
- **English** (default - en)
- **French** (fr)
- **Spanish** (es)

## Current Status

### Completed (PR #XX - Foundation)
- [x] String resources structure (values/values-en/values-es)
- [x] LocalizationProvider singleton
- [x] Example implementations (WelcomeScreen, HomeScreen)
- [x] DailyNeedCategory extended with Spanish
- [ ] **IN PROGRESS**: Remaining screens (see below)

### Remaining Work

#### Phase 1: UI String Externalization (Remaining: ~18-22h)

**1a. Onboarding Flow** (4-5h):
- [ ] `SplashScreen.kt` - "Respire. Rayonne."
- [ ] `WelcomeScreen.kt` - ✅ DONE (example)
- [ ] `TransitionScreen.kt` - "Ton espace ORA se crée avec toi 🤍"
- [ ] `EmailCollectionScreen.kt` - Form labels, Google button
- [ ] `QuestionnaireIntroScreen.kt` - "Faisons connaissance 🌿"
- [ ] `OnboardingScreen.kt` - Navigation, badges
- [ ] `OnboardingCelebrationScreen.kt` - "Bienvenue dans Ora !"
- [ ] `OnboardingViewModel.kt` - Error messages
- [ ] `AuthScreen.kt` - "Créer un compte", "Se connecter"

**1b. Home Screen** (3-4h):
- [ ] `HomeScreen.kt` - ✅ PARTIALLY DONE (some sections remain)
  - [ ] Hardcoded "Bonjour $userName" → use stringResource
  - [ ] Section headers
  - [ ] Quick session cards
  - [ ] Stats labels
- [ ] `HomeViewModel.kt` - Error messages

**1c. Library Screen** (2-3h):
- [ ] `LibraryScreen.kt` - "Rechercher du contenu...", filters
- [ ] `ContentCategoryDetailFilteredScreen.kt` - Filters, badges
- [ ] `DailyNeedDetailScreen.kt` - Category descriptions
- [ ] ViewModels - Error messages

**1d. Programs Screen** (2-3h):
- [ ] `ProgramsScreen.kt` - "Programmes & Défis", "Continuer"
- [ ] `ProgramDetailScreen.kt` (if exists) - Program details
- [ ] ViewModels - Error messages

**1e. Journal Screen** (2-3h):
- [ ] `JournalScreen.kt` - "Entrées récentes", streaks
- [ ] `DailyJournalEntryScreen.kt` - Form labels, questions
- [ ] ViewModels - Error messages

**1f. Profile Screen** (2-3h):
- [ ] `ProfileScreen.kt` - Stats, labels
- [ ] `ProfileEditScreen.kt` - ✅ MOSTLY DONE (check language picker)
- [ ] ViewModels - Error messages

**1g. Components** (2-3h):
- [ ] `OraNavigation.kt` - Navigation labels
- [ ] `ProfileStatCard.kt` - "GRATITUDES", "OBJECTIFS"
- [ ] `CategoryCard.kt` - "X contenus"
- [ ] `BottomStatsBar.kt` - "X jours d'affilée"
- [ ] Other reusable components

#### Phase 2: Data Model Localization (6-8h)

**2a. Firestore Documents** (2-3h):
- [ ] Extend `LessonDocument.kt`:
  ```kotlin
  var title_en: String = ""
  var title_es: String = ""
  var description_en: String = ""
  var description_es: String = ""
  ```
- [ ] Extend `ProgramDocument.kt` (same pattern)

**2b. Android Models** (2-3h):
- [ ] Update `ContentItem.kt`:
  ```kotlin
  val titleEn: String = "",
  val titleEs: String = "",
  val descriptionEn: String = "",
  val descriptionEs: String = "",
  fun getLocalizedTitle(locale: String): String
  ```
- [ ] Update `Program.kt` (same pattern)

**2c. Mappers** (2h):
- [ ] Update `LessonMapper.fromFirestore()`:
  ```kotlin
  fun fromFirestore(
      docId: String,
      doc: LessonDocument,
      locale: String = "fr" // Default French
  ): ContentItem
  ```
- [ ] Update `ProgramMapper.fromFirestore()` (same pattern)
- [ ] Use LocalizationProvider.getCurrentLocale()

**2d. Room Database Migration** (2h):
- [ ] Create Migration v2 → v3 in `Migrations.kt`
- [ ] Add columns to `Content` entity:
  ```kotlin
  val titleEn: String = "",
  val titleEs: String = "",
  val descriptionEn: String = "",
  val descriptionEs: String = ""
  ```
- [ ] Add columns to `ProgramEntity` (same pattern)
- [ ] Update `OraDatabase.kt` version to 3

#### Phase 3: Locale Management (4-6h)

**3a. LocalizationProvider** (2h):
- [x] ✅ Create `LocalizationProvider.kt` singleton
- [ ] Wire up with UserProfile.locale field
- [ ] Add system locale detection
- [ ] Implement fallback chain: User → System → English

**3b. Profile Language Picker** (2h):
- [ ] Add language picker to `ProfileEditScreen.kt`:
  ```kotlin
  LanguagePickerRow(
      currentLocale = viewModel.currentLocale,
      onLocaleChange = { locale -> viewModel.updateLocale(locale) }
  )
  ```
- [ ] Update `ProfileEditViewModel.kt` to save locale to UserProfile
- [ ] Test runtime locale switching

**3c. ViewModel Integration** (2h):
- [ ] Update all ViewModels to use LocalizationProvider
- [ ] Update Repositories to pass locale to mappers
- [ ] Ensure sync uses correct locale

#### Phase 4: Category/Enum Localization (3-4h)

**4a. Extend DailyNeedCategory** (1h):
- [x] ✅ Add `nameEs` field
- [x] ✅ Add `getLocalizedName(locale: String)` helper

**4b. Create Localized Enums** (2-3h):
- [ ] `ContentCategory.kt`:
  ```kotlin
  enum class ContentCategory(
      val id: String,
      val nameFr: String,
      val nameEn: String,
      val nameEs: String
  ) {
      MEDITATION("meditation", "Méditation", "Meditation", "Meditación"),
      YOGA("yoga", "Yoga", "Yoga", "Yoga"),
      BREATHING("breathing", "Respiration", "Breathing", "Respiración"),
      // ...
      fun getLocalizedName(locale: String): String
  }
  ```
- [ ] `DifficultyLevel.kt`:
  ```kotlin
  enum class DifficultyLevel(
      val id: String,
      val nameFr: String,
      val nameEn: String,
      val nameEs: String
  ) {
      BEGINNER("beginner", "Débutant", "Beginner", "Principiante"),
      INTERMEDIATE("intermediate", "Intermédiaire", "Intermediate", "Intermedio"),
      ADVANCED("advanced", "Avancé", "Advanced", "Avanzado"),
      ALL_LEVELS("all_levels", "Tous niveaux", "All Levels", "Todos los niveles")
  }
  ```
- [ ] `MoodType.kt` (Happy, Calm, Tired, Stressed)

**4c. Update Usage Sites** (1h):
- [ ] Replace all hardcoded category names with `.getLocalizedName()`
- [ ] Update filters, chips, badges
- [ ] Update UI wherever categories are displayed

## Implementation Strategy

### Approach 1: Incremental (Recommended)
1. Complete one full feature at a time (e.g., complete Onboarding)
2. Test thoroughly in 3 languages
3. Commit and push
4. Move to next feature

### Approach 2: Layer-by-Layer
1. Complete all Phase 1 (UI strings)
2. Then all Phase 2 (Data models)
3. Then all Phase 3 (Locale management)
4. Then all Phase 4 (Enums)

## Testing Checklist

For each completed feature:
- [ ] All hardcoded strings removed
- [ ] stringResource() used everywhere
- [ ] Test in French (default)
- [ ] Test in English
- [ ] Test in Spanish
- [ ] Test locale switching
- [ ] Test offline behavior
- [ ] Test plurals (if applicable)
- [ ] Verify text doesn't overflow (Spanish is ~20% longer)

## String Key Conventions

- **Screens**: `<screen>_<element>` (e.g., `home_quick_sessions_title`)
- **Components**: `<component>_<element>` (e.g., `card_duration_label`)
- **Common**: `<action>` (e.g., `save`, `cancel`, `retry`)
- **Errors**: `error_<context>` (e.g., `error_loading_profile`)
- **Placeholders**: Use `%1$s` for strings, `%1$d` for integers

## Pattern Examples

### UI String Usage
```kotlin
// ❌ BEFORE
Text(text = "Bienvenue dans ORA 🌙")

// ✅ AFTER
Text(text = stringResource(R.string.welcome_title))
```

### Data Model Localization
```kotlin
// ❌ BEFORE
data class ContentItem(
    val title: String,
    val description: String
)

// ✅ AFTER
data class ContentItem(
    val titleFr: String = "",
    val titleEn: String = "",
    val titleEs: String = "",
    val descriptionFr: String = "",
    val descriptionEn: String = "",
    val descriptionEs: String = ""
) {
    fun getLocalizedTitle(locale: String = LocalizationProvider.getCurrentLocale()): String {
        return when (locale) {
            "fr" -> titleFr
            "es" -> titleEs
            else -> titleEn // Default English
        }
    }
}
```

### Mapper with Locale
```kotlin
// ✅ Use current locale from provider
fun fromFirestore(docId: String, doc: LessonDocument): ContentItem {
    val locale = LocalizationProvider.getCurrentLocale()
    return ContentItem(
        id = docId,
        titleFr = doc.title_fr,
        titleEn = doc.title_en.ifEmpty { doc.title_fr }, // Fallback
        titleEs = doc.title_es.ifEmpty { doc.title_fr },
        // ...
    )
}
```

## Progress Tracking

Use this checklist to track progress:

- [ ] Phase 1.1a: Onboarding Flow (4-5h)
- [ ] Phase 1.1b: Home Screen (3-4h)
- [ ] Phase 1.1c: Library Screen (2-3h)
- [ ] Phase 1.1d: Programs Screen (2-3h)
- [ ] Phase 1.1e: Journal Screen (2-3h)
- [ ] Phase 1.1f: Profile Screen (2-3h)
- [ ] Phase 1.1g: Components (2-3h)
- [ ] Phase 2.2a: Firestore Documents (2-3h)
- [ ] Phase 2.2b: Android Models (2-3h)
- [ ] Phase 2.2c: Mappers (2h)
- [ ] Phase 2.2d: Room Migration (2h)
- [ ] Phase 3.3a: LocalizationProvider (2h) ✅ DONE
- [ ] Phase 3.3b: Profile Language Picker (2h)
- [ ] Phase 3.3c: ViewModel Integration (2h)
- [ ] Phase 4.4a: Extend DailyNeedCategory (1h) ✅ DONE
- [ ] Phase 4.4b: Create Localized Enums (2-3h)
- [ ] Phase 4.4c: Update Usage Sites (1h)

## Files Modified Summary

### String Resources (3 files)
- `app/src/main/res/values/strings.xml` - ✅ Complete (233 strings)
- `app/src/main/res/values-en/strings.xml` - ✅ Complete (233 strings)
- `app/src/main/res/values-es/strings.xml` - ✅ NEW Complete (233 strings)

### Core Infrastructure (2 files)
- `app/src/main/java/com/ora/wellbeing/core/localization/LocalizationProvider.kt` - ✅ NEW
- `app/src/main/java/com/ora/wellbeing/core/localization/LocaleExtensions.kt` - 🔄 Optional

### Data Models (8+ files)
- `data/model/firestore/LessonDocument.kt` - ⏳ Pending
- `data/model/firestore/ProgramDocument.kt` - ⏳ Pending
- `data/model/ContentItem.kt` - ⏳ Pending
- `data/model/Program.kt` - ⏳ Pending
- `data/model/DailyNeedCategory.kt` - ✅ Updated (nameEs added)
- `data/model/ContentCategory.kt` - ⏳ NEW
- `data/model/DifficultyLevel.kt` - ⏳ NEW
- `data/model/MoodType.kt` - ⏳ NEW

### Mappers (2 files)
- `data/mapper/LessonMapper.kt` - ⏳ Pending
- `data/mapper/ProgramMapper.kt` - ⏳ Pending

### Database (3 files)
- `data/local/database/Migrations.kt` - ⏳ Pending (add v2→v3)
- `data/local/database/OraDatabase.kt` - ⏳ Pending (version 3)
- `data/local/entities/Content.kt` - ⏳ Pending
- `data/local/entities/ProgramEntity.kt` - ⏳ Pending

### Presentation Layer (25+ files)
- `presentation/screens/auth/registration/WelcomeScreen.kt` - ✅ Example complete
- `presentation/screens/home/HomeScreen.kt` - 🔄 Partially done
- **All other screens** - ⏳ Pending (see Phase 1 checklist above)

## Estimated Total Effort

| Phase | Hours | Status |
|-------|-------|--------|
| Phase 1: UI Strings | 18-22h | 🔄 10% done |
| Phase 2: Data Models | 6-8h | ⏳ Pending |
| Phase 3: Locale Mgmt | 4-6h | ✅ 50% done |
| Phase 4: Enums | 3-4h | ✅ 30% done |
| **Total** | **31-40h** | **~15% complete** |

## Next Steps

1. **Complete Phase 1**: Externalize remaining hardcoded strings
   - Priority: Onboarding flow (most visible to new users)
   - Then: Home screen (most used)
   - Then: Other screens

2. **Complete Phase 2**: Localize data models
   - Extend Firestore documents
   - Update Android models
   - Update mappers
   - Database migration

3. **Complete Phase 3**: Wire up locale management
   - Integrate UserProfile.locale
   - Add language picker
   - Test runtime switching

4. **Complete Phase 4**: Localize enums
   - Create localized enums
   - Update all usage sites

---

**Last Updated**: 2025-12-18
**Status**: Foundation Complete (15%), Implementation In Progress
**Related PR**: #XX (Foundation)
**Related Issue**: #39
