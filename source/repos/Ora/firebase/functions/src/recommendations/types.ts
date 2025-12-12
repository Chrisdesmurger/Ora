/**
 * Ora Wellbeing - Recommendation Types
 * TypeScript interfaces for the recommendation system
 */

import { Timestamp } from "firebase-admin/firestore";

/**
 * Mapping of user intentions to relevant disciplines
 * Used for scoring recommendations
 */
export const INTENTION_TO_DISCIPLINE: Record<string, string[]> = {
  "reduce_stress": ["meditation", "respiration", "massage"],
  "improve_sleep": ["meditation", "respiration", "massage"],
  "increase_energy": ["yoga", "respiration"],
  "emotional_balance": ["meditation", "respiration"],
  "physical_wellness": ["yoga", "massage"],
  "develop_mindfulness": ["meditation"],
  "personal_growth": ["meditation", "yoga"],
  // French versions (from onboarding)
  "reduire_stress": ["meditation", "respiration", "massage"],
  "ameliorer_sommeil": ["meditation", "respiration", "massage"],
  "augmenter_energie": ["yoga", "respiration"],
  "equilibre_emotionnel": ["meditation", "respiration"],
  "bien_etre_physique": ["yoga", "massage"],
  "developper_pleine_conscience": ["meditation"],
  "croissance_personnelle": ["meditation", "yoga"],
};

/**
 * Experience level order for comparison
 */
export const EXPERIENCE_LEVEL_ORDER: Record<string, number> = {
  "beginner": 0,
  "debutant": 0,
  "intermediate": 1,
  "intermediaire": 1,
  "advanced": 2,
  "avance": 2,
  "expert": 3,
};

/**
 * User onboarding answer structure
 */
export interface OnboardingAnswer {
  questionId: string;
  selectedOptions: string[];
  textAnswer?: string;
  answeredAt?: Timestamp;
}

/**
 * User onboarding response (stored in users/{uid}.onboarding)
 */
export interface UserOnboarding {
  uid: string;
  configVersion: string;
  completed: boolean;
  completedAt?: Timestamp;
  startedAt?: Timestamp;
  answers: OnboardingAnswer[];
  // Parsed fields
  goals?: string[];
  mainGoal?: string;
  experienceLevels?: Record<string, string>;
  dailyTimeCommitment?: string;
  preferredTimes?: string[];
  contentPreferences?: string[];
  practiceStyle?: string;
  challenges?: string[];
  supportPreferences?: string[];
}

/**
 * Lesson document structure (from Firestore "lessons" collection)
 */
export interface LessonDocument {
  id?: string;
  title: string;
  description?: string;
  discipline: string;
  category?: string;
  difficulty?: string;
  duration_sec: number;
  status: string;
  program_id?: string;
  order?: number;
  renditions?: Record<string, RenditionInfo>;
  cover_image_url?: string;
  created_at?: Timestamp;
  updated_at?: Timestamp;
}

export interface RenditionInfo {
  url: string;
  width?: number;
  height?: number;
  bitrate?: number;
}

/**
 * User activity document (to check completed lessons)
 */
export interface UserActivity {
  uid: string;
  lessonId: string;
  completedAt: Timestamp;
  progressPercent: number;
  durationWatched: number;
}

/**
 * User program enrollment
 */
export interface UserProgram {
  uid: string;
  programId: string;
  currentDay: number;
  totalDays: number;
  status: string;
}

/**
 * Context used to generate recommendations
 */
export interface RecommendationContext {
  intentions: string[];
  experience_levels: Record<string, string>;
  time_commitment: string;
  completed_lesson_ids: string[];
}

/**
 * Recommendation metadata
 */
export interface RecommendationMetadata {
  total_lessons_scored: number;
  avg_score: number;
  trigger: "onboarding_complete" | "weekly_cron" | "manual";
}

/**
 * User recommendation document (stored in users/{uid}/recommendations/{id})
 */
export interface UserRecommendation {
  uid: string;
  lesson_ids: string[];
  scores: Record<string, number>;
  generated_at: Timestamp;
  algorithm_version: string;
  based_on: RecommendationContext;
  metadata: RecommendationMetadata;
}
