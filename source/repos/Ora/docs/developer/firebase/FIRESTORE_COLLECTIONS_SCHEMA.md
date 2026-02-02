# Firestore Collections Schema - Ora Wellbeing App

## Overview

This document describes the Firestore collections schema for the Ora wellbeing application, including collection paths, field definitions, security rules, and indexing strategy.

**Last Updated:** 2025-10-05
**Collections:** 6 (users, stats, gratitudes, programs, user_programs, content)

---

## Collections

### 1. users/{uid}

**Purpose:** Store user profile data with UID-based isolation.

**Security:** Each user can only read/write their own document.

**Path Pattern:** `/users/{uid}`

**Fields:**

| Field Name | Firestore (snake_case) | Kotlin Type | Required | Validation |
|------------|------------------------|-------------|----------|------------|
| uid | `uid` | String | ✅ | Non-empty string |
| firstName | `first_name` | String? | ❌ | Max 50 chars |
| lastName | `last_name` | String? | ❌ | Max 50 chars |
| email | `email` | String? | ❌ | - |
| photoUrl | `photo_url` | String? | ❌ | Valid URL |
| motto | `motto` | String? | ❌ | Max 200 chars |
| planTier | `plan_tier` | String | ✅ | 'free' or 'premium' |
| locale | `locale` | String? | ❌ | 'fr' or 'en' |
| createdAt | `created_at` | Timestamp | ✅ | Auto-generated |
| updatedAt | `updated_at` | Timestamp | ❌ | Auto-generated |
| lastSyncAt | `last_sync_at` | Timestamp? | ❌ | - |

**Security Rules:**
```javascript
match /users/{uid} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateUserProfile(request.resource.data);
  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateUserProfile(request.resource.data);
}
```

**Validation Function:**
```javascript
function validateUserProfile(data) {
  return data.uid is string
      && data.uid.size() > 0
      && (data.firstName == null || (data.firstName is string && data.firstName.size() <= 50))
      && (data.photoUrl == null || data.photoUrl is string)
      && data.planTier in ['free', 'premium']
      && data.createdAt is int
      && (data.locale == null || data.locale in ['fr', 'en'])
      && (data.lastSyncAt == null || data.lastSyncAt is int);
}
```

---

### 2. stats/{uid}

**Purpose:** Store user statistics and activity metrics.

**Security:** Each user can only read/write their own stats.

**Path Pattern:** `/stats/{uid}`

**Fields:**

| Field Name | Firestore (snake_case) | Kotlin Type | Required | Validation |
|------------|------------------------|-------------|----------|------------|
| uid | `uid` | String | ✅ | Non-empty string |
| totalMinutes | `total_minutes` | Int | ✅ | 0 to 525600 (1 year) |
| sessions | `sessions` | Int | ✅ | >= 0 |
| streakDays | `streak_days` | Int | ✅ | 0 to 3650 (10 years) |
| lastPracticeAt | `last_practice_at` | Timestamp? | ❌ | - |
| updatedAt | `updated_at` | Timestamp | ✅ | Auto-generated |

**Security Rules:**
```javascript
match /stats/{uid} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateUserStats(request.resource.data);
  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateUserStats(request.resource.data);
}
```

**Validation Function:**
```javascript
function validateUserStats(data) {
  return data.uid is string
      && data.uid.size() > 0
      && data.totalMinutes is int
      && data.totalMinutes >= 0
      && data.totalMinutes <= 525600
      && data.sessions is int
      && data.sessions >= 0
      && data.streakDays is int
      && data.streakDays >= 0
      && data.streakDays <= 3650
      && (data.lastPracticeAt == null || data.lastPracticeAt is int)
      && data.updatedAt is int;
}
```

**Indexes:**
- `lastPracticeAt DESC` (single field index)

---

### 3. gratitudes/{uid}/entries/{date}

**Purpose:** Store daily gratitude journal entries (subcollection per user).

**Security:** UID-based isolation - each user can only access their own gratitude entries.

**Path Pattern:** `/gratitudes/{uid}/entries/{date}`
**Document ID:** Date string in format `YYYY-MM-DD` (e.g., `2025-10-05`)

**Fields:**

| Field Name | Firestore (snake_case) | Kotlin Type | Required | Validation |
|------------|------------------------|-------------|----------|------------|
| uid | `uid` | String | ✅ | Non-empty string |
| date | `date` | String | ✅ | Format YYYY-MM-DD |
| gratitudes | `gratitudes` | List<String> | ✅ | 1-3 items |
| notes | `notes` | String? | ❌ | Max 500 chars |
| createdAt | `created_at` | Timestamp | ✅ | Auto-generated |
| updatedAt | `updated_at` | Timestamp? | ❌ | Auto-generated |

