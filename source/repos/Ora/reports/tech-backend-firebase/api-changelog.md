# Firestore API Changelog - Ora Wellbeing App

## [2025-10-05] - Firestore Collections Infrastructure

### Added Collections

#### 1. gratitudes/{uid}/entries/{date}
- **Purpose:** Daily gratitude journal entries (subcollection per user)
- **Security:** UID-based isolation
- **Fields:**
  - `uid: string` - User ID
  - `date: string` - Date in YYYY-MM-DD format
  - `gratitudes: array` - 1-3 gratitude strings
  - `notes: string?` - Optional notes (max 500 chars)
  - `created_at: timestamp` - Auto-generated
  - `updated_at: timestamp?` - Auto-generated
- **Validation:** 1-3 gratitudes required, date format enforced
- **Index:** `uid ASC, created_at DESC` (Collection Group)

#### 2. programs/{programId}
- **Purpose:** Program catalog (meditation, yoga, challenges)
- **Security:** Read-only for authenticated users, admin write only
- **Fields:**
  - `id: string` - Unique program ID
  - `title: string` - Program title (French)
  - `description: string` - Detailed description
  - `category: string` - Méditation, Yoga, Bien-être, Défis, Sommeil, Pilates
  - `duration: int` - Number of days
  - `level: string` - Débutant, Intermédiaire, Avancé, Tous niveaux
  - `participant_count: int` - Number of enrollments
  - `rating: float` - 0.0 to 5.0
  - `thumbnail_url: string` - Image URL
  - `instructor: string` - Instructor name
  - `is_premium_only: boolean` - Premium access required
  - `is_active: boolean` - Visible in catalog
  - `sessions: array` - Program sessions with content references
- **Indexes:**
  - `category ASC, rating DESC`
  - `is_active ASC, participant_count DESC`
- **Seed Data:** 10 programs created

#### 3. user_programs/{uid}/enrolled/{programId}
- **Purpose:** User enrollment and progress tracking
- **Security:** UID-based isolation
- **Fields:**
  - `uid: string` - User ID
  - `program_id: string` - References programs/{programId}
  - `current_day: int` - Current day (0 to totalDays)
  - `total_days: int` - Total program days
  - `is_completed: boolean` - Completion status
  - `enrolled_at: timestamp` - Enrollment date
  - `last_session_at: timestamp?` - Last activity
  - `completed_at: timestamp?` - Completion date
- **Validation:** currentDay <= totalDays, totalDays > 0
- **Index:** `uid ASC, is_completed ASC, last_session_at DESC` (Collection Group)

#### 4. content/{contentId}
- **Purpose:** Content catalog (meditation sessions, yoga videos)
- **Security:** Read-only for authenticated users, admin write only
- **Fields:**
  - `id: string` - Unique content ID
  - `title: string` - Content title (French)
  - `category: string` - Méditation, Yoga, Respiration, Pilates, Bien-être
  - `duration: string` - Human-readable (e.g., "10 min")
  - `duration_minutes: int` - Numeric duration
  - `instructor: string` - Instructor name
  - `description: string` - Detailed description
  - `thumbnail_url: string` - Thumbnail image
  - `video_url: string?` - Video URL
  - `audio_url: string?` - Audio URL
  - `is_premium_only: boolean` - Premium access required
  - `is_popular: boolean` - Featured as popular
  - `is_new: boolean` - Featured as new
  - `rating: float` - 0.0 to 5.0
  - `completion_count: int` - Number of completions
  - `tags: array<string>` - Searchable tags
  - `is_active: boolean` - Visible in catalog
  - `published_at: timestamp` - Publication date
- **Indexes:**
  - `category ASC, rating DESC`
  - `is_active ASC, is_popular ASC, published_at DESC`
  - `is_active ASC, is_new ASC, published_at DESC`
- **Seed Data:** 20 content items created

### Security Rules

#### New Validation Functions
- `validateGratitudeEntry(data)` - Validates gratitude entries (1-3 items, date format)
- `validateUserProgram(data)` - Validates user program enrollment (day bounds)

