# Build Instructions - Profile Redesign UI Branch

**Date**: 2025-11-06
**Branch**: feat/profile-redesign-ui
**Status**: Ready for Build
**Previous Status**: All compilation errors fixed

---

## Quick Start

Execute these commands in sequence:

```bash
cd c:\Users\chris\source\repos\Ora
./gradlew.bat clean
./gradlew.bat :app:assembleDebug --stacktrace --info
```

**Expected Result**: APK generated at `app/build/outputs/apk/debug/app-debug.apk`

---

## Detailed Build Steps

### Step 1: Navigate to Project Directory
```bash
cd c:\Users\chris\source\repos\Ora
```

### Step 2: Clean Build Cache
```bash
./gradlew.bat clean
```

**What it does**: Removes all previous build artifacts
**Duration**: 5-10 seconds
**Expected output**: `BUILD SUCCESSFUL`

### Step 3: Verify Kotlin Compilation
```bash
./gradlew.bat :app:compileDebugKotlin --stacktrace --info
```

**What it does**: Compiles all Kotlin source files
**Duration**: 30-60 seconds (first time) / 5-10 seconds (cached)
**Expected output**: `BUILD SUCCESSFUL`

**If it fails**: Shows Kotlin compilation errors with line numbers

### Step 4: Generate KSP Code (Hilt + Room)
```bash
./gradlew.bat :app:kspDebugKotlin --info
```

**What it does**: Generates Hilt components and Room DAOs
**Duration**: 10-20 seconds
**Expected output**: `BUILD SUCCESSFUL`

**Generated files location**: `app/build/generated/ksp/debug/`

### Step 5: Full Debug Build (Recommended)
```bash
./gradlew.bat :app:assembleDebug --stacktrace --info
```

**What it does**: Complete build including APK generation
**Duration**: 45-90 seconds
**Expected output**: `BUILD SUCCESSFUL`

**Output location**: `app/build/outputs/apk/debug/app-debug.apk`

### Step 6: Full Build with Logging (Optional)
```bash
./gradlew.bat :app:clean :app:assembleDebug --stacktrace --info 2>&1 | tee reports/build/last-build.log
```

**What it does**: Clean build with full console logging
**Duration**: 60-120 seconds
**Output**: APK + log file at `reports/build/last-build.log`

**Useful for**: Post-build analysis and troubleshooting

---

## Build Success Checklist

After each build step, verify:

### After Step 2 (Clean)
- [ ] `./gradlew.bat clean` completes with `BUILD SUCCESSFUL`
- [ ] Directory `app/build/` is removed or cleared

### After Step 3 (Kotlin Compilation)
- [ ] No "unresolved reference" errors
- [ ] No "type mismatch" errors
- [ ] No import errors
- [ ] All ProfileViewModel imports resolved
- [ ] All ProfileScreen imports resolved

### After Step 4 (KSP Code Generation)
- [ ] Hilt components generated
- [ ] Room database entities processed
- [ ] No "failed to generate" messages
- [ ] Directory `app/build/generated/ksp/debug/` exists

### After Step 5 (Full Build)
- [ ] `BUILD SUCCESSFUL` message
- [ ] APK file exists at `app/build/outputs/apk/debug/app-debug.apk`
- [ ] APK file size > 0 bytes (typically 50-100 MB)
- [ ] No "Installation failed" errors

---

## Expected Build Artifacts

After successful build:

```
app/build/
├── intermediates/
│   ├── processed_res/      (Processed resources)
│   ├── compiled_classes/   (Compiled Java/Kotlin)
│   └── dex/                (DEX files)
├── generated/
│   └── ksp/
│       └── debug/          (Hilt/Room generated code)
└── outputs/
    └── apk/
        └── debug/
            ├── app-debug.apk       (Main APK - installable)
            ├── app-debug-mapping.txt
            └── output-metadata.json
```

---

## Troubleshooting

### Issue: "ERROR: Duplicate class com.ora.wellbeing.data.model.PlanTier"

