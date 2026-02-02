# GitHub Issue Management

This directory contains issue templates for the Ora Android application.

## Available Templates

### 🐛 Bug Report (`bug.yml`)
Use this template to report bugs or unexpected behavior in the Android app.

**When to use:**
- App crashes or freezes
- Features not working as expected
- UI rendering issues
- Performance problems
- Data sync issues

**Key fields:**
- Android version and device model
- Network state (online/offline)
- Logcat output
- Steps to reproduce
- Severity and frequency

### 🚀 Feature Request (`feature.yml`)
Use this template to propose new features for the app.

**When to use:**
- Requesting new functionality
- Suggesting UX improvements
- Proposing architecture changes

**Key fields:**
- User story (As a... I want... So that...)
- Acceptance criteria (Gherkin format)
- Technical impacts (UI, ViewModel, Database, etc.)
- Priority and complexity estimates
- Platform scope (Android only, cross-platform, etc.)

### 📋 Technical Specification (`spec.yml`)
Use this template to create detailed technical specifications before implementing features.

**When to use:**
- Before starting complex feature development
- To document architectural decisions
- For AI-assisted implementation planning

**Key fields:**
- Architecture & design patterns
- UI components (Compose)
- Data models (Room entities, Firestore)
- Offline support strategy
- Testing strategy
- Implementation tasks checklist

## Workflow

### Standard Development Flow

1. **Create Feature Request** → Use `feature.yml`
2. **Create Spec** → Use `spec.yml` to detail implementation
3. **Implement** → Follow spec checklist with Claude Code
4. **Report Bugs** → Use `bug.yml` if issues arise

### Example: Adding Custom Meditation Timers

```
1. Create issue: [FEATURE] Custom meditation timer durations
   - Fill in user story, acceptance criteria, priority
   - Label: feature, spec-needed

2. Create spec: [SPEC] Custom meditation timer implementation
   - Detail UI components (TimerPickerBottomSheet)
   - Define data models (TimerPreference entity)
   - Plan offline support (Room + Firestore sync)
   - List implementation tasks

3. Implement using Claude Code:
   - Reference spec issue #123
   - Check off tasks as completed
   - Add commits referencing spec

4. If bugs found during testing:
   - Create [BUG] Timer notification not firing
   - Link to feature/spec issues
```

## AI-Assisted Development

These templates are designed to work seamlessly with **Claude Code**:

### Using Feature Requests with Claude Code

```
User: "Implement the custom timer feature from issue #42"

Claude Code:
1. Reads the feature request
2. Generates a technical spec (or reads existing spec)
3. Creates implementation plan
4. Writes code following spec
5. Runs tests
6. Creates PR referencing issue
```

### Using Specs with Claude Code

```
User: "Follow the spec in issue #43 to implement timers"

Claude Code:
1. Reads the spec
2. Creates TodoWrite checklist from implementation tasks
3. Implements each component (UI, ViewModel, Repository, etc.)
4. Writes unit tests and UI tests
5. Updates spec checklist as tasks complete
```

## Labels

Issues are automatically labeled based on template:

- `bug` + `needs-triage` → Bug reports
- `feature` + `spec-needed` → Feature requests
- `spec` + `documentation` → Technical specs

Additional labels you can add:

- **Priority**: `P0-critical`, `P1-high`, `P2-medium`, `P3-low`
- **Component**: `ui`, `viewmodel`, `repository`, `database`, `firebase`, `offline`
- **Status**: `in-progress`, `blocked`, `ready-for-review`
- **Platform**: `android`, `cross-platform`

## Tips

### Writing Good Bug Reports

- **Always include logcat output** for crashes
- Specify exact Android version and device model
- Note if offline or online when bug occurred
- Include screenshots/screen recording when possible
- Test on emulator if possible to reproduce

### Writing Good Feature Requests

- Use Gherkin format for acceptance criteria
  ```gherkin
  Scenario: User selects custom timer
    Given I am on meditation player screen
    When I tap timer icon
    Then I should see timer picker with options 5, 10, 15, 20, 30 minutes
  ```
- List technical impacts (which components affected)
- Consider offline support implications
- Think about cross-platform needs (iOS, Web Portal)

### Writing Good Specs

- **Be specific** about data models and API contracts
- Include Kotlin code examples for key components
- Plan for offline-first architecture
- Define testing strategy (unit, UI, integration)
- Break down into small, actionable tasks
- Consider performance and security implications

## Related Documentation

- [CLAUDE.md](../CLAUDE.md) - Project overview and architecture
- [OFFLINE_SUPPORT_GUIDE.md](../docs/OFFLINE_SUPPORT_GUIDE.md) - Offline-first patterns
- [FIRESTORE_KOTLIN_MAPPING_GUIDE.md](../docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md) - Data modeling

## Questions?

- Check [Documentation](https://github.com/Chrisdesmurger/Ora/tree/main/docs)
- Ask in [Discussions](https://github.com/Chrisdesmurger/Ora/discussions)
- Report security issues [privately](https://github.com/Chrisdesmurger/Ora/security/advisories/new)
