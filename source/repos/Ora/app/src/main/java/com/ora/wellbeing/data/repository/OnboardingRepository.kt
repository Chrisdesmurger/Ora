package com.ora.wellbeing.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ora.wellbeing.data.model.onboarding.OnboardingConfig
import com.ora.wellbeing.data.model.onboarding.OnboardingMetadata
import com.ora.wellbeing.data.model.onboarding.UserOnboardingAnswer
import com.ora.wellbeing.data.model.onboarding.UserOnboardingResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Onboarding
 * Manages onboarding configuration fetching and user response storage
 */
@Singleton
class OnboardingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Fetch active onboarding configuration
     * Returns the first published config (status = "active")
     */
    suspend fun getActiveOnboardingConfig(): Result<OnboardingConfig> {
        return try {
            Timber.d("OnboardingRepository: Fetching active onboarding config")

            val snapshot = firestore.collection("onboarding_configs")
                .whereEqualTo("status", "active")
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Timber.w("OnboardingRepository: No active onboarding config found")
                return Result.failure(Exception("No active onboarding configuration found"))
            }

            val doc = snapshot.documents.first()
            val config = doc.toObject(OnboardingConfig::class.java)
                ?: return Result.failure(Exception("Failed to parse onboarding config"))

            config.id = doc.id
            Timber.d("OnboardingRepository: Loaded config ${config.id} with ${config.questions.size} questions")
            Result.success(config)
        } catch (e: Exception) {
            Timber.e(e, "OnboardingRepository: Failed to fetch config")
            Result.failure(e)
        }
    }

    /**
     * Get user onboarding response from Firestore
     * Stored in users/{uid} document under "onboarding" field
     */
    suspend fun getUserOnboardingResponse(uid: String): Result<UserOnboardingResponse?> {
        return try {
            Timber.d("OnboardingRepository: Fetching onboarding response for user $uid")

            val doc = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val onboardingData = doc.get("onboarding") as? Map<*, *>

            if (onboardingData == null) {
                Timber.d("OnboardingRepository: No onboarding data for user $uid")
                return Result.success(null)
            }

            // Parse the onboarding field
            val response = UserOnboardingResponse(
                uid = uid,
                configVersion = onboardingData["configVersion"] as? String ?: "",
                completed = onboardingData["completed"] as? Boolean ?: false,
                completedAt = onboardingData["completedAt"] as? Timestamp,
                startedAt = onboardingData["startedAt"] as? Timestamp,
                answers = parseAnswers(onboardingData["answers"] as? List<*>),
                metadata = parseMetadata(onboardingData["metadata"] as? Map<*, *>)
            )

            Timber.d("OnboardingRepository: Loaded response, completed=${response.completed}")
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "OnboardingRepository: Failed to fetch user response")
            Result.failure(e)
        }
    }

    /**
     * Save user onboarding response to Firestore
     *
     * DUAL WRITE: Saves to both locations for backward compatibility and performance
     * 1. users/{uid}.onboarding (nested field - backward compatibility)
     * 2. user_onboarding_responses/{uid}/responses/{configVersion} (dedicated collection - analytics)
     */
    suspend fun saveUserOnboardingResponse(
        uid: String,
        response: UserOnboardingResponse
    ): Result<Unit> {
        return try {
            Timber.d("OnboardingRepository: Saving onboarding response for user $uid (dual write)")

            // Prepare nested field data (for backward compatibility)
            val onboardingData = mapOf(
                "uid" to response.uid,
                "configVersion" to response.configVersion,
                "completed" to response.completed,
                "completedAt" to response.completedAt,
                "startedAt" to response.startedAt,
                "answers" to response.answers.map { answer ->
                    mapOf(
                        "questionId" to answer.questionId,
                        "selectedOptions" to answer.selectedOptions,
                        "textAnswer" to answer.textAnswer,
                        "answeredAt" to answer.answeredAt
                    )
                },
                "metadata" to response.metadata?.let { meta ->
                    mapOf(
                        "deviceType" to meta.deviceType,
                        "appVersion" to meta.appVersion,
                        "totalTimeSeconds" to meta.totalTimeSeconds,
                        "locale" to meta.locale
                    )
                }
            )

            // Use batch write for atomicity
            val batch = firestore.batch()

            // 1. Write to nested field (backward compatibility)
            val userRef = firestore.collection("users").document(uid)
            batch.set(userRef, mapOf("onboarding" to onboardingData), com.google.firebase.firestore.SetOptions.merge())
            Timber.d("OnboardingRepository: Queued write to users/$uid.onboarding")

            // 2. Write to dedicated collection (new - uses @PropertyName for snake_case)
            val responseRef = firestore
                .collection("user_onboarding_responses")
                .document(uid)
                .collection("responses")
                .document(response.configVersion)
            batch.set(responseRef, response)
            Timber.d("OnboardingRepository: Queued write to user_onboarding_responses/$uid/responses/${response.configVersion}")

            // Commit both writes atomically
            batch.commit().await()

            Timber.d("OnboardingRepository: Response saved successfully to both locations")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "OnboardingRepository: Failed to save response")
            Result.failure(e)
        }
    }

    /**
     * Start onboarding for user (mark started)
     */
    suspend fun startOnboarding(uid: String, configVersion: String): Result<Unit> {
        return try {
            Timber.d("OnboardingRepository: Starting onboarding for user $uid")

            val onboardingData = mapOf(
                "uid" to uid,
                "configVersion" to configVersion,
                "completed" to false,
                "startedAt" to Timestamp.now(),
                "answers" to emptyList<Any>()
            )

            firestore.collection("users")
                .document(uid)
                .set(mapOf("onboarding" to onboardingData), com.google.firebase.firestore.SetOptions.merge())
                .await()

            Timber.d("OnboardingRepository: Onboarding started")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "OnboardingRepository: Failed to start onboarding")
            Result.failure(e)
        }
    }

    /**
     * Listen to user onboarding status changes
     */
    fun observeUserOnboardingStatus(uid: String): Flow<UserOnboardingResponse?> = callbackFlow {
        Timber.d("OnboardingRepository: Starting to observe onboarding for user $uid")

        val listener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "OnboardingRepository: Error observing onboarding")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val onboardingData = snapshot.get("onboarding") as? Map<*, *>
                if (onboardingData == null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val response = UserOnboardingResponse(
                    uid = uid,
                    configVersion = onboardingData["configVersion"] as? String ?: "",
                    completed = onboardingData["completed"] as? Boolean ?: false,
                    completedAt = onboardingData["completedAt"] as? Timestamp,
                    startedAt = onboardingData["startedAt"] as? Timestamp,
                    answers = parseAnswers(onboardingData["answers"] as? List<*>),
                    metadata = parseMetadata(onboardingData["metadata"] as? Map<*, *>)
                )

                trySend(response)
            }

        awaitClose { listener.remove() }
    }

    // Helper: Parse answers list from Firestore
    private fun parseAnswers(answersList: List<*>?): List<UserOnboardingAnswer> {
        if (answersList == null) return emptyList()

        return answersList.mapNotNull { item ->
            val answerMap = item as? Map<*, *> ?: return@mapNotNull null
            UserOnboardingAnswer(
                questionId = answerMap["questionId"] as? String ?: "",
                selectedOptions = (answerMap["selectedOptions"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                textAnswer = answerMap["textAnswer"] as? String,
                answeredAt = answerMap["answeredAt"] as? Timestamp
            )
        }
    }

    // Helper: Parse metadata from Firestore
    private fun parseMetadata(metadataMap: Map<*, *>?): OnboardingMetadata? {
        if (metadataMap == null) return null

        return OnboardingMetadata(
            deviceType = metadataMap["deviceType"] as? String,
            appVersion = metadataMap["appVersion"] as? String,
            totalTimeSeconds = (metadataMap["totalTimeSeconds"] as? Number)?.toInt(),
            locale = metadataMap["locale"] as? String
        )
    }
}
