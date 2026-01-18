# Flow Patterns Guide - Offline-First Architecture

**Last Updated**: 2025-11-04
**Status**: Official Pattern Guide

---

## Overview

This guide documents the correct Flow patterns for offline-first repositories in the Ora Android app. Follow these patterns to avoid infinite loops, memory leaks, and UI hangs.

---

## Pattern 1: Reactive Flow (Continuous Updates)

**Use when**: Data can change over time, UI needs reactive updates

### Correct Pattern ✅

```kotlin
override fun getAllContent(): Flow<List<ContentItem>> {
    Timber.d("getAllContent: Returning offline-first Flow")

    return contentDao.getAllContentFlow()
        .map { entities ->
            entities.map { it.toDomainModel() }
        }
        .onStart {
            // Trigger sync in background if needed
            if (shouldSync()) {
                Timber.d("getAllContent: Triggering background sync")
                syncAllContentFromFirestore()
            }
        }
}
```

**Why it works**:
- Returns Flow directly (no `flow {}` builder)
- `.map {}` transforms each emission without blocking
- `.onStart {}` runs once when Flow is first collected
- Sync happens in background, doesn't block emissions
- UI gets immediate cache data, then updates when sync completes

---

### Incorrect Pattern ❌

```kotlin
// DON'T DO THIS - INFINITE LOOP!
override fun getAllContent(): Flow<List<ContentItem>> = flow {
    contentDao.getAllContentFlow().collect { entities ->
        emit(entities.map { it.toDomainModel() })
    }

    // Code here NEVER executes because .collect() blocks forever!
    if (shouldSync()) {
        syncAllContentFromFirestore()
    }
}
```

**Why it fails**:
- `flow {}` builder creates new Flow
- `.collect {}` is a terminal operator that blocks until upstream completes
- Room Flows **never complete** - they emit indefinitely
- Code after `.collect {}` is unreachable
- UI hangs waiting for emissions that never come

---

## Pattern 2: Single Value (Suspend Function)

**Use when**: Need one-time snapshot of data, no reactive updates

### Correct Pattern ✅

```kotlin
override suspend fun getTotalContentCount(): Int {
    return try {
        Timber.d("getTotalContentCount: Fetching from cache")

        // Sync if needed
        if (shouldSync()) {
            Timber.d("getTotalContentCount: Syncing from Firestore")
            syncAllContentFromFirestore()
        }

        // Get single value from Flow using .first()
        val count = contentDao.getAllContentFlow().first().size
        Timber.d("getTotalContentCount: $count items")
        count
    } catch (e: Exception) {
        Timber.e(e, "getTotalContentCount: Error, returning 0")
        0
    }
}
```

**Why it works**:
- `.first()` gets first emission then cancels Flow
- Sync happens before getting value
- Returns single Int, not a Flow
- Proper exception handling

---

### Incorrect Pattern ❌

```kotlin
// DON'T DO THIS - WRONG RETURN TYPE!
override suspend fun getTotalContentCount(): Int {
    var count = 0
    contentDao.getAllContentFlow().collect { entities ->
        count = entities.size
        // When does this stop collecting? Never!
    }
    return count // Unreachable!
}
```

**Why it fails**:
- `.collect {}` blocks forever
- `return` statement never reached
- Function suspends indefinitely

---

## Pattern 3: Flow with Filtering

**Use when**: Need to transform or filter reactive data

### Correct Pattern ✅

```kotlin
override fun getContentByCategory(category: String): Flow<List<ContentItem>> {
    require(category.isNotBlank()) { "Category cannot be blank" }
    Timber.d("getContentByCategory: Returning offline-first Flow for category=$category")

    val roomCategory = mapStringToCategory(category)

    return contentDao.getContentByCategoryFlow(roomCategory)
        .map { entities ->
            entities.map { it.toDomainModel() }
        }
        .onStart {
            if (shouldSync()) {
                Timber.d("getContentByCategory: Triggering background sync")
                syncAllContentFromFirestore()
            }
        }
}
```

**Key points**:
- Chain transformations with `.map {}`
- Can add `.filter {}`, `.distinctUntilChanged()`, etc.
- All operators are non-blocking

---

### Also Correct: Client-Side Filtering ✅

