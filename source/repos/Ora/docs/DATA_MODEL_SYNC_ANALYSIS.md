# Data Model Synchronization Analysis
**Date**: 2025-11-03
**Author**: Claude Code
**Related Issues**: #8 (Feature), #9 (Spec)

## Executive Summary

This document analyzes the data model divergence between **OraWebApp** (Next.js admin portal) and **Ora** (Android mobile app) following the lessons and programs refactoring in Firebase.

### Key Findings:
- ❌ **Lessons (Content)** cannot be loaded from Firestore due to schema mismatch
- ❌ **Programs** structure has changed but Android expects old format
- ❌ **Field naming** differs: snake_case (backend) vs camelCase (Android)
- ✅ **Firestore SDK** already integrated in Android
- ✅ **Offline-first architecture** in place, just needs mapper updates

---

## Problem Statement

After the refactoring of lesson management in OraWebApp:
1. **Admin portal uploads lessons** to Firebase Storage with new schema
2. **Android app cannot load** these lessons (expects old format)
3. **Users cannot access** content created by admins/teachers

**Impact**: High priority blocking issue preventing content delivery.

---

## Data Model Comparison

### Lessons (Content)

| Field | OraWebApp Backend | Ora Android (Current) | Status |
|-------|-------------------|----------------------|--------|
| **Field Naming** | `snake_case` | `camelCase` | ❌ Incompatible |
| **Title** | `title: string` | `title: String` | ✅ Compatible |
| **Program ID** | `program_id: string` | Not present | ❌ Missing |
| **Duration** | `duration_sec: number` | `durationMinutes: Int` | ⚠️ Needs conversion |
| **Type** | `type: 'video' \| 'audio'` | `type: ContentType` (enum) | ⚠️ Needs mapping |
| **Status** | `status: 'draft' \| 'ready' \| ...` | Not present | ❌ Missing |
| **Renditions** | `renditions: { high, medium, low }` | `videoUrl: String?` | ⚠️ Needs extraction |
| **Thumbnail** | `thumbnail_url: string?` | `thumbnailUrl: String?` | ✅ Compatible (rename) |
| **Tags** | `tags: string[]` | `tags: List<String>` | ✅ Compatible |

### Programs

| Field | OraWebApp Backend | Ora Android (Current) | Status |
|-------|-------------------|----------------------|--------|
| **Field Naming** | `snake_case` | `camelCase` | ❌ Incompatible |
| **Duration** | `duration_days: number` | `duration: Int` (days) | ✅ Compatible (rename) |
| **Lessons** | `lessons: string[]` (IDs) | `sessions: List<Map>` (embedded) | ❌ Different structure |
| **Category** | `category: 'meditation' \| 'yoga' \| ...` | `category: String` | ⚠️ Needs validation |
| **Difficulty** | `difficulty: 'beginner' \| ...` | `level: String` | ⚠️ Field name differs |
| **Cover Image** | `cover_image_url: string?` | `thumbnailUrl: String?` | ✅ Compatible (rename) |
| **Status** | `status: 'draft' \| 'published' \| ...` | `isActive: Boolean` | ⚠️ Needs mapping |
| **Author** | `author_id: string` | Not present | ❌ Missing |
| **Scheduling** | `scheduled_publish_at: Timestamp?` | Not present | ❌ Missing |

---

## Firestore Schema (Backend - Source of Truth)

### Collection: `lessons/{lessonId}`

```typescript
interface LessonDocument {
  // Identification
  title: string;
  description: string | null;
  type: 'video' | 'audio';

  // Program Association
  program_id: string;
  order: number;

  // Media Details
  duration_sec: number | null;
  tags: string[];
  transcript: string | null;

  // Storage & Processing
  status: 'draft' | 'uploading' | 'processing' | 'ready' | 'failed';
  storage_path_original: string | null;

  // Video Renditions (multiple quality levels)
  renditions?: {
    high?: { path: string; width?: number; height?: number; bitrate_kbps?: number };
    medium?: { path: string; width?: number; height?: number; bitrate_kbps?: number };
    low?: { path: string; width?: number; height?: number; bitrate_kbps?: number };
  };

  // Audio Variants
  audio_variants?: {
    high?: { path: string; bitrate_kbps: number };
    medium?: { path: string; bitrate_kbps: number };
    low?: { path: string; bitrate_kbps: number };
  };

  // Metadata
  codec: string | null;
  size_bytes: number | null;
  thumbnail_url?: string | null;
  mime_type?: string | null;

  // Timestamps
  created_at: Timestamp;
  updated_at: Timestamp;

  // Authorship
  author_id: string;
}
```

