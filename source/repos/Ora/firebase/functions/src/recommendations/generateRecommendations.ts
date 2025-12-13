/**
 * Ora Wellbeing - Recommendation Generation Functions
 *
 * Cloud Functions for generating personalized lesson recommendations
 * based on user onboarding responses.
 */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { Timestamp } from "firebase-admin/firestore";
import {
  UserOnboarding,
  OnboardingAnswer,
  LessonDocument,
  UserRecommendation,
  INTENTION_TO_DISCIPLINE,
  EXPERIENCE_LEVEL_ORDER,
} from "./types";

const ALGORITHM_VERSION = "1.0.0";
const MAX_RECOMMENDATIONS = 5;
const BATCH_SIZE = 100;

/**
 * Helper function to extract error message from unknown error type
 */
function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return String(error);
}

/**
 * Trigger: When user completes onboarding
 * Listens for new documents in user_onboarding/{uid}/responses subcollection
 */
export const onUserOnboardingComplete = functions.firestore
  .document("user_onboarding/{uid}/responses/{responseId}")
  .onCreate(async (snapshot, context) => {
    const uid = context.params.uid;
    const responseId = context.params.responseId;
    const data = snapshot.data();

    // Ignore the "in_progress" document
    if (responseId === "in_progress") {
      functions.logger.info(`Ignoring in_progress document for user ${uid}`);
      return null;
    }

    // Check if onboarding is completed
    if (data.completed === true) {
      functions.logger.info(`User ${uid} completed onboarding (response ${responseId}), generating recommendations`);

      try {
        await generateUserRecommendations(uid, "onboarding_complete");
        functions.logger.info(`Successfully generated recommendations for user ${uid}`);
      } catch (error) {
        functions.logger.error(`Failed to generate recommendations for user ${uid}:`, error);
        throw error;
      }
    }

    return null;
  });

/**
 * Scheduled: Weekly recommendation update
 * Runs every Monday at 3:00 AM UTC
 */
export const weeklyRecommendationsUpdate = functions.pubsub
  .schedule("0 3 * * 1") // Every Monday at 3:00 AM UTC
  .timeZone("UTC")
  .onRun(async (_context) => {
    functions.logger.info("Starting weekly recommendations update");

    const db = admin.firestore();
    let processedCount = 0;
    let errorCount = 0;

    try {
      // Get all users with completed onboarding
      const usersSnapshot = await db.collection("users")
        .where("has_completed_onboarding", "==", true)
        .get();

      functions.logger.info(`Found ${usersSnapshot.size} users with completed onboarding`);

      // Process in batches
      const users = usersSnapshot.docs;
      for (let i = 0; i < users.length; i += BATCH_SIZE) {
        const batch = users.slice(i, i + BATCH_SIZE);

        await Promise.all(
          batch.map(async (userDoc) => {
            try {
              await generateUserRecommendations(userDoc.id, "weekly_cron");
              processedCount++;
            } catch (error) {
              functions.logger.error(`Error processing user ${userDoc.id}:`, error);
              errorCount++;
            }
          })
        );

        functions.logger.info(`Processed batch ${i / BATCH_SIZE + 1}, total: ${processedCount} success, ${errorCount} errors`);
      }

      functions.logger.info(`Weekly update complete: ${processedCount} users processed, ${errorCount} errors`);
    } catch (error) {
      functions.logger.error("Weekly recommendations update failed:", error);
      throw error;
    }

    return null;
  });

/**
 * Callable: Manually regenerate recommendations for a user
 * Can be called from Admin Portal via Firebase SDK
 */
export const regenerateUserRecommendations = functions.https.onCall(
  async (data, context) => {
    // Verify admin authentication
    if (!context.auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "Must be authenticated to regenerate recommendations"
      );
    }

    const uid = data.uid;
    if (!uid || typeof uid !== "string") {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "User ID (uid) is required"
      );
    }

    functions.logger.info(`Manual regeneration requested for user ${uid} by ${context.auth.uid}`);

    try {
      await generateUserRecommendations(uid, "manual");
      return { success: true, message: `Recommendations regenerated for user ${uid}` };
    } catch (error) {
      functions.logger.error(`Manual regeneration failed for user ${uid}:`, error);
      throw new functions.https.HttpsError("internal", `Failed to regenerate recommendations: ${error}`);
    }
  }
);

