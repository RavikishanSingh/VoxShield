# Dashboard UI Bug Fixes and Dark Theme Polishing

Fixing hardcoded light-theme colors in `DashboardScreen.kt` to ensure perfect contrast and aesthetics in dark mode, and adding an empty state for call logs.

## User Review Required

> [!NOTE]
> This fix aligns the security status and risk level badge colors with the dark theme established in `MainActivity.kt`, replacing hardcoded light pastel backgrounds with dark-mode optimized semantic colors.

## Proposed Changes

### UI Components

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/RAVI KISHAN SINGH/AndroidStudioProjects/SIH2026/app/src/main/java/com/example/sih_2026/ui/DashboardScreen.kt)
- Update security status card background and text colors to be dark-theme compatible (`0xFF065F46` / `0xFF6EE7B7`).
- Update risk level badge colors (`CRITICAL`, `HIGH`, `LOW`/`SECURE`) for proper dark theme contrast.
- Add an empty state placeholder when `callLogs` is empty.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify successful compilation.

### Manual Verification
- Launch the app, navigate to the Security Dashboard, and verify telemetry stats and call logs display correctly with high contrast and zero visual bugs.