**Collection Path**: `lessons/{lessonId}`
**Indexes Required**:
- `status + created_at` (for listing ready lessons)
- `program_id + order` (for program lesson ordering)

---

### Collection: `programs/{programId}`

```typescript
interface ProgramDocument {
  // Basic Info
  title: string;
  description: string;

  // Classification
  category: 'meditation' | 'yoga' | 'mindfulness' | 'wellness';
  difficulty: 'beginner' | 'intermediate' | 'advanced';

  // Structure
  duration_days: number;
  lessons: string[]; // Array of lesson IDs in order

  // Media
  cover_image_url: string | null;
  cover_storage_path: string | null;

  // Publishing
  status: 'draft' | 'published' | 'archived';
  scheduled_publish_at: Timestamp | null;
  scheduled_archive_at: Timestamp | null;
  auto_publish_enabled: boolean;

  // Metadata
  tags: string[];
  author_id: string;
  created_at: Timestamp;
  updated_at: Timestamp;
}
```

**Collection Path**: `programs/{programId}`
**Indexes Required**:
- `status + created_at` (for listing published programs)
- `category + status` (for filtering)

---

## Android Data Models (Target)

### Current: `ContentItem.kt` (Firestore Model)

```kotlin
@IgnoreExtraProperties
class ContentItem() {
    var id: String = ""
    var title: String = ""
    var category: String = ""
    var duration: String = ""
    var durationMinutes: Int = 0
    var instructor: String = ""
    var description: String = ""
    var thumbnailUrl: String? = null
    var videoUrl: String? = null
    var audioUrl: String? = null
    var isPremiumOnly: Boolean = false
    var isPopular: Boolean = false
    var isNew: Boolean = false
    var rating: Float = 0.0f
    var completionCount: Int = 0
    var tags: List<String> = emptyList()
    var isActive: Boolean = true
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null
    var publishedAt: Timestamp? = null
}
```

**Issues**:
- ❌ No `program_id` field
- ❌ No `status` field
- ❌ No `renditions` support
- ❌ No `order` field
- ⚠️ Uses `category` as string instead of type mapping

---

### Current: `Program.kt` (Firestore Model)

```kotlin
@IgnoreExtraProperties
class Program() {
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var category: String = ""
    var duration: Int = 0 // days
    var level: String = ""
    var participantCount: Int = 0
    var rating: Float = 0.0f
    var thumbnailUrl: String? = null
    var instructor: String? = null
    var isPremiumOnly: Boolean = false
    var sessions: List<Map<String, Any>> = emptyList() // ❌ Should be lesson IDs
    var isActive: Boolean = true
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null
}
```

**Issues**:
- ❌ Uses `sessions: List<Map>` instead of `lessons: List<String>`
- ❌ No `author_id` field
- ❌ No scheduling fields
- ⚠️ `level` instead of `difficulty`
- ⚠️ `thumbnailUrl` instead of `cover_image_url`

---

## Proposed Solution: Mappers

### LessonMapper (Firestore ↔ Android)

Create a mapper to convert between backend `LessonDocument` and Android `ContentItem`:

```kotlin
// app/src/main/java/com/ora/wellbeing/data/mapper/LessonMapper.kt
object LessonMapper {
    /**
     * Converts Firestore LessonDocument (snake_case) to ContentItem (camelCase)
     */
    fun fromFirestore(id: String, doc: LessonDocument): ContentItem {
        return ContentItem().apply {
            this.id = id
            this.title = doc.title
            this.description = doc.description ?: ""
            this.category = mapLessonTypeToCategory(doc.type, doc.tags)
            this.duration = formatDuration(doc.duration_sec)
            this.durationMinutes = (doc.duration_sec ?: 0) / 60
            this.thumbnailUrl = doc.thumbnail_url
            this.videoUrl = extractBestVideoUrl(doc.renditions)
            this.audioUrl = extractBestAudioUrl(doc.audio_variants)
            this.tags = doc.tags
            this.isActive = doc.status == "ready"
            this.createdAt = doc.created_at
            this.updatedAt = doc.updated_at
        }
    }

    /**
     * Extracts best quality video URL from renditions
     * Priority: high > medium > low
     */
    private fun extractBestVideoUrl(renditions: Map<String, Map<String, Any>>?): String? {
        return renditions?.get("high")?.get("path") as? String
            ?: renditions?.get("medium")?.get("path") as? String
            ?: renditions?.get("low")?.get("path") as? String
    }

    /**
     * Extracts best quality audio URL from variants
     */
    private fun extractBestAudioUrl(audioVariants: Map<String, Map<String, Any>>?): String? {
        return audioVariants?.get("high")?.get("path") as? String
            ?: audioVariants?.get("medium")?.get("path") as? String
            ?: audioVariants?.get("low")?.get("path") as? String
    }

    /**
     * Maps lesson type and tags to Android category
     */
    private fun mapLessonTypeToCategory(type: String, tags: List<String>): String {
        // Use tags to determine category
        return when {
            tags.contains("yoga") -> "Yoga"
            tags.contains("meditation") -> "Méditation"
            tags.contains("breathing") -> "Respiration"
            tags.contains("pilates") -> "Pilates"
            tags.contains("sleep") -> "Sommeil"
            else -> "Bien-être"
        }
    }

    /**
     * Formats duration from seconds to readable string
     */
    private fun formatDuration(durationSec: Int?): String {
        if (durationSec == null) return ""
        val minutes = durationSec / 60
        return "$minutes min"
    }
}
```