/**
 * HTTP Endpoint: Manually regenerate recommendations for a user
 * Called via direct HTTP POST from Admin Portal (OraWebApp)
 *
 * URL: https://us-central1-ora-app-f429a.cloudfunctions.net/regenerateUserRecommendationsHttp
 *
 * Request body:
 *   { "uid": "user-id-here" }
 *
 * Response:
 *   { "success": true, "message": "...", "uid": "..." }
 *   or
 *   { "error": "...", "details": "..." }
 */
export const regenerateUserRecommendationsHttp = functions.https.onRequest(
  async (req, res) => {
    // Enable CORS for admin portal
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
    res.set("Access-Control-Allow-Headers", "Content-Type, Authorization");

    // Handle preflight OPTIONS request
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    // Only accept POST requests
    if (req.method !== "POST") {
      res.status(405).json({
        error: "Method not allowed",
        details: "Only POST requests are accepted",
      });
      return;
    }

    try {
      // Parse request body
      const { uid } = req.body;

      if (!uid || typeof uid !== "string") {
        res.status(400).json({
          error: "Invalid request",
          details: "User ID (uid) is required in request body",
        });
        return;
      }

      functions.logger.info(`HTTP: Manual regeneration requested for user ${uid}`);

      // Verify user exists
      const db = admin.firestore();
      const userDoc = await db.collection("users").doc(uid).get();

      if (!userDoc.exists) {
        res.status(404).json({
          error: "User not found",
          details: `No user found with ID: ${uid}`,
        });
        return;
      }

      // Generate recommendations
      await generateUserRecommendations(uid, "manual");

      functions.logger.info(`HTTP: Successfully regenerated recommendations for user ${uid}`);

      res.status(200).json({
        success: true,
        message: `Recommendations regenerated successfully for user ${uid}`,
        uid,
        timestamp: new Date().toISOString(),
      });
    } catch (error: unknown) {
      functions.logger.error("HTTP: Failed to regenerate recommendations:", error);

      const errorMessage = getErrorMessage(error);

      // Determine appropriate status code
      let statusCode = 500;
      let displayMessage = "Failed to regenerate recommendations";

      if (errorMessage.includes("not found")) {
        statusCode = 404;
        displayMessage = errorMessage;
      } else if (errorMessage.includes("no completed onboarding")) {
        statusCode = 400;
        displayMessage = "User has not completed onboarding";
      }

      res.status(statusCode).json({
        error: displayMessage,
        details: errorMessage,
      });
    }
  }
);

/**
 * Main recommendation generation function
 *
 * Algorithm:
 * Score = (60% x Intention Match) + (40% x Experience Level Match)
 *
 * Filtering:
 * 1. Exclude lessons already completed
 * 2. Exclude lessons from active programs
 * 3. Respect experience level (no advanced for beginners)
 * 4. Favor short lessons for time-constrained users
 */