```kotlin
override fun getContentByInstructor(instructor: String): Flow<List<ContentItem>> {
    require(instructor.isNotBlank()) { "Instructor cannot be blank" }
    Timber.d("getContentByInstructor: Returning offline-first Flow")

    return contentDao.getAllContentFlow()
        .map { entities ->
            entities
                .filter { it.instructorName?.equals(instructor, ignoreCase = true) == true }
                .map { it.toDomainModel() }
        }
        .onStart {
            if (shouldSync()) {
                syncAllContentFromFirestore()
            }
        }
}
```

**Key points**:
- Filter in `.map {}` block
- Multiple transformations chained together
- Still non-blocking and reactive

---

## Pattern 4: Conditional Flow

**Use when**: Need to return different Flows based on condition

### Correct Pattern ✅

```kotlin
override fun searchContent(query: String): Flow<List<ContentItem>> {
    Timber.d("searchContent: Returning offline-first Flow for query='$query'")

    return if (query.isBlank()) {
        // Empty query returns all content
        getAllContent()
    } else {
        contentDao.searchContentFlow(query)
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
            .onStart {
                if (shouldSync()) {
                    Timber.d("searchContent: Triggering background sync")
                    syncAllContentFromFirestore()
                }
            }
    }
}
```

**Key points**:
- Use `if/else` or `when` to return different Flows
- Each branch returns a Flow
- No `.collect {}` needed

---

## Pattern 5: Background Sync (Private Suspend Function)

**Use when**: Need to sync data from Firestore to Room

### Correct Pattern ✅

```kotlin
private suspend fun syncAllContentFromFirestore() {
    try {
        Timber.d("syncAllContent: Starting sync from Firestore")

        val snapshot = firestore
            .collection(COLLECTION_LESSONS)
            .whereEqualTo("status", "ready")
            .get()
            .await()

        val entities = snapshot.documents.mapNotNull { doc ->
            try {
                val lessonDoc = doc.toObject(LessonDocument::class.java)
                if (lessonDoc != null) {
                    val contentItem = LessonMapper.fromFirestore(doc.id, lessonDoc)
                    contentItem.toEntity()
                } else null
            } catch (e: Exception) {
                Timber.e(e, "syncAllContent: Error mapping document ${doc.id}")
                null
            }
        }

        if (entities.isNotEmpty()) {
            contentDao.insertAllContent(entities)
            Timber.d("syncAllContent: Synced ${entities.size} items to cache")
        }

        markSynced()
    } catch (e: Exception) {
        Timber.e(e, "syncAllContent: Sync failed")
        // Don't throw - continue with cached data
    }
}
```