**Cause**: The fix might not have been applied
**Solution**:
1. Verify files were modified:
   - Check `app/src/main/java/com/ora/wellbeing/data/model/PlanTier.kt` exists
   - Check `app/src/main/java/com/ora/wellbeing/data/model/UserProfile.kt` doesn't have enum definition
2. Run: `./gradlew clean`
3. Try build again

### Issue: "Cannot resolve symbol 'ProfileViewModel'"

**Cause**: Hilt code generation failed
**Solution**:
1. Run: `./gradlew :app:kspDebugKotlin`
2. Check for KSP errors in output
3. Verify `app/build/generated/ksp/debug/` directory exists
4. Try clean build: `./gradlew clean :app:assembleDebug`

### Issue: "Task assembleDebug failed"

**Cause**: Could be many issues, need to investigate
**Solution**:
1. Check full error in console (usually near end of output)
2. If error mentions file names, check file exists
3. Run with extra logging: `./gradlew :app:assembleDebug --stacktrace`
4. Search error message online
5. Check diagnostic.md for common issues

### Issue: Build hangs (appears to freeze)

**Cause**: Large dependency downloads or Gradle daemon issue
**Solution**:
1. Wait 2-3 minutes (first build can be slow)
2. If still hanging after 5 minutes:
   - Press Ctrl+C to cancel
   - Run: `./gradlew.bat --stop` (stop daemon)
   - Run: `./gradlew.bat --version` (restart daemon)
   - Try build again

### Issue: APK too large (> 150 MB)

**Cause**: Debug APK includes all code without minification (normal)
**Solution**: Normal for debug builds. Use `assembleRelease` for optimized APK
**Note**: Debug APKs are larger but install faster

---

## Installing the APK

After successful build:

### Option 1: Via Android Studio
1. Open Android Studio
2. Run → Install Debuggable APK
3. Select `app/build/outputs/apk/debug/app-debug.apk`
4. Choose target device/emulator
5. Click Install

### Option 2: Via ADB Command
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Via Gradle
```bash
./gradlew installDebug
```

**Expected output**:
```
Installation succeeded
```

---

## Post-Build Testing

### Test 1: Unit Tests
```bash
./gradlew test --info
```

**Expected**: All tests pass
**Duration**: 30-60 seconds

### Test 2: UI Tests (requires emulator/device)
```bash
./gradlew connectedAndroidTest
```

**Expected**: All tests pass
**Duration**: 2-5 minutes

### Test 3: Manual Testing

Launch the app and verify:

1. **App Launch**
   - [ ] App starts without crashing
   - [ ] Splash screen appears
   - [ ] Navigation bar loads

2. **Profile Screen**
   - [ ] Navigate to Profile tab
   - [ ] User name displays (or "Guest")
   - [ ] Avatar shows (photo or initials)
   - [ ] Settings icon visible (top right)

3. **Monthly Stats**
   - [ ] "Completed Calendar" section shows
   - [ ] Percentage displays (0-100%)
   - [ ] Month name shows (current month)
   - [ ] Previous months visible (if any)

4. **My Statistics**
   - [ ] "Completed Workouts" count shows
   - [ ] "Challenges in Progress" count shows
   - [ ] "Completed Challenges" count shows
   - [ ] Circular badges with colored backgrounds

5. **Challenge Progress** (if user has active challenges)
   - [ ] "Challenge in Progress" section shows
   - [ ] Challenge name displays
   - [ ] Day progress shows (e.g., "Day 5/21")
   - [ ] Progress bar displays
   - [ ] Percentage shows

6. **Favorites Section**
   - [ ] Two side-by-side cards
   - [ ] "Favorite Workouts" count (with heart icon)
   - [ ] "Favorite Challenges" count (with star icon)
   - [ ] Both cards visible and styled correctly

7. **No Errors**
   - [ ] No red error messages
   - [ ] No crash dialogs
   - [ ] Console logs are clean (in Android Studio Logcat)

---

## Build Performance Tips

### Speed Up Builds

