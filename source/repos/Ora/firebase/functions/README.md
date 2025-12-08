# Ora Wellbeing - Firebase Cloud Functions

This directory contains the Firebase Cloud Functions for the Ora Wellbeing app.

## Setup

### Prerequisites

- Node.js 18+
- Firebase CLI installed (`npm install -g firebase-tools`)
- Firebase project configured

### Installation

```bash
cd firebase/functions
npm install
```

### Build

```bash
npm run build
```

### Deploy

```bash
npm run deploy
# or
firebase deploy --only functions
```

## Functions

### Recommendation System

#### `onUserOnboardingComplete`
- **Trigger**: Firestore document update on `users/{uid}`
- **Purpose**: Generates personalized recommendations when user completes onboarding
- **Condition**: Fires when `onboarding.completed` changes from `false` to `true`

#### `weeklyRecommendationsUpdate`
- **Trigger**: Cloud Scheduler (CRON)
- **Schedule**: Every Monday at 3:00 AM UTC (`0 3 * * 1`)
- **Purpose**: Updates recommendations for all users with completed onboarding
- **Batch Size**: 100 users per batch

#### `regenerateUserRecommendations`
- **Trigger**: HTTPS Callable
- **Purpose**: Manually regenerate recommendations for a specific user
- **Authentication**: Required
- **Parameters**: `{ uid: string }`

## Algorithm

### Scoring Formula

```
Score = (60% x Intention Match) + (40% x Experience Level Match)
```

### Intention Matching

User intentions from onboarding are mapped to disciplines:

| Intention | Relevant Disciplines |
|-----------|---------------------|
| reduce_stress | meditation, respiration, massage |
| improve_sleep | meditation, respiration, massage |
| increase_energy | yoga, respiration |
| emotional_balance | meditation, respiration |
| physical_wellness | yoga, massage |
| develop_mindfulness | meditation |
| personal_growth | meditation, yoga |

### Experience Level Matching

| Level Difference | Score |
|------------------|-------|
| Exact match | 1.0 |
| One below user | 0.8 |
| One above user | 0.5 |
| Two+ above user | 0.0 |
| Much easier | 0.3 |

### Filtering Rules

1. Exclude lessons already completed (80%+ progress)
2. Exclude lessons from active programs
3. Respect experience level (no advanced for beginners)
4. Favor short lessons for time-constrained users

### Time Preference Bonus

- `time_commitment = "5-10"` + lesson <= 10 min: +0.1
- `time_commitment = "10-20"` + lesson <= 20 min: +0.05

## Firestore Structure

### Input: User Onboarding

```
users/{uid}
  onboarding:
    completed: boolean
    goals: string[]
    mainGoal: string
    experienceLevels: { discipline: level }
    dailyTimeCommitment: string
    ...
```

### Output: Recommendations

```
users/{uid}/recommendations/{id}
  uid: string
  lesson_ids: string[]
  scores: { lessonId: number }
  generated_at: Timestamp
  algorithm_version: string
  based_on:
    intentions: string[]
    experience_levels: { discipline: level }
    time_commitment: string
    completed_lesson_ids: string[]
  metadata:
    total_lessons_scored: number
    avg_score: number
    trigger: "onboarding_complete" | "weekly_cron" | "manual"
```

## Testing

### Local Testing with Emulator

```bash
npm run serve
```

### Unit Tests

```bash
npm run test
```

## Troubleshooting

### Common Issues

1. **Function not triggering**: Ensure Firestore rules allow the function to read/write
2. **CRON not running**: Check Cloud Scheduler console for errors
3. **Timeout errors**: Increase function timeout or reduce batch size

### Logs

```bash
npm run logs
# or
firebase functions:log
```

## Version History

- **1.0.0** (2025-12-08): Initial release with onboarding trigger and weekly CRON
