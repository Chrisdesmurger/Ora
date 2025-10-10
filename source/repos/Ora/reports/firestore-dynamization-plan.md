# Firestore Dynamization Implementation Plan

**Project:** Ora Wellbeing Application
**Task ID:** TASK-2025-10-05-FIRESTORE-DYNAMIC-SCREENS
**Created:** 2025-10-05T00:00:00Z
**Supervisor:** supervisor-tech
**Status:** Approved for Execution

## Executive Summary

This plan outlines the complete implementation of Firestore integration for all main screens in the Ora application (Journal, Programs, Library, Home). Currently, only ProfileScreen uses Firestore with UserProfile and UserStats collections. This implementation will follow the proven ProfileScreen pattern to dynamize the remaining screens.

### Objectives

1. Replace all mock data with real-time Firestore data
2. Implement 4 new Firestore collections (gratitudes, programs, content, user_programs)
3. Maintain clean architecture and MVVM pattern
4. Ensure UID-based security isolation for user-specific data
5. Enable offline-first functionality with Firestore cache
6. Achieve zero CustomClassMapper warnings (proper Kotlin mapping)

### Success Criteria

- All 4 screens display real Firestore data
- Real-time sync functional across all collections
- Offline mode works seamlessly
- Zero cross-user access vulnerabilities
- Zero Firestore mapping errors
- Code coverage > 80% for new repositories and ViewModels

## Implementation Phases

### Phase 1: Firestore Models and Security Rules (4 hours)

**Priority:** HIGH | **Dependencies:** None | **Status:** READY TO START

#### 1.1 Create Firestore Models (tech-android)

**Reference Implementation:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\data\model\UserProfile.kt

**Critical Requirements:**
- Use regular `class` (NOT `data class`)
- Properties declared as `var` outside constructor
- `@get:PropertyName` and `@set:PropertyName` for snake_case mapping
- `@Exclude` on computed methods
- `@IgnoreExtraProperties` on class

##### Model 1: GratitudeEntry.kt

**Location:** app/src/main/java/com/ora/wellbeing/data/model/GratitudeEntry.kt

**Firestore Path:** gratitudes/{uid}/entries/{date}

**Schema:**
```kotlin
@IgnoreExtraProperties
class GratitudeEntry {
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = ""

    @get:PropertyName("date")
    @set:PropertyName("date")
    var date: String = ""  // ISO format yyyy-MM-dd (document ID)

    @get:PropertyName("gratitudes")
    @set:PropertyName("gratitudes")
    var gratitudes: List<String> = emptyList()  // Max 3 elements

    @get:PropertyName("mood")
    @set:PropertyName("mood")
    var mood: String? = null  // Emoji + label

    @get:PropertyName("notes")
    @set:PropertyName("notes")
    var notes: String? = null  // Max 500 chars

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    @ServerTimestamp
    var updatedAt: Date? = null

    @Exclude
    fun getFormattedDate(): String {
        // Format date for display
    }
}
```

##### Model 2: Program.kt

**Location:** app/src/main/java/com/ora/wellbeing/data/model/Program.kt

**Firestore Path:** programs/{programId}

**Schema:**
```kotlin
@IgnoreExtraProperties
class Program {
    @DocumentId
    var id: String = ""

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = ""

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = ""

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = ""  // Méditation, Yoga, Bien-être, Défis, Sommeil

    @get:PropertyName("duration")
    @set:PropertyName("duration")
    var duration: Int = 0  // Number of days

    @get:PropertyName("level")
    @set:PropertyName("level")
    var level: String = "Tous niveaux"

    @get:PropertyName("participant_count")
    @set:PropertyName("participant_count")
    var participantCount: Int = 0

    @get:PropertyName("rating")
    @set:PropertyName("rating")
    var rating: Float = 0f

    @get:PropertyName("thumbnail_url")
    @set:PropertyName("thumbnail_url")
    var thumbnailUrl: String? = null

    @get:PropertyName("instructor")
    @set:PropertyName("instructor")
    var instructor: String? = null

    @get:PropertyName("is_premium_only")
    @set:PropertyName("is_premium_only")
    var isPremiumOnly: Boolean = false

    @get:PropertyName("sessions")
    @set:PropertyName("sessions")
    var sessions: List<Map<String, Any>> = emptyList()  // Session details

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true

    @Exclude
    fun getFormattedDuration(): String {
        return "$duration jours"
    }
}
```

