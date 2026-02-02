# Daily Journal - User Guide

## Overview

The Daily Journal feature allows users to capture their day in a comprehensive and structured way, tracking mood, gratitudes, accomplishments, improvements, and learnings.

## Features

### 1. Mood Tracking
Select from 4 mood types to describe your day:
- 😊 **Joyeux (Happy)** - Great day, feeling positive
- 😐 **Neutre (Neutral)** - Average day, balanced mood
- 😕 **Triste (Sad)** - Challenging day, feeling down
- 😠 **Frustré (Frustrated)** - Difficult day, feeling stressed

### 2. Quick Summary
**Short Note** (Max 200 characters)
- Brief one-liner about your day
- Example: "Belle journée ensoleillée, productive au travail"

### 3. Daily Story
**Detailed Narrative** (Max 2000 characters)
- Write in detail about your day
- What happened, how you felt, what you experienced
- Free-form journaling space

### 4. Gratitudes
**3 Things You're Grateful For**
- Each gratitude has a colored background (pink, peach, mint)
- Focus on the positive aspects of your day
- Examples: "Ma famille", "Le beau temps", "Mon café du matin"

### 5. Accomplishments
**What You Achieved Today**
- Unlimited list (add as many as you want)
- Celebrate your wins, big and small
- Examples: "Terminé le projet X", "30 min de yoga", "Appelé un ami"

### 6. Improvements
**3 Areas to Improve Tomorrow**
- What could you do better?
- Goals for the next day
- Examples: "Dormir plus tôt", "Boire plus d'eau", "Être plus patient"

### 7. Learnings
**What You Learned Today** (Max 1000 characters)
- Key insights or lessons
- New skills or knowledge acquired
- Includes "Remind me tomorrow" checkbox for important learnings

## How to Use

### Creating a New Entry

1. **Navigate to Journal Tab**
   - Tap the "Journal" icon in the bottom navigation

2. **Start New Entry**
   - Tap the "+" button in the top-right corner
   - Or tap "Commencer" if you haven't written today

3. **Fill Out Sections**
   - **Mood** (Required): Select one of the 4 emojis
   - **Short Note** (Optional): Quick summary
   - **Daily Story** (Optional): Detailed narrative
   - **Gratitudes** (Optional): Up to 3 items
   - **Accomplishments** (Optional): Add/remove as needed
   - **Improvements** (Optional): Up to 3 items
   - **Learnings** (Optional): Text + reminder checkbox

4. **Save Entry**
   - Tap "Enregistrer" button at the bottom
   - Entry is saved to Firestore immediately

### Editing an Existing Entry

1. **From Journal Screen**
   - Tap "Modifier" next to today's entry
   - Or tap on the entry in "Entrées récentes"

2. **From Calendar View**
   - Tap the day you want to edit
   - Entry opens in edit mode

3. **Make Changes**
   - Update any section
   - Tap "Enregistrer" to save

4. **Delete Entry** (Optional)
   - Tap the trash icon in the top-right
   - Confirms deletion

### Viewing Calendar

1. **Open Calendar**
   - From Journal screen, tap the history icon (clock)

2. **Navigate Months**
   - Use ← → arrows to change months
   - Current month highlighted
   - Cannot navigate to future months

3. **View Entries**
   - Days with entries show mood emoji
   - Days with entries have colored background
   - Current day has a border
   - Tap any day to view/edit entry

4. **Month Statistics**
   - Total entries this month
   - Days in month
   - Completion percentage

## Tips & Best Practices

### When to Write

**Morning**
- Reflect on yesterday before starting the new day
- Review your improvements and learnings

**Evening**
- Capture the day while it's fresh
- Better recall of details and emotions

**Anytime**
- No strict rules - write when you feel like it
- Even partial entries are valuable

### What to Write

**Be Honest**
- Write authentically about your feelings
- Don't filter or judge yourself
- This is your private space

**Be Specific**
- Instead of "Good day", write "Productive day at work, finished the design mockups"
- Instead of "Family", write "Dinner with mom, she told funny stories"

**Focus on Growth**
- Use improvements to set actionable goals
- Track patterns in your mood over time
- Celebrate progress, not perfection

### Privacy

**Your Data is Secure**
- Entries are stored in Firestore
- UID-based isolation (only you can access)
- End-to-end encryption in transit
- Firestore security rules enforce privacy

## Keyboard Shortcuts

**Android**
- **Tab**: Move to next field
- **Shift+Tab**: Move to previous field
- **Enter**: New line in multiline fields

## Character Limits

| Field | Max Length | Purpose |
|-------|-----------|---------|
| Short Note | 200 chars | Quick summary |
| Daily Story | 2000 chars | Detailed narrative |
| Learnings | 1000 chars | Key insights |
| Gratitudes | 3 items | Focus on top 3 |
| Improvements | 3 items | Manageable goals |
| Accomplishments | Unlimited | Celebrate all wins |

## FAQ

### Q: Can I write multiple entries per day?
**A**: No, one entry per day. You can edit it as many times as you want.

### Q: What if I miss a day?
**A**: No problem! You can go back and fill in past days using the calendar.

### Q: Can I export my journal?
**A**: Not yet - this feature is coming in Phase 2.

### Q: Are my entries backed up?
**A**: Yes, all entries are stored in Firestore and automatically synced.

### Q: Can I use the journal offline?
**A**: Not yet - offline support is coming in Phase 2 with Room database.

### Q: What happens to my "remind me tomorrow" learnings?
**A**: Notifications are not yet implemented - coming in Phase 2.

### Q: Can I search my past entries?
**A**: Not yet - search functionality is coming in Phase 2.

### Q: Can I add photos to my entries?
**A**: Not yet - photo attachments are coming in Phase 2.

## Troubleshooting

### Entry won't save
- ✅ Check internet connection
- ✅ Ensure mood is selected (required field)
- ✅ Check character limits aren't exceeded
- ✅ Try closing and reopening the app

### Calendar shows old data
- ✅ Pull to refresh (swipe down)
- ✅ Navigate to another month and back
- ✅ Close and reopen the app

### Can't see today's entry
- ✅ Check you're logged in
- ✅ Verify correct date is selected
- ✅ Check Firestore connectivity

## Support

For issues or feedback, contact: [support@ora-wellbeing.com](mailto:support@ora-wellbeing.com)

---

**Version**: 1.0.0
**Last Updated**: 2025-10-12
**Platform**: Android