async function generateUserRecommendations(
  uid: string,
  trigger: "onboarding_complete" | "weekly_cron" | "manual"
): Promise<void> {
  const db = admin.firestore();

  // 1. Fetch user onboarding data
  const userDoc = await db.collection("users").doc(uid).get();
  if (!userDoc.exists) {
    throw new Error(`User ${uid} not found`);
  }

  // Fetch latest onboarding response from new structure
  functions.logger.info(`Fetching onboarding responses from user_onboarding/${uid}/responses`);

  const responsesSnapshot = await db.collection("user_onboarding").doc(uid)
    .collection("responses")
    .where("completed", "==", true)
    .orderBy("completedAt", "desc")
    .limit(1)
    .get();

  if (responsesSnapshot.empty) {
    throw new Error(`User ${uid} has no completed onboarding responses in user_onboarding/${uid}/responses`);
  }

  const onboarding = responsesSnapshot.docs[0].data() as UserOnboarding;

  functions.logger.info(`Found onboarding response with ${onboarding.answers?.length || 0} answers for user ${uid}`);

  if (!onboarding.answers || onboarding.answers.length === 0) {
    throw new Error(`User ${uid} onboarding response has no answers to process`);
  }

  // 2. Extract user preferences from answers
  const userIntentions = extractIntentionsFromAnswers(onboarding.answers);
  const userExperienceLevels = extractExperienceLevelsFromAnswers(onboarding.answers);
  const timeCommitment = extractTimeCommitmentFromAnswers(onboarding.answers);

  functions.logger.info(`User ${uid} preferences:`, {
    intentions: userIntentions,
    experienceLevels: userExperienceLevels,
    timeCommitment,
  });

  // 3. Fetch all ready lessons (status = "ready" in Firestore)
  const lessonsSnapshot = await db.collection("lessons")
    .where("status", "==", "ready")
    .get();

  const lessons = lessonsSnapshot.docs.map((doc) => ({
    id: doc.id,
    ...doc.data(),
  } as LessonDocument & { id: string }));

  functions.logger.info(`Found ${lessons.length} published lessons`);

  // 4. Fetch user's completed lessons
  const activitiesSnapshot = await db.collection("user_activities")
    .where("uid", "==", uid)
    .where("progressPercent", ">=", 80) // Consider 80%+ as completed
    .get();

  const completedLessonIds = new Set(
    activitiesSnapshot.docs.map((doc) => doc.data().lessonId)
  );

  // 5. Fetch user's active program lessons
  const userProgramsSnapshot = await db.collection("user_programs")
    .where("uid", "==", uid)
    .where("status", "==", "active")
    .get();

  const activeProgramIds = new Set(
    userProgramsSnapshot.docs.map((doc) => doc.data().programId)
  );

  // Get lessons from active programs to exclude
  const activeProgramLessonIds = new Set<string>();
  if (activeProgramIds.size > 0) {
    const programLessonsSnapshot = await db.collection("lessons")
      .where("program_id", "in", Array.from(activeProgramIds).slice(0, 10)) // Firestore limit
      .get();

    programLessonsSnapshot.docs.forEach((doc) => {
      activeProgramLessonIds.add(doc.id);
    });
  }

  // 6. Score and filter lessons
  const scoredLessons: Array<{ id: string; score: number }> = [];

  for (const lesson of lessons) {
    // Filter: Skip completed lessons
    if (completedLessonIds.has(lesson.id)) {
      continue;
    }

    // Filter: Skip lessons from active programs
    if (activeProgramLessonIds.has(lesson.id)) {
      continue;
    }

    // Calculate score
    const score = calculateLessonScore(
      lesson,
      userIntentions,
      userExperienceLevels,
      timeCommitment
    );

    // Filter: Skip if score is too low (experience level mismatch)
    if (score >= 0.1) {
      scoredLessons.push({ id: lesson.id, score });
    }
  }

  // 7. Sort by score and take top 5
  scoredLessons.sort((a, b) => b.score - a.score);
  const topLessons = scoredLessons.slice(0, MAX_RECOMMENDATIONS);

  functions.logger.info(`Top ${topLessons.length} recommendations for user ${uid}:`, topLessons);

  // 8. Create recommendation document
  const recommendation: UserRecommendation = {
    uid,
    lesson_ids: topLessons.map((l) => l.id),
    scores: Object.fromEntries(topLessons.map((l) => [l.id, l.score])),
    generated_at: Timestamp.now(),
    algorithm_version: ALGORITHM_VERSION,
    based_on: {
      intentions: userIntentions,
      experience_levels: userExperienceLevels,
      time_commitment: timeCommitment,
      completed_lesson_ids: Array.from(completedLessonIds),
    },
    metadata: {
      total_lessons_scored: scoredLessons.length,
      avg_score: scoredLessons.length > 0
        ? scoredLessons.reduce((sum, l) => sum + l.score, 0) / scoredLessons.length
        : 0,
      trigger,
    },
  };

  // 9. Save to new structure: user_onboarding/{uid}/recommendations
  const recommendationId = trigger === "weekly_cron"
    ? new Date().toISOString().split("T")[0] // e.g., "2025-12-08"
    : trigger; // e.g., "onboarding_complete" or "manual"

  await db.collection("user_onboarding")
    .doc(uid)
    .collection("recommendations")
    .doc(recommendationId)
    .set(recommendation);

  // Also save as "latest" for easy access
  await db.collection("user_onboarding")
    .doc(uid)
    .collection("recommendations")
    .doc("latest")
    .set(recommendation);

  functions.logger.info(`Saved recommendations for user ${uid} as ${recommendationId}`);
}

/**
 * Extract user intentions from raw onboarding answers
 * Looks for question IDs: intentions, life_situation
 */