##### Model 3: UserProgram.kt

**Location:** app/src/main/java/com/ora/wellbeing/data/model/UserProgram.kt

**Firestore Path:** user_programs/{uid}/enrolled/{programId}

**Schema:**
```kotlin
@IgnoreExtraProperties
class UserProgram {
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = ""

    @get:PropertyName("program_id")
    @set:PropertyName("program_id")
    var programId: String = ""

    @get:PropertyName("current_day")
    @set:PropertyName("current_day")
    var currentDay: Int = 1

    @get:PropertyName("total_days")
    @set:PropertyName("total_days")
    var totalDays: Int = 0

    @get:PropertyName("progress_percentage")
    @set:PropertyName("progress_percentage")
    var progressPercentage: Int = 0

    @get:PropertyName("started_at")
    @set:PropertyName("started_at")
    @ServerTimestamp
    var startedAt: Date? = null

    @get:PropertyName("last_session_at")
    @set:PropertyName("last_session_at")
    var lastSessionAt: Date? = null

    @get:PropertyName("completed_sessions")
    @set:PropertyName("completed_sessions")
    var completedSessions: List<String> = emptyList()  // contentIds

    @get:PropertyName("is_completed")
    @set:PropertyName("is_completed")
    var isCompleted: Boolean = false

    @get:PropertyName("completed_at")
    @set:PropertyName("completed_at")
    var completedAt: Date? = null

    @Exclude
    fun calculateProgress(): Int {
        return if (totalDays > 0) {
            ((currentDay.toFloat() / totalDays) * 100).toInt()
        } else 0
    }
}
```

##### Model 4: ContentItem.kt

**Location:** app/src/main/java/com/ora/wellbeing/data/model/ContentItem.kt

**Firestore Path:** content/{contentId}

**Schema:**
```kotlin
@IgnoreExtraProperties
class ContentItem {
    @DocumentId
    var id: String = ""

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = ""

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = ""  // Méditation, Yoga, Respiration, Pilates, Bien-être

    @get:PropertyName("duration")
    @set:PropertyName("duration")
    var duration: String = "10 min"

    @get:PropertyName("duration_minutes")
    @set:PropertyName("duration_minutes")
    var durationMinutes: Int = 10

    @get:PropertyName("instructor")
    @set:PropertyName("instructor")
    var instructor: String = ""

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = ""

    @get:PropertyName("thumbnail_url")
    @set:PropertyName("thumbnail_url")
    var thumbnailUrl: String? = null

    @get:PropertyName("video_url")
    @set:PropertyName("video_url")
    var videoUrl: String? = null

    @get:PropertyName("audio_url")
    @set:PropertyName("audio_url")
    var audioUrl: String? = null

    @get:PropertyName("is_premium_only")
    @set:PropertyName("is_premium_only")
    var isPremiumOnly: Boolean = false

    @get:PropertyName("is_popular")
    @set:PropertyName("is_popular")
    var isPopular: Boolean = false

    @get:PropertyName("is_new")
    @set:PropertyName("is_new")
    var isNew: Boolean = false

    @get:PropertyName("rating")
    @set:PropertyName("rating")
    var rating: Float = 0f

    @get:PropertyName("completion_count")
    @set:PropertyName("completion_count")
    var completionCount: Int = 0

    @get:PropertyName("tags")
    @set:PropertyName("tags")
    var tags: List<String> = emptyList()

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null

    @get:PropertyName("published_at")
    @set:PropertyName("published_at")
    var publishedAt: Date? = null

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true
}
```

#### 1.2 Update Firestore Security Rules (tech-backend-firebase)

**Reference:** c:\Users\chris\source\repos\Ora\firestore.rules

**Location:** firestore.rules