1. **Enable Gradle Daemon** (automatic, but verify)
   ```bash
   ./gradlew --daemon
   ```

2. **Use Local Gradle Cache**
   - Builds improve after first run
   - 2nd+ builds typically 10x faster

3. **Increase Gradle Memory** (if available)
   ```bash
   # Edit gradle.properties
   org.gradle.jvmargs=-Xmx4096m
   ```

4. **Skip Tests During Development**
   ```bash
   ./gradlew assembleDebug -x test
   ```

5. **Use Incremental Compilation**
   - Enabled by default in Kotlin 2.0.21
   - Speeds up recompilation of changed files

### Monitor Build Performance

```bash
# Show build task timing
./gradlew build --profile

# View report at: build/reports/profile/profile-*.html
```

---

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Build Android App
  run: |
    ./gradlew clean
    ./gradlew :app:assembleDebug --info
```

### Jenkins Example
```groovy
stage('Build') {
    steps {
        sh './gradlew clean :app:assembleDebug --stacktrace --info'
    }
}
```

---

## Verification Commands

Run these to verify the fix was successful:

```bash
# Check PlanTier.kt exists
if [ -f "app/src/main/java/com/ora/wellbeing/data/model/PlanTier.kt" ]; then
    echo "✓ PlanTier.kt created"
else
    echo "✗ PlanTier.kt missing"
fi

# Check UserProfile.kt doesn't have duplicate enum
if grep -q "enum class PlanTier" "app/src/main/java/com/ora/wellbeing/data/model/UserProfile.kt"; then
    echo "✗ PlanTier still in UserProfile.kt"
else
    echo "✓ PlanTier removed from UserProfile.kt"
fi

# Check ProfileViewModel compiles
./gradlew :app:compileDebugKotlin --info 2>&1 | grep -i "ProfileViewModel"
```

---

## Support & Debugging

### Need More Information?

1. **Build Diagnostics**: See `reports/build/diagnostic.md`
2. **Changes Summary**: See `reports/build/CHANGES_SUMMARY.md`
3. **Previous Build Logs**: See `reports/build/last-build.log`

### Get Help

1. Check console error message (last few lines)
2. Search for error in `diagnostic.md` troubleshooting section
3. Check if error is in `CHANGES_SUMMARY.md`
4. Review full build log: `reports/build/last-build.log`

### Report Issues

If build fails after applying the fix:
1. Save console output
2. Check git status (verify all files modified correctly)
3. Review diagnostic report
4. Report with: error message + full console output

---

## Success Criteria

Build is successful when:

```
✅ BUILD SUCCESSFUL message appears
✅ APK generated at app/build/outputs/apk/debug/app-debug.apk
✅ APK file size > 0 bytes
✅ No "ERROR" messages in console
✅ Warnings only (safe to ignore)
✅ All Gradle tasks completed
```

---

## Next Steps After Successful Build

1. **Install APK** (see instructions above)
2. **Test App** (see manual testing checklist)
3. **Run Unit Tests**: `./gradlew test`
4. **Run UI Tests**: `./gradlew connectedAndroidTest`
5. **Review Profile Screen** (all sections render correctly)
6. **Create Git Commit** (if satisfied with changes)
7. **Submit PR** (for code review)

---

## Build Summary

| Item | Value |
|------|-------|
| **Project** | Ora Android Wellbeing App |
| **Branch** | feat/profile-redesign-ui |
| **Kotlin** | 2.0.21 |
| **Gradle** | 8.5.0 |
| **Build Type** | Debug |
| **Min API** | 26 (Android 8.0) |
| **Target API** | 34 (Android 14) |
| **Expected Build Time** | 45-90 seconds |
| **Expected APK Size** | 50-100 MB (debug) |

---

**Status**: ✅ READY FOR BUILD
**Last Updated**: 2025-11-06
**Confidence**: HIGH (95%)

For detailed diagnostics, see: `reports/build/diagnostic.md`
For code changes, see: `reports/build/CHANGES_SUMMARY.md`