function extractIntentionsFromAnswers(answers: OnboardingAnswer[]): string[] {
  const intentions: string[] = [];

  for (const answer of answers) {
    // Intentions question
    if (answer.questionId === "intentions" && Array.isArray(answer.selectedOptions)) {
      intentions.push(...answer.selectedOptions);
    }
    // Life situation can also indicate intentions
    if (answer.questionId === "life_situation" && Array.isArray(answer.selectedOptions)) {
      intentions.push(...answer.selectedOptions);
    }
  }

  // Normalize to lowercase and unique
  return [...new Set(intentions.map((i: string) => i.toLowerCase().replace(/\s+/g, "_")))];
}

/**
 * Extract experience levels from raw onboarding answers
 * Looks for question ID: practice_levels
 */
function extractExperienceLevelsFromAnswers(answers: OnboardingAnswer[]): Record<string, string> {
  const levels: Record<string, string> = {};

  for (const answer of answers) {
    if (answer.questionId === "practice_levels" && Array.isArray(answer.selectedOptions)) {
      // Parse experience level answers
      // Expected format: "meditation:beginner", "yoga:intermediate", etc.
      for (const option of answer.selectedOptions) {
        const parts = option.split(":");
        if (parts.length === 2) {
          levels[parts[0].toLowerCase()] = parts[1].toLowerCase();
        }
      }
    }
  }

  return levels;
}

/**
 * Extract time commitment from raw onboarding answers
 * Looks for question ID: time_commitment
 */
function extractTimeCommitmentFromAnswers(answers: OnboardingAnswer[]): string {
  for (const answer of answers) {
    if (answer.questionId === "time_commitment" && answer.selectedOptions && answer.selectedOptions.length > 0) {
      return answer.selectedOptions[0];
    }
  }

  return "10-20"; // Default
}


/**
 * Calculate recommendation score for a lesson
 *
 * Formula: Score = (60% x Intention Match) + (40% x Experience Level Match)
 */
function calculateLessonScore(
  lesson: LessonDocument,
  userIntentions: string[],
  userExperienceLevels: Record<string, string>,
  timeCommitment: string
): number {
  let intentionScore = 0;
  let experienceScore = 0;

  const discipline = (lesson.discipline || lesson.category || "").toLowerCase();
  const lessonDifficulty = (lesson.difficulty || "beginner").toLowerCase();
  const durationMinutes = lesson.duration_sec / 60;

  // 1. Calculate intention score (60%)
  let matchingIntentions = 0;
  for (const intention of userIntentions) {
    const relevantDisciplines = INTENTION_TO_DISCIPLINE[intention] || [];
    if (relevantDisciplines.some((d) => discipline.includes(d))) {
      matchingIntentions++;
    }
  }

  if (userIntentions.length > 0) {
    intentionScore = matchingIntentions / userIntentions.length;
  } else {
    // No intentions = neutral score
    intentionScore = 0.5;
  }

  // 2. Calculate experience level score (40%)
  const userLevel = userExperienceLevels[discipline] ||
                   Object.values(userExperienceLevels)[0] ||
                   "beginner";

  const userLevelValue = EXPERIENCE_LEVEL_ORDER[userLevel.toLowerCase()] ?? 0;
  const lessonLevelValue = EXPERIENCE_LEVEL_ORDER[lessonDifficulty] ?? 0;
  const levelDiff = lessonLevelValue - userLevelValue;

  if (levelDiff === 0) {
    // Exact match
    experienceScore = 1.0;
  } else if (levelDiff === -1) {
    // One level below user (easier) - still good
    experienceScore = 0.8;
  } else if (levelDiff === 1) {
    // One level above user (slightly challenging)
    experienceScore = 0.5;
  } else if (levelDiff > 1) {
    // Too advanced - penalize
    experienceScore = 0.0;
  } else {
    // Much easier than user level
    experienceScore = 0.3;
  }

  // 3. Time preference bonus
  let timeBonus = 0;
  if (timeCommitment === "5-10" && durationMinutes <= 10) {
    timeBonus = 0.1; // Bonus for short lessons
  } else if (timeCommitment === "10-20" && durationMinutes <= 20) {
    timeBonus = 0.05;
  }

  // 4. Calculate final score
  const finalScore = (0.6 * intentionScore) + (0.4 * experienceScore) + timeBonus;

  return Math.min(1.0, finalScore); // Cap at 1.0
}