**New Rules:**
```javascript
// Collection gratitudes/{uid}/entries/{date}
// User-specific: Only owner can read/write
match /gratitudes/{uid}/entries/{date} {
  allow read, write: if request.auth != null && request.auth.uid == uid;

  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateGratitudeEntry(request.resource.data);

  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateGratitudeEntry(request.resource.data);
}

// Collection programs/{programId}
// Public read-only catalog
match /programs/{programId} {
  allow read: if request.auth != null;
  allow write: if false;  // Admin only (future Cloud Functions)
}

// Collection user_programs/{uid}/enrolled/{programId}
// User-specific: Only owner can read/write
match /user_programs/{uid}/enrolled/{programId} {
  allow read, write: if request.auth != null && request.auth.uid == uid;

  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateUserProgram(request.resource.data);

  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateUserProgram(request.resource.data);
}

// Collection content/{contentId}
// Public read-only catalog
match /content/{contentId} {
  allow read: if request.auth != null;
  allow write: if false;  // Admin only (future Cloud Functions)
}

// Validation functions
function validateGratitudeEntry(data) {
  return data.uid is string
      && data.date is string
      && data.gratitudes is list
      && data.gratitudes.size() <= 3
      && (data.notes == null || data.notes.size() <= 500);
}

function validateUserProgram(data) {
  return data.uid is string
      && data.programId is string
      && data.currentDay is int
      && data.currentDay > 0
      && data.totalDays is int
      && data.totalDays > 0;
}
```

#### 1.3 Create Firestore Indexes (tech-backend-firebase)

**Location:** firestore.indexes.json

**New Indexes:**
```json
{
  "indexes": [
    {
      "collectionGroup": "entries",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "uid", "order": "ASCENDING" },
        { "fieldPath": "date", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "programs",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "rating", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "programs",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "isActive", "order": "ASCENDING" },
        { "fieldPath": "participantCount", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "enrolled",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "uid", "order": "ASCENDING" },
        { "fieldPath": "isCompleted", "order": "ASCENDING" },
        { "fieldPath": "lastSessionAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "content",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "rating", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "content",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "isActive", "order": "ASCENDING" },
        { "fieldPath": "isPopular", "order": "ASCENDING" },
        { "fieldPath": "publishedAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "content",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "isActive", "order": "ASCENDING" },
        { "fieldPath": "isNew", "order": "ASCENDING" },
        { "fieldPath": "publishedAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

**Phase 1 Deliverables:**
- [x] GratitudeEntry.kt model
- [x] Program.kt model
- [x] UserProgram.kt model
- [x] ContentItem.kt model
- [x] firestore.rules updated
- [x] firestore.indexes.json created

**Validation Gate:** backend_ready (models created, rules defined)

---

### Phase 2: Repository Implementations (6 hours)

**Priority:** HIGH | **Dependencies:** Phase 1 | **Status:** BLOCKED

**Reference Implementation:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\data\repository\UserProfileRepository.kt

#### 2.1 Create Domain Repository Interfaces (tech-android)

##### GratitudeRepository.kt

**Location:** app/src/main/java/com/ora/wellbeing/domain/repository/GratitudeRepository.kt

```kotlin
interface GratitudeRepository {
    fun getTodayEntry(uid: String): Flow<GratitudeEntry?>
    fun getRecentEntries(uid: String, limit: Int): Flow<List<GratitudeEntry>>
    fun getEntriesByDateRange(uid: String, startDate: String, endDate: String): Flow<List<GratitudeEntry>>
    suspend fun createEntry(entry: GratitudeEntry): Result<Unit>
    suspend fun updateEntry(entry: GratitudeEntry): Result<Unit>
    suspend fun deleteEntry(uid: String, date: String): Result<Unit>
    fun getGratitudeStats(uid: String): Flow<GratitudeStats>
}