**Security Rules:**
```javascript
match /gratitudes/{uid}/entries/{date} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateGratitudeEntry(request.resource.data);
  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateGratitudeEntry(request.resource.data);
}
```

**Validation Function:**
```javascript
function validateGratitudeEntry(data) {
  return data.uid is string
      && data.uid.size() > 0
      && data.date is string
      && data.date.matches('^[0-9]{4}-[0-9]{2}-[0-9]{2}$')
      && data.gratitudes is list
      && data.gratitudes.size() >= 1
      && data.gratitudes.size() <= 3
      && (data.notes == null || (data.notes is string && data.notes.size() <= 500))
      && data.created_at is int
      && (data.updated_at == null || data.updated_at is int);
}
```

**Indexes:**
- Collection Group Query: `entries` with `uid ASC, created_at DESC`

**Example Document:**
```json
{
  "uid": "OCqFs1Q7zYV9uNUVYbPAnzoQV0N2",
  "date": "2025-10-05",
  "gratitudes": [
    "Ma santé",
    "Le soutien de mes proches",
    "Un moment de calme ce matin"
  ],
  "notes": "Journée apaisante avec une belle séance de méditation",
  "created_at": 1728086400,
  "updated_at": 1728086400
}
```

---

### 4. programs/{programId}

**Purpose:** Store program catalog (meditation programs, yoga challenges, etc.).

**Security:** Read-only for authenticated users, write admin only.

**Path Pattern:** `/programs/{programId}`

**Fields:**

| Field Name | Firestore (snake_case) | Kotlin Type | Required | Description |
|------------|------------------------|-------------|----------|-------------|
| id | `id` | String | ✅ | Unique program identifier |
| title | `title` | String | ✅ | Program title (French) |
| description | `description` | String | ✅ | Detailed description |
| category | `category` | String | ✅ | Méditation, Yoga, Bien-être, Défis, Sommeil, Pilates |
| duration | `duration` | Int | ✅ | Number of days |
| level | `level` | String | ✅ | Débutant, Intermédiaire, Avancé, Tous niveaux |
| participantCount | `participant_count` | Int | ✅ | Number of enrollments |
| rating | `rating` | Float | ✅ | 0.0 to 5.0 |
| thumbnailUrl | `thumbnail_url` | String | ✅ | Image URL |
| instructor | `instructor` | String | ✅ | Instructor name |
| isPremiumOnly | `is_premium_only` | Boolean | ✅ | Premium access required |
| isActive | `is_active` | Boolean | ✅ | Visible in catalog |
| sessions | `sessions` | Array | ✅ | Array of session objects |

**Session Object Structure:**
```json
{
  "day": 1,
  "content_id": "med-respiration-consciente",
  "title": "Respiration consciente",
  "duration_minutes": 10
}
```

**Security Rules:**
```javascript
match /programs/{programId} {
  allow read: if request.auth != null;
  allow write: if false; // Admin only (future Cloud Functions)
}
```

**Indexes:**
- `category ASC, rating DESC` (composite)
- `is_active ASC, participant_count DESC` (composite)

**Seed Data:** See `firebase/seed-data/programs.json` (10 programs)

---

### 5. user_programs/{uid}/enrolled/{programId}

**Purpose:** Track user enrollment and progress in programs (subcollection per user).

**Security:** UID-based isolation - each user can only access their own enrolled programs.

**Path Pattern:** `/user_programs/{uid}/enrolled/{programId}`

**Fields:**

| Field Name | Firestore (snake_case) | Kotlin Type | Required | Validation |
|------------|------------------------|-------------|----------|------------|
| uid | `uid` | String | ✅ | Non-empty string |
| programId | `program_id` | String | ✅ | References programs/{programId} |
| currentDay | `current_day` | Int | ✅ | >= 0, <= totalDays |
| totalDays | `total_days` | Int | ✅ | > 0 |
| isCompleted | `is_completed` | Boolean | ✅ | - |
| enrolledAt | `enrolled_at` | Timestamp | ✅ | Auto-generated |
| lastSessionAt | `last_session_at` | Timestamp? | ❌ | - |
| completedAt | `completed_at` | Timestamp? | ❌ | - |

**Security Rules:**
```javascript
match /user_programs/{uid}/enrolled/{programId} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateUserProgram(request.resource.data);
  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateUserProgram(request.resource.data);
}
```