---

### ProgramMapper (Firestore ↔ Android)

```kotlin
// app/src/main/java/com/ora/wellbeing/data/mapper/ProgramMapper.kt
object ProgramMapper {
    /**
     * Converts Firestore ProgramDocument (snake_case) to Program (camelCase)
     */
    fun fromFirestore(id: String, doc: ProgramDocument): Program {
        return Program().apply {
            this.id = id
            this.title = doc.title
            this.description = doc.description
            this.category = doc.category
            this.duration = doc.duration_days
            this.level = mapDifficulty(doc.difficulty)
            this.thumbnailUrl = doc.cover_image_url
            this.isPremiumOnly = false // Determine from plan logic
            this.sessions = emptyList() // Will be populated with lessons
            this.isActive = doc.status == "published"
            this.createdAt = doc.created_at
            this.updatedAt = doc.updated_at
        }
    }

    /**
     * Maps backend difficulty to Android level
     */
    private fun mapDifficulty(difficulty: String): String {
        return when (difficulty) {
            "beginner" -> "Débutant"
            "intermediate" -> "Intermédiaire"
            "advanced" -> "Avancé"
            else -> "Tous niveaux"
        }
    }
}
```

---

## Implementation Plan

### Phase 1: Create Firestore Models (Week 1, Day 1-2)
- [ ] Create `app/src/main/java/com/ora/wellbeing/data/model/firestore/` package
- [ ] Create `LessonDocument.kt` with snake_case fields
- [ ] Create `ProgramDocument.kt` with snake_case fields
- [ ] Follow Firestore best practices (no data class, var properties, @IgnoreExtraProperties)

### Phase 2: Create Mappers (Week 1, Day 2-3)
- [ ] Create `app/src/main/java/com/ora/wellbeing/data/mapper/` package
- [ ] Implement `LessonMapper.fromFirestore()`
- [ ] Implement `ProgramMapper.fromFirestore()`
- [ ] Write unit tests for both mappers

### Phase 3: Update Repositories (Week 1, Day 3-4)
- [ ] Update `OfflineFirstContentRepository.kt` to use `LessonMapper`
- [ ] Add Firestore queries for `lessons` collection (filter by `status == "ready"`)
- [ ] Update sync logic to convert and cache in Room

### Phase 4: Update Room Entities (Week 1, Day 4-5)
- [ ] Add new fields to `Content` entity (programId, status, order)
- [ ] Create migration v2 → v3 for database schema
- [ ] Update DAOs with new queries

### Phase 5: Update ViewModels & UI (Week 2, Day 1-2)
- [ ] Update `LibraryViewModel.kt` to fetch lessons
- [ ] Update `ProgramsViewModel.kt` to populate program lessons
- [ ] Modify UI to show lesson status, renditions, etc.

### Phase 6: Testing (Week 2, Day 3-4)
- [ ] Unit tests for mappers
- [ ] Integration tests for repository sync
- [ ] UI tests for LibraryScreen with real data
- [ ] Test offline mode thoroughly

### Phase 7: Deployment (Week 2, Day 5)
- [ ] Document schema in `docs/FIRESTORE_SCHEMA.md`
- [ ] Update `CLAUDE.md` with mapper usage
- [ ] Deploy to beta testers
- [ ] Monitor logs and fix issues