#### Security Patterns
- **UID-based Isolation:** `gratitudes`, `user_programs` - Users can only access their own data
- **Public Read-Only:** `programs`, `content` - All authenticated users can read, admin-only write
- **Field Validation:** Required fields, data types, value ranges enforced
- **Default Deny:** All unlisted collections blocked

### Indexes

#### Composite Indexes Created (7 new)
1. `gratitudes/entries` - `uid ASC, created_at DESC` (Collection Group)
2. `programs` - `category ASC, rating DESC`
3. `programs` - `is_active ASC, participant_count DESC`
4. `user_programs/enrolled` - `uid ASC, is_completed ASC, last_session_at DESC` (Collection Group)
5. `content` - `category ASC, rating DESC`
6. `content` - `is_active ASC, is_popular ASC, published_at DESC`
7. `content` - `is_active ASC, is_new ASC, published_at DESC`

### Seed Data

#### Programs (10 items)
- Categories: Méditation (3), Yoga (2), Sommeil (1), Défis (2), Bien-être (1), Pilates (1)
- Levels: Débutant (2), Intermédiaire (2), Avancé (1), Tous niveaux (5)
- Premium: 5 programs, Free: 5 programs
- Each program includes 3+ session references

#### Content (20 items)
- Categories: Méditation (7), Yoga (6), Respiration (3), Pilates (2), Bien-être (2)
- Premium: 8 items, Free: 12 items
- Popular: 14 items, New: 6 items
- Duration range: 5-30 minutes
- Instructors: 6 unique instructors

### Files Modified

1. **firestore.rules**
   - Added 4 collection security rules
   - Added 2 validation functions
   - Total rules: 6 collections secured

2. **firestore.indexes.json**
   - Added 7 composite indexes
   - Total indexes: 8

### Files Created

1. **firebase/seed-data/programs.json**
   - 10 realistic programs with French content
   - Covers all main categories
   - Mix of free and premium offerings

2. **firebase/seed-data/content.json**
   - 20 meditation, yoga, and wellness content items
   - Realistic ratings and completion counts
   - Tags for searchability

3. **docs/FIRESTORE_COLLECTIONS_SCHEMA.md**
   - Comprehensive schema documentation
   - Security rules explanation
   - Kotlin mapping examples
   - Deployment instructions
   - Testing scenarios

### Breaking Changes
- None (new collections only)

### Migration Required
- None (no existing data to migrate)

### Deployment Status
- ✅ Rules file updated
- ✅ Indexes file updated
- ✅ Seed data generated
- ⏳ Pending: Deploy rules to Firebase
- ⏳ Pending: Deploy indexes to Firebase
- ⏳ Pending: Import seed data via Console

### Next Steps for Developers

#### Backend
- Deploy rules: `firebase deploy --only firestore:rules`
- Deploy indexes: `firebase deploy --only firestore:indexes`
- Import seed data via Firebase Console

#### Android
- Create Kotlin data models for new collections (see schema doc)
- Implement repositories:
  - `GratitudeRepository`
  - `ProgramRepository`
  - `UserProgramRepository`
  - `ContentRepository`
- Follow Firestore Kotlin mapping guide
- Use collection names exactly as defined in rules

### Documentation
- [Firestore Collections Schema](c:\Users\chris\source\repos\Ora\docs\FIRESTORE_COLLECTIONS_SCHEMA.md)
- [Firestore Kotlin Mapping Guide](c:\Users\chris\source\repos\Ora\docs\FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- [Deployment Report](c:\Users\chris\source\repos\Ora\bus\outbox\tech-backend-firebase\report-firestore-deployment-2025-10-05.json)

### Notes
- All field names use snake_case in Firestore
- Use `@PropertyName` annotations for Kotlin camelCase mapping
- Security rules enforce UID-based isolation for user data
- Public catalogs (programs, content) are read-only for clients
- Admin write access reserved for future Cloud Functions