**Validation Function:**
```javascript
function validateUserProgram(data) {
  return data.uid is string
      && data.uid.size() > 0
      && data.program_id is string
      && data.program_id.size() > 0
      && data.current_day is int
      && data.current_day >= 0
      && data.total_days is int
      && data.total_days > 0
      && data.current_day <= data.total_days
      && data.is_completed is bool
      && data.enrolled_at is int
      && (data.last_session_at == null || data.last_session_at is int)
      && (data.completed_at == null || data.completed_at is int);
}
```

**Indexes:**
- Collection Group Query: `enrolled` with `uid ASC, is_completed ASC, last_session_at DESC`

**Example Document:**
```json
{
  "uid": "OCqFs1Q7zYV9uNUVYbPAnzoQV0N2",
  "program_id": "meditation-debutant-7j",
  "current_day": 3,
  "total_days": 7,
  "is_completed": false,
  "enrolled_at": 1728000000,
  "last_session_at": 1728086400,
  "completed_at": null
}
```

---

### 6. content/{contentId}

**Purpose:** Store content catalog (meditation sessions, yoga videos, breathing exercises).

**Security:** Read-only for authenticated users, write admin only.

**Path Pattern:** `/content/{contentId}`

**Fields:**

| Field Name | Firestore (snake_case) | Kotlin Type | Required | Description |
|------------|------------------------|-------------|----------|-------------|
| id | `id` | String | ✅ | Unique content identifier |
| title | `title` | String | ✅ | Content title (French) |
| category | `category` | String | ✅ | Méditation, Yoga, Respiration, Pilates, Bien-être |
| duration | `duration` | String | ✅ | Human-readable (e.g., "10 min") |
| durationMinutes | `duration_minutes` | Int | ✅ | Numeric duration for sorting |
| instructor | `instructor` | String | ✅ | Instructor name |
| description | `description` | String | ✅ | Detailed description |
| thumbnailUrl | `thumbnail_url` | String | ✅ | Thumbnail image URL |
| videoUrl | `video_url` | String? | ❌ | Video content URL |
| audioUrl | `audio_url` | String? | ❌ | Audio content URL |
| isPremiumOnly | `is_premium_only` | Boolean | ✅ | Premium access required |
| isPopular | `is_popular` | Boolean | ✅ | Featured as popular |
| isNew | `is_new` | Boolean | ✅ | Featured as new |
| rating | `rating` | Float | ✅ | 0.0 to 5.0 |
| completionCount | `completion_count` | Int | ✅ | Number of completions |
| tags | `tags` | Array<String> | ✅ | Searchable tags |
| isActive | `is_active` | Boolean | ✅ | Visible in catalog |
| publishedAt | `published_at` | Timestamp | ✅ | Publication date |

**Security Rules:**
```javascript
match /content/{contentId} {
  allow read: if request.auth != null;
  allow write: if false; // Admin only (future Cloud Functions)
}
```

**Indexes:**
- `category ASC, rating DESC` (composite)
- `is_active ASC, is_popular ASC, published_at DESC` (composite)
- `is_active ASC, is_new ASC, published_at DESC` (composite)

**Seed Data:** See `firebase/seed-data/content.json` (20 content items)

---

## Security Principles

### 1. Privacy by Design
- **User Data Collections:** `users`, `stats`, `gratitudes`, `user_programs` have UID-based isolation
- Each user can ONLY access documents where `{uid}` matches `request.auth.uid`
- No cross-user access possible

### 2. Public Catalog Collections
- **Public Collections:** `programs`, `content` are read-only for all authenticated users
- Write access reserved for admin (future Cloud Functions)

### 3. Field Validation
- All user-writable collections have validation functions
- Validates required fields, data types, and value ranges
- Prevents malformed data from being written

### 4. Default Deny
- All unlisted collections are blocked by default: `allow read, write: if false`

---

## Indexing Strategy

### Composite Indexes

**Purpose:** Optimize common queries with multiple filters/sorts.

**Created Indexes:**

1. **gratitudes/entries:** `uid ASC, created_at DESC` (Collection Group)
   - Query: User's recent gratitude entries

2. **programs:** `category ASC, rating DESC`
   - Query: Top-rated programs by category

3. **programs:** `is_active ASC, participant_count DESC`
   - Query: Most popular active programs

4. **user_programs/enrolled:** `uid ASC, is_completed ASC, last_session_at DESC` (Collection Group)
   - Query: User's active programs sorted by recent activity

5. **content:** `category ASC, rating DESC`
   - Query: Top-rated content by category

6. **content:** `is_active ASC, is_popular ASC, published_at DESC`
   - Query: Popular active content, newest first

7. **content:** `is_active ASC, is_new ASC, published_at DESC`
   - Query: New active content, newest first

### Single-Field Indexes

- `stats.lastPracticeAt DESC`: For recent activity queries

---

## Deployment Instructions

### 1. Deploy Security Rules