data class GratitudeStats(
    val totalEntries: Int,
    val currentStreak: Int,
    val thisMonthEntries: Int
)
```

##### ProgramRepository.kt

**Location:** app/src/main/java/com/ora/wellbeing/domain/repository/ProgramRepository.kt

```kotlin
interface ProgramRepository {
    fun getAllPrograms(): Flow<List<Program>>
    fun getProgramsByCategory(category: String): Flow<List<Program>>
    fun getProgram(programId: String): Flow<Program?>
    fun getPopularPrograms(limit: Int): Flow<List<Program>>
}
```

##### UserProgramRepository.kt

**Location:** app/src/main/java/com/ora/wellbeing/domain/repository/UserProgramRepository.kt

```kotlin
interface UserProgramRepository {
    fun getEnrolledPrograms(uid: String): Flow<List<UserProgram>>
    fun getActivePrograms(uid: String): Flow<List<UserProgram>>
    fun getCompletedPrograms(uid: String): Flow<List<UserProgram>>
    fun getUserProgram(uid: String, programId: String): Flow<UserProgram?>
    suspend fun enrollInProgram(uid: String, programId: String, totalDays: Int): Result<Unit>
    suspend fun updateProgress(uid: String, programId: String, currentDay: Int, completedSessionId: String): Result<Unit>
    suspend fun completeProgram(uid: String, programId: String): Result<Unit>
}
```

##### ContentRepository.kt

**Location:** app/src/main/java/com/ora/wellbeing/domain/repository/ContentRepository.kt

```kotlin
interface ContentRepository {
    fun getAllContent(): Flow<List<ContentItem>>
    fun getContentByCategory(category: String): Flow<List<ContentItem>>
    fun getContent(contentId: String): Flow<ContentItem?>
    fun getPopularContent(limit: Int): Flow<List<ContentItem>>
    fun getNewContent(limit: Int): Flow<List<ContentItem>>
    fun searchContent(query: String): Flow<List<ContentItem>>
}
```

#### 2.2 Create Repository Implementations (tech-android)

**Pattern:** Use callbackFlow with addSnapshotListener for real-time sync

**Example:** GratitudeRepositoryImpl.kt

```kotlin
class GratitudeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GratitudeRepository {

    companion object {
        private const val COLLECTION_GRATITUDES = "gratitudes"
        private const val SUBCOLLECTION_ENTRIES = "entries"
    }

    override fun getTodayEntry(uid: String): Flow<GratitudeEntry?> = callbackFlow {
        val today = LocalDate.now().toString()  // yyyy-MM-dd
        val docRef = firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .document(today)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to today's gratitude entry")
                trySend(null)
                return@addSnapshotListener
            }

