/**
 * Ora Wellbeing - Firebase Cloud Functions
 * Entry point for all Cloud Functions
 */

import * as admin from "firebase-admin";

// Initialize Firebase Admin SDK
admin.initializeApp();

// Export recommendation functions
export {
  onUserOnboardingComplete,
  weeklyRecommendationsUpdate,
  regenerateUserRecommendations,
} from "./recommendations/generateRecommendations";
