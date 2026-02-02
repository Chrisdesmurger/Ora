# Quick Start - Testing Onboarding Responses Feature

**Feature**: Dedicated Firestore collection for onboarding responses
**Issue**: #15
**Status**: Ready for manual testing

## ⚡ Quick Start (5 minutes)

### Step 1: Deploy & Build (2 min)

```bash
# Already done - Firestore rules deployed ✅
# Build Android app
cd /c/Users/chris/source/repos/Ora
./gradlew.bat clean assembleDebug installDebug
```

### Step 2: Test Dual Write (2 min)

1. Launch Ora app on emulator/device
2. Complete onboarding questionnaire
3. Open Firebase Console: https://console.firebase.google.com/project/ora-wellbeing/firestore
4. **Verify both collections**:

   **Collection 1**: `users/{your-uid}`
   - Click on your user document
   - Check field `onboarding` exists
   - Expand to see: `config_version`, `completed`, `started_at`, `answers`

   **Collection 2**: `user_onboarding_responses/{your-uid}/responses/{config-version}`
   - Navigate to `user_onboarding_responses` collection
   - Find your UID document
   - Open subcollection `responses`
   - Check document with config version ID
   - Verify same data with snake_case fields

**Expected**: ✅ Both collections contain identical data

### Step 3: Test Web Admin API (1 min)

```bash
cd /c/Users/chris/source/repos/OraWebApp
npm run dev
```

1. Open: http://localhost:3000
2. Login as admin
3. Navigate to: `/admin/onboarding`
4. Click on the active configuration
5. Open DevTools > Network
6. Check API calls:
   - `GET /api/admin/onboarding/[id]/responses`
   - `GET /api/admin/onboarding/[id]/analytics`

**Expected**: ✅ Data loads without errors

---

## 🔍 What to Check

### ✅ Success Criteria

1. **Dual Write Works**:
   - Data appears in `users/{uid}.onboarding` (nested field)
   - Data appears in `user_onboarding_responses/{uid}/responses/{version}` (dedicated collection)
   - Data is identical in both locations

2. **Field Mapping Correct**:
   - `configVersion` → `config_version`
   - `startedAt` → `started_at`
   - `completedAt` → `completed_at`
   - `questionId` → `question_id`
   - `selectedOptions` → `selected_options`
   - `textAnswer` → `text_answer`

3. **Web Admin Works**:
   - Responses page loads
   - Analytics calculates correctly
   - CSV export downloads
   - JSON export downloads

4. **Security Rules Work**:
   - Can only read your own responses
   - Cannot read other users' responses

---

## 🐛 Troubleshooting

### Issue: Data not appearing in dedicated collection

**Check**:
1. Android app logs: `adb logcat | grep -i "OnboardingRepository"`
2. Look for: "Saving onboarding response for user X (dual write)"
3. Look for: "Response saved successfully to both locations"

**Solution**:
- If you see error in logs, check Firestore rules in Console
- Verify `@PropertyName` annotations in UserOnboardingResponse.kt

### Issue: Web Admin API errors

**Check**:
1. Browser DevTools > Console
2. Browser DevTools > Network > Response tab
3. Check error message

**Solution**:
- If 404: Config ID doesn't exist
- If 403: Authentication issue
- If 500: Check server logs in terminal

### Issue: CSV export empty

**Check**:
1. Verify responses exist in Firestore
2. Check browser Network tab for API call
3. Check response data

**Solution**:
- Complete at least one onboarding on Android first
- Verify data in Firebase Console

---

## 📊 Quick Validation Checklist

Run through this in 5 minutes:

- [ ] Android app installs successfully
- [ ] Can complete onboarding flow
- [ ] Data appears in `users/{uid}.onboarding`
- [ ] Data appears in `user_onboarding_responses/{uid}/responses/{version}`
- [ ] Web Admin loads responses page
- [ ] Analytics show correct counts
- [ ] CSV export downloads

**If all checked**: ✅ Ready to merge!

---

## 🚀 After Testing

### If Tests Pass ✅

1. **Merge Android**:
   ```bash
   cd /c/Users/chris/source/repos/Ora
   git checkout feat/user-onboarding
   git merge fix/onboarding-scroll-crash
   git push
   ```

2. **Merge Web Admin**:
   - Go to: https://github.com/Chrisdesmurger/OraWebApp/pull/52
   - Click "Merge pull request"

3. **Update Issue**:
   - Go to: https://github.com/Chrisdesmurger/Ora/issues/15
   - Add comment: "✅ Phase 2 complete. All tests passed."
   - Move to "Phase 3: Deployment"

### If Tests Fail ❌

1. **Document the failure**:
   - Which test failed?
   - What was the error message?
   - Screenshots?

2. **Report**:
   - Add comment to Issue #15
   - Ping for help if needed

---

## 📚 Full Documentation

- **Complete Test Plan**: [ONBOARDING_RESPONSES_TEST_PLAN.md](./ONBOARDING_RESPONSES_TEST_PLAN.md)
- **Validation Summary**: [PHASE_2_VALIDATION_SUMMARY.md](./PHASE_2_VALIDATION_SUMMARY.md)
- **GitHub Issue**: #15

---

## 💡 Tips

1. **Use Emulator Snapshot**: Save state after login to speed up testing
2. **Use Same Test User**: Easier to find data in Firebase Console
3. **Keep Firebase Console Open**: Refresh to see dual write in real-time
4. **Use Network Tab**: See API calls and responses in browser DevTools

---

**Time Estimate**: 5-10 minutes for basic validation
**Full Test Suite**: 30-45 minutes (optional, see ONBOARDING_RESPONSES_TEST_PLAN.md)

---

**Ready?** Start with Step 1! 🚀