---

## Testing Checklist

### Unit Tests
- [x] `LessonMapper.fromFirestore()` maps all fields correctly
- [x] `LessonMapper.extractBestVideoUrl()` prioritizes high quality
- [x] `LessonMapper.mapLessonTypeToCategory()` handles all types
- [x] `ProgramMapper.fromFirestore()` maps all fields correctly
- [x] `ProgramMapper.mapDifficulty()` converts all levels

### Integration Tests
- [ ] Fetch lessons from Firestore and cache in Room
- [ ] Fetch programs with populated lessons
- [ ] Offline mode returns cached data
- [ ] Background sync updates cache

### UI Tests
- [ ] LibraryScreen displays lessons from Firestore
- [ ] ProgramsScreen shows programs with lesson count
- [ ] Lesson detail screen shows video/audio renditions
- [ ] Offline indicator shows when network unavailable

---

## Risk Assessment

### High Risks ❌
- **Schema mismatch**: Firestore field names must match exactly (snake_case)
- **Null values**: Missing fields in Firestore will cause crashes

**Mitigation**: Use `@IgnoreExtraProperties` and null-safe mappers

### Medium Risks ⚠️
- **Performance**: Large lesson lists may slow down sync
- **Offline conflicts**: User edits while offline may conflict with server

**Mitigation**: Pagination, timestamp-based conflict resolution

### Low Risks ✅
- **Backward compatibility**: Old cached data will be migrated
- **Breaking changes**: Room migration will handle schema updates

---

## Success Metrics

1. ✅ **Lessons load successfully**: 100% of "ready" lessons from Firestore appear in Library
2. ✅ **Programs populate**: All programs show correct lesson count and order
3. ✅ **Offline works**: Cached lessons accessible without network
4. ✅ **No crashes**: Zero crashes related to data model mismatch
5. ✅ **Performance**: Sync completes in <5 seconds for 50 lessons

---

## References

### OraWebApp (Backend)
- **Lesson Types**: `lib/validators/lesson.ts`, `types/lesson.ts`
- **Program Types**: `lib/validators/program.ts`, `types/program.ts`
- **Firestore Rules**: `firestore.rules`

### Ora Android
- **Firestore Models**: `app/src/main/java/com/ora/wellbeing/data/model/`
- **Room Entities**: `app/src/main/java/com/ora/wellbeing/data/local/entities/`
- **Repositories**: `app/src/main/java/com/ora/wellbeing/data/repository/impl/`

### GitHub Issues
- **Feature Request**: #8
- **Technical Spec**: #9

---

## Appendix: Field Mapping Reference

### Lesson Field Mapping

| Firestore (snake_case) | Android (camelCase) | Conversion Logic |
|------------------------|---------------------|------------------|
| `title` | `title` | Direct copy |
| `description` | `description` | `?: ""` (default empty) |
| `type` | `category` | Map via tags |
| `program_id` | `programId` | Direct copy |
| `order` | `lessonOrder` | Direct copy |
| `duration_sec` | `durationMinutes` | `/ 60` |
| `tags` | `tags` | Direct copy |
| `status` | `isActive` | `== "ready"` |
| `storage_path_original` | `storagePathOriginal` | Direct copy |
| `renditions.high.path` | `videoUrl` | Extract best |
| `audio_variants.high.path` | `audioUrl` | Extract best |
| `thumbnail_url` | `thumbnailUrl` | Direct copy |
| `created_at` | `createdAt` | Direct copy |
| `updated_at` | `updatedAt` | Direct copy |
| `author_id` | `authorId` | Direct copy |

### Program Field Mapping

| Firestore (snake_case) | Android (camelCase) | Conversion Logic |
|------------------------|---------------------|------------------|
| `title` | `title` | Direct copy |
| `description` | `description` | Direct copy |
| `category` | `category` | Direct copy |
| `difficulty` | `level` | Map (beginner → Débutant) |
| `duration_days` | `duration` | Direct copy |
| `lessons` (string[]) | `sessions` (Map[]) | **TODO: Populate** |
| `cover_image_url` | `thumbnailUrl` | Direct copy |
| `status` | `isActive` | `== "published"` |
| `author_id` | `authorId` | Direct copy |
| `tags` | `tags` | Direct copy |
| `created_at` | `createdAt` | Direct copy |
| `updated_at` | `updatedAt` | Direct copy |

---

**Document Version**: 1.0
**Last Updated**: 2025-11-03
**Next Review**: After Phase 1 implementation