```bash
firebase deploy --only firestore:rules
```

**Expected Output:**
```
✔  Deploy complete!
✔  firestore: rules updated successfully
```

### 2. Deploy Indexes

```bash
firebase deploy --only firestore:indexes
```

**Expected Output:**
```
✔  Deploy complete!
✔  firestore: indexes created (building in background)
```

**Note:** Composite indexes can take several minutes to build. Check status in Firebase Console.

### 3. Import Seed Data

**Manual Import via Firebase Console:**

1. Open Firebase Console → Firestore Database
2. Create collection `programs`
3. For each program in `firebase/seed-data/programs.json`:
   - Click "Add document"
   - Document ID: Use `program.id` value
   - Copy fields from JSON (respect snake_case field names)
4. Create collection `content`
5. For each content item in `firebase/seed-data/content.json`:
   - Click "Add document"
   - Document ID: Use `content.id` value
   - Copy fields from JSON

**Field Type Mapping:**
- `string` → String
- `number` (integer) → Number
- `number` (float) → Number
- `boolean` → Boolean
- `array` → Array
- `timestamp` (unix) → Number (will convert to Timestamp)

---

## Testing Security Rules

### Test Scenarios

#### ✅ Test 1: User can read own gratitudes
```kotlin
// Auth as User A (uid = "userA")
db.collection("gratitudes").document("userA")
  .collection("entries").document("2025-10-05")
  .get() // ✅ SUCCESS
```

#### ❌ Test 2: User cannot read other user's gratitudes
```kotlin
// Auth as User A (uid = "userA")
db.collection("gratitudes").document("userB")
  .collection("entries").document("2025-10-05")
  .get() // ❌ PERMISSION_DENIED
```

#### ✅ Test 3: User can read public programs
```kotlin
// Auth as User A
db.collection("programs").document("meditation-debutant-7j")
  .get() // ✅ SUCCESS
```

#### ❌ Test 4: User cannot write to programs
```kotlin
// Auth as User A
db.collection("programs").document("new-program")
  .set(data) // ❌ PERMISSION_DENIED
```

---

## Kotlin Mapping Examples

### GratitudeEntry Model

```kotlin
@IgnoreExtraProperties
class GratitudeEntry {
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = ""

    @get:PropertyName("date")
    @set:PropertyName("date")
    var date: String = ""

    @get:PropertyName("gratitudes")
    @set:PropertyName("gratitudes")
    var gratitudes: List<String> = emptyList()

    @get:PropertyName("notes")
    @set:PropertyName("notes")
    var notes: String? = null

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    @ServerTimestamp
    var updatedAt: Date? = null
}
```

### Program Model

```kotlin
@IgnoreExtraProperties
class Program {
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = ""

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = ""

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = ""

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = ""

    @get:PropertyName("duration")
    @set:PropertyName("duration")
    var duration: Int = 0

    @get:PropertyName("level")
    @set:PropertyName("level")
    var level: String = ""

    @get:PropertyName("participant_count")
    @set:PropertyName("participant_count")
    var participantCount: Int = 0

    @get:PropertyName("rating")
    @set:PropertyName("rating")
    var rating: Float = 0f

    @get:PropertyName("thumbnail_url")
    @set:PropertyName("thumbnail_url")
    var thumbnailUrl: String = ""

    @get:PropertyName("instructor")
    @set:PropertyName("instructor")
    var instructor: String = ""

    @get:PropertyName("is_premium_only")
    @set:PropertyName("is_premium_only")
    var isPremiumOnly: Boolean = false

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true

    @get:PropertyName("sessions")
    @set:PropertyName("sessions")
    var sessions: List<ProgramSession> = emptyList()
}

@IgnoreExtraProperties
class ProgramSession {
    @get:PropertyName("day")
    @set:PropertyName("day")
    var day: Int = 0

    @get:PropertyName("content_id")
    @set:PropertyName("content_id")
    var contentId: String = ""

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = ""

    @get:PropertyName("duration_minutes")
    @set:PropertyName("duration_minutes")
    var durationMinutes: Int = 0
}
```

---

## References

- **Firebase Documentation:** [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- **Kotlin Mapping Guide:** `docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md`
- **Troubleshooting:** `docs/FIRESTORE_TROUBLESHOOTING.md`
- **User Data Contract:** `contracts/user_data_contract.yaml`

---

## Changelog

### 2025-10-05 - Initial Schema
- Created 4 new collections: `gratitudes`, `programs`, `user_programs`, `content`
- Added 4 validation functions for user-writable collections
- Created 8 composite indexes for optimized queries
- Generated seed data: 10 programs, 20 content items
- Deployed to Firebase production environment