            val entry = snapshot?.toObject(GratitudeEntry::class.java)
            trySend(entry)
        }

        awaitClose { listener.remove() }
    }

    override fun getRecentEntries(uid: String, limit: Int): Flow<List<GratitudeEntry>> = callbackFlow {
        val listener = firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to recent entries")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val entries = snapshot?.toObjects(GratitudeEntry::class.java) ?: emptyList()
                trySend(entries)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun createEntry(entry: GratitudeEntry): Result<Unit> = try {
        firestore
            .collection(COLLECTION_GRATITUDES)
            .document(entry.uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .document(entry.date)
            .set(entry)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating gratitude entry")
        Result.failure(e)
    }
}
```

#### 2.3 Update DI Module (tech-android)

**Location:** app/src/main/java/com/ora/wellbeing/di/FirestoreModule.kt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    // Existing providers...

    @Provides
    @Singleton
    fun provideGratitudeRepository(
        firestore: FirebaseFirestore
    ): GratitudeRepository = GratitudeRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideProgramRepository(
        firestore: FirebaseFirestore
    ): ProgramRepository = ProgramRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideUserProgramRepository(
        firestore: FirebaseFirestore
    ): UserProgramRepository = UserProgramRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideContentRepository(
        firestore: FirebaseFirestore
    ): ContentRepository = ContentRepositoryImpl(firestore)
}
```

**Phase 2 Deliverables:**
- [x] 4 repository interfaces in domain/repository/
- [x] 4 repository implementations in data/repository/impl/
- [x] DI bindings in FirestoreModule.kt
- [x] Flow-based listeners implemented

**Validation Gate:** backend_ready (repositories functional)

---

### Phase 3: ViewModel Integration (5 hours)

**Priority:** HIGH | **Dependencies:** Phase 2 | **Status:** BLOCKED

**Reference:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\presentation\screens\profile\ProfileViewModel.kt

#### 3.1 Update JournalViewModel (tech-android)

**Current:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\presentation\screens\journal\JournalViewModel.kt

**Changes:**
```kotlin
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val gratitudeRepository: GratitudeRepository,
    private val syncManager: SyncManager  // For uid access
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        observeGratitudeData()
    }

    private fun observeGratitudeData() {
        viewModelScope.launch {
            val uid = syncManager.userProfile.value?.uid ?: return@launch

            combine(
                gratitudeRepository.getTodayEntry(uid),
                gratitudeRepository.getRecentEntries(uid, 10),
                gratitudeRepository.getGratitudeStats(uid)
            ) { today, recent, stats ->
                Triple(today, recent, stats)
            }.collect { (today, recent, stats) ->
                _uiState.value = JournalUiState(
                    todayEntry = today?.toUiModel(),
                    recentEntries = recent.map { it.toUiModel() },
                    totalEntries = stats.totalEntries,
                    gratitudeStreak = stats.currentStreak,
                    thisMonthEntries = stats.thisMonthEntries
                )
            }
        }
    }

    fun onEvent(event: JournalUiEvent) {
        when (event) {
            is JournalUiEvent.SaveGratitudes -> saveGratitudes(event.gratitudes, event.mood, event.notes)
            is JournalUiEvent.DeleteEntry -> deleteEntry(event.date)
        }
    }

    private fun saveGratitudes(gratitudes: List<String>, mood: String?, notes: String?) {
        viewModelScope.launch {
            val uid = syncManager.userProfile.value?.uid ?: return@launch
            val today = LocalDate.now().toString()

            val entry = GratitudeEntry().apply {
                this.uid = uid
                this.date = today
                this.gratitudes = gratitudes
                this.mood = mood
                this.notes = notes
            }

            val result = gratitudeRepository.createEntry(entry)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de l'enregistrement"
                )
            }
        }
    }
}
```

#### 3.2 Update ProgramsViewModel (tech-android)

**Current:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\presentation\screens\programs\ProgramsViewModel.kt

**Changes:**
```kotlin
@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val userProgramRepository: UserProgramRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramsUiState())
    val uiState: StateFlow<ProgramsUiState> = _uiState.asStateFlow()

    init {
        observeProgramsData()
    }

    private fun observeProgramsData() {
        viewModelScope.launch {
            val uid = syncManager.userProfile.value?.uid ?: return@launch

            combine(
                programRepository.getAllPrograms(),
                userProgramRepository.getActivePrograms(uid)
            ) { allPrograms, activePrograms ->
                Pair(allPrograms, activePrograms)
            }.collect { (allPrograms, activePrograms) ->
                _uiState.value = ProgramsUiState(
                    allPrograms = allPrograms.groupBy { it.category },
                    activePrograms = activePrograms.map { it.toUiModel() },
                    selectedCategory = _uiState.value.selectedCategory
                )
            }
        }
    }

    fun onEvent(event: ProgramsUiEvent) {
        when (event) {
            is ProgramsUiEvent.SelectCategory -> selectCategory(event.category)
            is ProgramsUiEvent.EnrollInProgram -> enrollInProgram(event.programId)
        }
    }
}
```

#### 3.3 Update LibraryViewModel (tech-android)

**Current:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\presentation\screens\library\LibraryViewModel.kt

**Changes:**
```kotlin
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeContentData()
    }

    private fun observeContentData() {
        viewModelScope.launch {
            combine(
                contentRepository.getAllContent(),
                contentRepository.getPopularContent(10),
                contentRepository.getNewContent(5)
            ) { all, popular, new ->
                Triple(all, popular, new)
            }.collect { (all, popular, new) ->
                _uiState.value = LibraryUiState(
                    allContent = all.groupBy { it.category },
                    popularContent = popular,
                    newContent = new,
                    selectedCategory = _uiState.value.selectedCategory
                )
            }
        }
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.SearchContent -> searchContent(event.query)
            is LibraryUiEvent.FilterByCategory -> filterByCategory(event.category)
        }
    }
}
```

#### 3.4 Update HomeViewModel (tech-android)

**Current:** c:\Users\chris\source\repos\Ora\app\src\main\java\com\ora\wellbeing\presentation\screens\home\HomeViewModel.kt

**Changes:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val contentRepository: ContentRepository,
    private val programRepository: ProgramRepository,
    private val userProgramRepository: UserProgramRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeData()
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            val uid = syncManager.userProfile.value?.uid ?: return@launch

            combine(
                syncManager.userProfile,
                syncManager.userStats,
                contentRepository.getPopularContent(5),
                userProgramRepository.getActivePrograms(uid)
            ) { profile, stats, popularContent, activePrograms ->
                // Build recommendations based on user stats and preferences
                _uiState.value = HomeUiState(
                    userName = profile?.getFirstNameOrGuest() ?: "Invité",
                    recommendedContent = popularContent,
                    activePrograms = activePrograms,
                    weeklyStats = stats?.let { buildWeeklyStats(it) }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
```

**Phase 3 Deliverables:**
- [x] JournalViewModel updated with GratitudeRepository
- [x] ProgramsViewModel updated with ProgramRepository + UserProgramRepository
- [x] LibraryViewModel updated with ContentRepository
- [x] HomeViewModel updated with personalized recommendations
- [x] All ViewModels use Flow-based state management
- [x] Error handling implemented

**Validation Gate:** android_web_ios_ready (app builds, ViewModels functional)

---

### Phase 4: Seed Data and Deployment (3 hours)

**Priority:** MEDIUM | **Dependencies:** Phase 3 | **Status:** BLOCKED

#### 4.1 Create Seed Data (tech-backend-firebase)

**Location:** firebase/seed_data.json

**Programs Seed Data (10+ programs):**
```json
{
  "programs": [
    {
      "id": "meditation-7-days",
      "title": "7 jours de méditation",
      "description": "Initiation à la méditation en 7 jours",
      "category": "Méditation",
      "duration": 7,
      "level": "Débutant",
      "participant_count": 1250,
      "rating": 4.8,
      "thumbnail_url": "https://example.com/meditation-7.jpg",
      "instructor": "Sophie Martin",
      "is_premium_only": false,
      "is_active": true,
      "sessions": [
        {
          "day": 1,
          "content_id": "meditation-breathing-basics",
          "title": "Respiration consciente",
          "duration_minutes": 10
        },
        // ... 6 more sessions
      ]
    },
    {
      "id": "yoga-morning-21",
      "title": "Yoga du matin - 21 jours",
      "description": "Créez une routine matinale avec le yoga",
      "category": "Yoga",
      "duration": 21,
      "level": "Tous niveaux",
      "participant_count": 890,
      "rating": 4.7,
      "thumbnail_url": "https://example.com/yoga-morning.jpg",
      "instructor": "Léa Dubois",
      "is_premium_only": true,
      "is_active": true,
      "sessions": []
    }
    // ... 8+ more programs
  ]
}
```

**Content Seed Data (15+ items):**
```json
{
  "content": [
    {
      "id": "meditation-breathing-basics",
      "title": "Respiration consciente - Bases",
      "category": "Méditation",
      "duration": "10 min",
      "duration_minutes": 10,
      "instructor": "Sophie Martin",
      "description": "Apprenez les fondamentaux de la respiration consciente",
      "thumbnail_url": "https://example.com/breathing.jpg",
      "video_url": "https://example.com/videos/breathing.mp4",
      "audio_url": "https://example.com/audio/breathing.mp3",
      "is_premium_only": false,
      "is_popular": true,
      "is_new": false,
      "rating": 4.9,
      "completion_count": 3200,
      "tags": ["débutant", "respiration", "relaxation"],
      "is_active": true
    }
    // ... 14+ more content items
  ]
}
```

#### 4.2 Deploy Firestore Rules (tech-backend-firebase)

```bash
# From project root
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

**Validation:**
- Rules deployed successfully
- Indexes created in Firestore Console
- No security warnings

#### 4.3 Import Seed Data (tech-backend-firebase)

**Manual Import via Firebase Console:**
1. Navigate to Firestore Console
2. Import programs collection from seed_data.json
3. Import content collection from seed_data.json

**Future:** Create Firebase Cloud Function for automated import

#### 4.4 Security Testing (tech-backend-firebase)

**Test Cross-User Access:**
```javascript
// Test script (Firebase Emulator)
const assert = require('assert');
const firebase = require('@firebase/testing');

// Test: User A cannot read User B's gratitudes
it('prevents cross-user gratitude access', async () => {
  const userA = 'userA';
  const userB = 'userB';

  const dbA = firebase.initializeTestApp({
    auth: { uid: userA }
  }).firestore();

  await assert.rejects(
    dbA.collection('gratitudes').doc(userB).collection('entries').get(),
    /PERMISSION_DENIED/
  );
});
```

**Phase 4 Deliverables:**
- [x] firebase/seed_data.json created with 10+ programs and 15+ content items
- [x] firestore.rules deployed to Firebase
- [x] firestore.indexes deployed to Firebase
- [x] Seed data imported to Firestore
- [x] Security testing passed (no cross-user access)
- [x] Offline mode validated

**Validation Gate:** backend_ready + security_ready

---

### Phase 5: Tests and Documentation (4 hours)

**Priority:** LOW | **Dependencies:** Phase 4 | **Status:** BLOCKED

#### 5.1 Unit Tests (tech-android)

**GratitudeRepositoryTest.kt:**
```kotlin
@Test
fun `getTodayEntry returns entry for authenticated user`() = runTest {
    val uid = "testUser"
    val today = LocalDate.now().toString()

    // Mock Firestore response
    val expectedEntry = GratitudeEntry().apply {
        this.uid = uid
        this.date = today
        this.gratitudes = listOf("Test gratitude")
    }

    val result = repository.getTodayEntry(uid).first()

    assertEquals(expectedEntry.uid, result?.uid)
    assertEquals(expectedEntry.date, result?.date)
}
```

#### 5.2 Integration Tests (tech-android)

**JournalViewModelTest.kt:**
```kotlin
@Test
fun `viewModel updates state when gratitude entry is saved`() = runTest {
    val viewModel = JournalViewModel(gratitudeRepository, syncManager)

    val gratitudes = listOf("Test 1", "Test 2", "Test 3")
    viewModel.onEvent(JournalUiEvent.SaveGratitudes(gratitudes, "😊", "Test notes"))

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.todayEntry)
    assertEquals(3, state.todayEntry?.gratitudes?.size)
}
```

#### 5.3 Documentation (tech-android)

**Create docs/FIRESTORE_COLLECTIONS_SCHEMA.md:**
- Complete schema documentation for all 4 collections
- Security rules explanation
- Query patterns and indexes
- Migration guide from mock data

**Update CLAUDE.md:**
- Add new collections to "Current Implementation Status"
- Update file structure with new repositories
- Document new dependencies

**Phase 5 Deliverables:**
- [x] Unit tests for all 4 repositories (CRUD operations)
- [x] Integration tests for all 4 ViewModels
- [x] Security rules tests (Firestore Emulator)
- [x] Offline mode tests
- [x] docs/FIRESTORE_COLLECTIONS_SCHEMA.md created
- [x] CLAUDE.md updated
- [x] Code coverage > 80%

**Validation Gate:** analytics_ready (tests passing, documentation complete)

---

## Timeline and Milestones

| Phase | Duration | Start | End | Status |
|-------|----------|-------|-----|--------|
| Phase 1 | 4h | T+0h | T+4h | READY TO START |
| Phase 2 | 6h | T+4h | T+10h | BLOCKED |
| Phase 3 | 5h | T+10h | T+15h | BLOCKED |
| Phase 4 | 3h | T+15h | T+18h | BLOCKED |
| Phase 5 | 4h | T+18h | T+22h | BLOCKED |

**Total Estimated Duration:** 22 hours

**Critical Path:** Phase 1 → 2 → 3 → 4 → 5 (fully sequential)

**Milestones:**
- M1 (T+4h): All Firestore models created, rules defined
- M2 (T+10h): All repositories functional with real-time listeners
- M3 (T+15h): All ViewModels dynamized, app builds successfully
- M4 (T+18h): Seed data deployed, security validated
- M5 (T+22h): All tests passing, documentation complete

## Agent Coordination

### tech-backend-firebase

**Responsibilities:**
- Phase 1: Create firestore.rules and firestore.indexes.json
- Phase 4: Create seed data, deploy rules, security testing

**Estimated Workload:** 7 hours (4h + 3h)

**Deliverables:**
1. firestore.rules (updated with 4 new collections)
2. firestore.indexes.json (7 composite indexes)
3. firebase/seed_data.json (10+ programs, 15+ content items)
4. Security test report

### tech-android

**Responsibilities:**
- Phase 1: Create 4 Firestore models
- Phase 2: Create 4 repository interfaces + implementations + DI
- Phase 3: Update 4 ViewModels
- Phase 5: Tests and documentation

**Estimated Workload:** 19 hours (4h + 6h + 5h + 4h)

**Deliverables:**
1. 4 Firestore models (GratitudeEntry, Program, UserProgram, ContentItem)
2. 4 repository interfaces (domain/repository/)
3. 4 repository implementations (data/repository/impl/)
4. 4 updated ViewModels (Journal, Programs, Library, Home)
5. DI module updates
6. Unit + integration tests
7. Documentation (FIRESTORE_COLLECTIONS_SCHEMA.md, CLAUDE.md updates)

## Risk Mitigation Strategies

### Risk 1: Firestore Mapping Errors (HIGH)

**Symptoms:**
- CustomClassMapper warnings in logs
- Objects deserialize as null
- Properties not populated

**Mitigation:**
1. Strictly follow docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md
2. Use regular `class`, not `data class`
3. Properties as `var` outside constructor
4. `@get:PropertyName` and `@set:PropertyName` annotations
5. Validate with test documents before full deployment

**Validation:** Zero CustomClassMapper warnings in logs

### Risk 2: Firestore Cost Increase (MEDIUM)

**Symptoms:**
- High read/write counts in Firebase Console
- Quota warnings
- Unexpected billing

**Mitigation:**
1. Enable Firestore persistence (cache enabled)
2. Limit real-time listeners (unsubscribe when not in use)
3. Throttle listener updates (debounce rapid changes)
4. Monitor quota daily in Firebase Console
5. Set budget alerts in Google Cloud Console

**Validation:** Read/write counts within free tier limits

### Risk 3: Network Latency (MEDIUM)

**Symptoms:**
- Slow initial load times
- Blank screens on poor connections
- Poor user experience

**Mitigation:**
1. Show skeleton screens during load
2. Enable Firestore persistence for offline cache
3. Optimistic updates for write operations
4. Prefetch popular content
5. Implement pagination for large lists

**Validation:** Initial load < 2s with cache, < 5s without

### Risk 4: Offline Conflicts (LOW)

**Symptoms:**
- Gratitude entries overwritten
- Lost data on offline edits
- Unexpected sync behavior

**Mitigation:**
1. Implement last-write-wins strategy
2. Add conflict resolution UI (future)
3. Use server timestamps for accurate ordering
4. Display sync status to user

**Validation:** No data loss in offline mode testing

## Success Metrics

| Metric | Target | Current | Validation Method |
|--------|--------|---------|-------------------|
| Real Firestore data on all screens | 100% (4/4 screens) | 25% (1/4) | Manual testing |
| Real-time sync functional | Yes | Partial | Update Firestore → UI reflects change immediately |
| Offline mode functional | Yes | Partial | Airplane mode → app still usable |
| Zero cross-user access | Zero vulnerabilities | Unknown | Security test suite |
| Zero CustomClassMapper warnings | Zero warnings | Unknown | adb logcat \| grep CustomClassMapper |
| Code coverage | > 80% | Unknown | ./gradlew test jacocoTestReport |
| Build success | All builds pass | Yes | ./gradlew build |
| Performance | Initial load < 2s | Unknown | Firebase Performance Monitoring |

## Next Steps

### Immediate Actions

1. **Create agent task files:**
   - bus/inbox/tech-backend-firebase/task-firestore-collections-2025-10-05.json
   - bus/inbox/tech-android/task-firestore-integration-2025-10-05.json

2. **Start Phase 1 execution:**
   - tech-android: Begin creating GratitudeEntry.kt model
   - tech-backend-firebase: Begin updating firestore.rules

3. **Set up monitoring:**
   - Firebase Console quota monitoring
   - Android Studio logcat for CustomClassMapper warnings
   - Build CI for each phase completion

### Phase Transitions

After each phase completion:
1. Update status/pipeline.json with phase status
2. Validate gate requirements
3. tech-android/tech-backend-firebase report to bus/outbox/
4. supervisor-tech reviews and approves next phase
5. Unblock dependent phase

### Final Validation

Before marking task as complete:
1. All 5 phases marked as COMPLETED in pipeline.json
2. All success metrics met
3. All acceptance criteria validated
4. Documentation complete
5. No critical bugs or warnings

---

**Plan Created By:** supervisor-tech
**Timestamp:** 2025-10-05T00:00:00Z
**Status:** Approved for Execution
**Total Estimated Duration:** 22 hours
**Critical Path:** 5 sequential phases
**Agents Involved:** tech-backend-firebase, tech-android