**Key points**:
- `private suspend fun` (not Flow)
- One-time operation
- Error handling (don't throw, just log)
- Update `lastSyncTime` on success
- Write to Room cache

---

## Anti-Patterns to Avoid

### ❌ Don't Use `.collect {}` Inside `flow {}`

```kotlin
// WRONG! Creates infinite loop
flow {
    daoFlow.collect { data ->
        emit(data)
    }
}
```

**Fix**: Return Flow directly with `.map {}`

---

### ❌ Don't Block Main Thread

```kotlin
// WRONG! Blocks UI thread
fun getData(): List<Item> {
    return runBlocking {
        dao.getAllFlow().first()
    }
}
```

**Fix**: Use `suspend fun` or return `Flow`

---

### ❌ Don't Create Flow from Suspend Function

```kotlin
// WRONG! Unnecessary wrapper
fun getData(): Flow<List<Item>> = flow {
    val data = dao.getAll() // suspend function
    emit(data)
}
```

**Fix**: Use `flow { emit(dao.getAll()) }` OR convert DAO to return Flow

---

### ❌ Don't Forget Error Handling

```kotlin
// WRONG! No error handling
.map { entities ->
    entities.map { it.toDomainModel() } // May throw!
}
```

**Fix**: Wrap in try-catch or use `.catch {}`

```kotlin
.map { entities ->
    entities.mapNotNull { entity ->
        try {
            entity.toDomainModel()
        } catch (e: Exception) {
            Timber.e(e, "Error mapping entity")
            null
        }
    }
}
.catch { e ->
    Timber.e(e, "Flow error")
    emit(emptyList())
}
```

---

## Testing Patterns

### Unit Test: Flow Emissions

```kotlin
@Test
fun `getAllContent emits cached data immediately`() = runTest {
    // Given
    val cachedContent = listOf(mockContent1, mockContent2)
    coEvery { contentDao.getAllContentFlow() } returns flowOf(cachedContent)

    // When
    val result = repository.getAllContent().first()

    // Then
    assertEquals(2, result.size)
    verify { contentDao.getAllContentFlow() }
}
```

---

### Unit Test: Background Sync

```kotlin
@Test
fun `getAllContent triggers sync when cache stale`() = runTest {
    // Given
    val cachedContent = listOf(mockContent1)
    every { contentDao.getAllContentFlow() } returns flowOf(cachedContent)
    coEvery { firestore.collection(any()).get() } returns mockSnapshot

    // When
    repository.getAllContent().first()

    // Then
    coVerify { firestore.collection("lessons").get() }
}
```

---

## Performance Guidelines

### Cache Hit Rate
- **Target**: >95% of requests served from cache
- **Strategy**: 1-hour sync interval (configurable)
- **Measurement**: Log `shouldSync()` results

### Memory Usage
- **Target**: No memory leaks from Flow collectors
- **Strategy**: Use `.onStart {}` instead of `.collect {}`
- **Measurement**: Android Studio Memory Profiler

### UI Responsiveness
- **Target**: <50ms to first emission from cache
- **Strategy**: Return Room Flow directly (no blocking operations)
- **Measurement**: Log timestamps in Timber

---

## Quick Reference

| Use Case | Pattern | Return Type | Example |
|----------|---------|-------------|---------|
| Reactive list | `.map {} .onStart {}` | `Flow<List<T>>` | `getAllContent()` |
| Reactive item | `.map {} .onStart {}` | `Flow<T?>` | `getContent(id)` |
| Single value | `.first()` | `suspend fun: T` | `getTotalCount()` |
| Background sync | `firestore.get().await()` | `private suspend fun` | `syncFromFirestore()` |
| Filtering | `.map { filter {} }` | `Flow<List<T>>` | `getByCategory()` |
| Search | `.map { search {} }` | `Flow<List<T>>` | `searchContent()` |

---

## Checklist for New Repository Methods

When adding a new repository method:

- [ ] Returns Flow for reactive data, suspend for one-time operations
- [ ] Uses `.map {}` and `.onStart {}` (not `flow {}` + `.collect {}`)
- [ ] Triggers background sync in `.onStart {}` if needed
- [ ] Has proper error handling (try-catch or `.catch {}`)
- [ ] Logs key operations with Timber
- [ ] Validates input parameters (`require`, `check`)
- [ ] Includes KDoc documentation
- [ ] Adds unit tests for:
  - [ ] Immediate cache emission
  - [ ] Background sync triggering
  - [ ] Error handling
  - [ ] Edge cases (empty, null)

---

## References

### Official Documentation
- [Kotlin Flow Guide](https://kotlinlang.org/docs/flow.html)
- [Room with Flow](https://developer.android.com/training/data-storage/room/async-queries#flow)
- [Offline-First Architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)

### Internal Documentation
- `docs/FEATURE_OFFLINE_FIRST_SYNC.md` - Complete offline-first guide
- `reports/build/diagnostic-infinite-loop-fix.md` - Fix details
- `CLAUDE.md` - Project architecture overview

### Reference Implementations
- `app/src/main/java/com/ora/wellbeing/data/repository/impl/ProgramRepositoryImpl.kt` - Correct pattern
- `app/src/main/java/com/ora/wellbeing/data/repository/impl/ContentRepositoryImpl.kt` - Fixed pattern

---

## FAQ

**Q: When should I use `flow {}` builder?**
A: Only when creating a new Flow from scratch (e.g., wrapping a callback). If you already have a Flow (from DAO), just return it with `.map {}`.

**Q: Why can't I use `.collect {}` inside `flow {}`?**
A: Because `.collect {}` is a terminal operator that suspends until the upstream Flow completes. Room Flows never complete, so it blocks forever.

**Q: How do I trigger sync without blocking?**
A: Use `.onStart {}` - it runs once when Flow is first collected, but doesn't block emissions.

**Q: What if I need to sync before emitting data?**
A: Use `.onStart {}` to trigger sync, then rely on Room's reactive Flow to emit updated data automatically when sync completes.

**Q: How do I test background sync?**
A: Mock the DAO and Firestore, call the repository method, verify sync was triggered using `coVerify`.

---

**Last Updated**: 2025-11-04
**Maintained by**: build-debug-android agent
**Status**: Official Pattern Guide
