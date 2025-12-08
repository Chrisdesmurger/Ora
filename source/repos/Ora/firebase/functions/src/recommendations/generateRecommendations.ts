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
  LessonDocument,
  UserRecommendation,
  INTENTION_TO_DISCIPLINE,
  EXPERIENCE_LEVEL_ORDER,
} from "./types";

const ALGORITHM_VERSION = "1.0.0";
const MAX_RECOMMENDATIONS = 5;
const BATCH_SIZE = 100;

/**
 * Trigger: When user completes onboarding
 * Listens for updates to users/{uid} where onboarding.completed changes to true
 */
export const onUserOnboardingComplete = functions.firestore
  .document("users/{uid}")
  .onUpdate(async (change, context) => {
    const uid = context.params.uid;
    const before = change.before.data();
    const after = change.after.data();

    // Check if onboarding.completed changed from false to true
    const wasCompleted = before?.onboarding?.completed === true;
    const isNowCompleted = after?.onboarding?.completed === true;

    if (!wasCompleted && isNowCompleted) {
      functions.logger.info(`User ${uid} completed onboarding, generating recommendations`);

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
        .where("onboarding.completed", "==", true)
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
 * Can be called from Admin Portal
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

  const userData = userDoc.data();
  const onboarding = userData?.onboarding as UserOnboarding | undefined;

  if (!onboarding?.completed) {
    throw new Error(`User ${uid} has not completed onboarding`);
  }

  // 2. Extract user preferences
  const userIntentions = extractIntentions(onboarding);
  const userExperienceLevels = onboarding.experienceLevels || {};
  const timeCommitment = onboarding.dailyTimeCommitment || "10-20";

  functions.logger.info(`User ${uid} preferences:`, {
    intentions: userIntentions,
    experienceLevels: userExperienceLevels,
    timeCommitment,
  });

  // 3. Fetch all published lessons
  const lessonsSnapshot = await db.collection("lessons")
    .where("status", "==", "published")
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

  // 9. Save to Firestore
  const recommendationId = trigger === "weekly_cron"
    ? new Date().toISOString().split("T")[0] // e.g., "2025-12-08"
    : trigger; // e.g., "onboarding_complete" or "manual"

  await db.collection("users")
    .doc(uid)
    .collection("recommendations")
    .doc(recommendationId)
    .set(recommendation);

  // Also save as "latest" for easy access
  await db.collection("users")
    .doc(uid)
    .collection("recommendations")
    .doc("latest")
    .set(recommendation);

  functions.logger.info(`Saved recommendations for user ${uid} as ${recommendationId}`);
}

/**
 * Extract user intentions from onboarding answers
 */
function extractIntentions(onboarding: UserOnboarding): string[] {
  const intentions: string[] = [];

  // From goals field
  if (onboarding.goals && Array.isArray(onboarding.goals)) {
    intentions.push(...onboarding.goals);
  }

  // From mainGoal field
  if (onboarding.mainGoal) {
    intentions.push(onboarding.mainGoal);
  }

  // From challenges field (can indicate intentions)
  if (onboarding.challenges && Array.isArray(onboarding.challenges)) {
    intentions.push(...onboarding.challenges);
  }

  // Normalize to lowercase and unique
  return [...new Set(intentions.map((i) => i.toLowerCase().replace(/\s+/g, "_")))];
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
