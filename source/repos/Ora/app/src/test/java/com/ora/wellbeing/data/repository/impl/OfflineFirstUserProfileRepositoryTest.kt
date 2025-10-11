package com.ora.wellbeing.data.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.CollectionReference
import com.google.android.gms.tasks.Tasks
import com.ora.wellbeing.core.util.NetworkMonitor
import com.ora.wellbeing.core.util.Resource
import com.ora.wellbeing.data.local.dao.UserDao
import com.ora.wellbeing.data.local.entities.User
import com.ora.wellbeing.data.model.UserProfile
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for OfflineFirstUserProfileRepository
 * Uses MockK for mocking dependencies
 */
class OfflineFirstUserProfileRepositoryTest {

    private lateinit var repository: OfflineFirstUserProfileRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userDao: UserDao
    private lateinit var networkMonitor: NetworkMonitor

    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference
    private lateinit var mockSnapshot: DocumentSnapshot

    @Before
    fun setup() {
        // Mock dependencies
        firestore = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        networkMonitor = mockk(relaxed = true)

        // Mock Firestore chain
        mockCollection = mockk(relaxed = true)
        mockDocument = mockk(relaxed = true)
        mockSnapshot = mockk(relaxed = true)

        every { firestore.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument

        // Create repository
        repository = OfflineFirstUserProfileRepository(firestore, userDao, networkMonitor)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getUserProfile should emit cached data first when available`() = runTest {
        // Given
        val uid = "test_user_123"
        val cachedUser = User(
            id = uid,
            name = "Cached User",
            email = "cached@example.com",
            createdAt = LocalDateTime.now(),
            lastActiveAt = LocalDateTime.now()
        )

        coEvery { userDao.getUserById(uid) } returns cachedUser
        every { networkMonitor.isCurrentlyConnected() } returns false

        // When
        repository.getUserProfile(uid).test {
            // Then
            // First emission: Loading
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Second emission: Success with cached data
            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat((success as Resource.Success).data.uid).isEqualTo(uid)

            awaitComplete()
        }

        // Verify
        coVerify { userDao.getUserById(uid) }
    }

    @Test
    fun `getUserProfile should fetch from Firestore when online and cache exists`() = runTest {
        // Given
        val uid = "test_user_456"
        val cachedUser = User(
            id = uid,
            name = "Cached User",
            email = "cached@example.com"
        )

        val firestoreProfile = UserProfile().apply {
            this.uid = uid
            firstName = "Fresh"
            lastName = "User"
            email = "fresh@example.com"
        }

        coEvery { userDao.getUserById(uid) } returns cachedUser
        every { networkMonitor.isCurrentlyConnected() } returns true
        every { mockDocument.get() } returns Tasks.forResult(mockSnapshot)
        every { mockSnapshot.toObject(UserProfile::class.java) } returns firestoreProfile
        coEvery { userDao.insertUser(any()) } just Runs

        // When
        repository.getUserProfile(uid).test {
            // Then
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Cached data
            val cached = awaitItem()
            assertThat(cached).isInstanceOf(Resource.Success::class.java)

            // Fresh data from Firestore
            val fresh = awaitItem()
            assertThat(fresh).isInstanceOf(Resource.Success::class.java)
            assertThat((fresh as Resource.Success).data.firstName).isEqualTo("Fresh")

            awaitComplete()
        }

        // Verify cache was updated
        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun `getUserProfile should return error when offline and no cache`() = runTest {
        // Given
        val uid = "test_user_789"

        coEvery { userDao.getUserById(uid) } returns null
        every { networkMonitor.isCurrentlyConnected() } returns false

        // When
        repository.getUserProfile(uid).test {
            // Then
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat((error as Resource.Error).message).contains("cached data")

            awaitComplete()
        }
    }

    @Test
    fun `setUserProfile should cache locally and sync to Firestore when online`() = runTest {
        // Given
        val profile = UserProfile().apply {
            uid = "test_user_set"
            firstName = "New"
            lastName = "User"
            email = "new@example.com"
        }

        coEvery { userDao.insertUser(any()) } just Runs
        every { networkMonitor.isCurrentlyConnected() } returns true
        every { mockDocument.set(any()) } returns Tasks.forResult(null)

        // When
        val result = repository.setUserProfile(profile)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { userDao.insertUser(any()) }
        verify { mockDocument.set(profile) }
    }

    @Test
    fun `setUserProfile should cache locally when offline`() = runTest {
        // Given
        val profile = UserProfile().apply {
            uid = "test_user_offline"
            firstName = "Offline"
            lastName = "User"
        }

        coEvery { userDao.insertUser(any()) } just Runs
        every { networkMonitor.isCurrentlyConnected() } returns false

        // When
        val result = repository.setUserProfile(profile)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { userDao.insertUser(any()) }
        verify(exactly = 0) { mockDocument.set(any()) }
    }

    @Test
    fun `updateUserProfile should update Firestore when online`() = runTest {
        // Given
        val uid = "test_user_update"
        val updates = mapOf("firstName" to "Updated")
        val cachedUser = User(id = uid, name = "Old Name", email = "test@example.com")

        coEvery { userDao.getUserById(uid) } returns cachedUser
        coEvery { userDao.updateUser(any()) } just Runs
        every { networkMonitor.isCurrentlyConnected() } returns true
        every { mockDocument.update(updates) } returns Tasks.forResult(null)

        // When
        val result = repository.updateUserProfile(uid, updates)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { mockDocument.update(updates) }
    }

    @Test
    fun `deleteUserProfile should delete from both cache and Firestore when online`() = runTest {
        // Given
        val uid = "test_user_delete"

        coEvery { userDao.deleteUserById(uid) } just Runs
        every { networkMonitor.isCurrentlyConnected() } returns true
        every { mockDocument.delete() } returns Tasks.forResult(null)

        // When
        val result = repository.deleteUserProfile(uid)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { userDao.deleteUserById(uid) }
        verify { mockDocument.delete() }
    }

    @Test
    fun `deleteUserProfile should delete from cache only when offline`() = runTest {
        // Given
        val uid = "test_user_delete_offline"

        coEvery { userDao.deleteUserById(uid) } just Runs
        every { networkMonitor.isCurrentlyConnected() } returns false

        // When
        val result = repository.deleteUserProfile(uid)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { userDao.deleteUserById(uid) }
        verify(exactly = 0) { mockDocument.delete() }
    }
}
