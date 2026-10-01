# Cluely Keyboard — Stealth + Style Memory (Axion build)

## What changed

### 1. Stealth context (no screenshot detection)
- Primary path: AccessibilityService UI-tree text extraction on AI key tap only.
- No hardware/system screenshot is taken in the normal path.
- Apps listening for Android 14 `ScreenCaptureCallback` / MediaStore screenshot files are not notified.
- Screenshot / MediaProjection remains only as fallback when accessibility is off or the tree is empty.

### 2. On-device automatic reply-style memory
- Default mode: **automatic**
- Every accepted suggestion is stored locally as a style example.
- Optional context snippet can be attached.
- Preference text can be set manually; if empty, a light style hint is inferred after a few examples.
- Memory never leaves the device. Editable / clearable in Settings.
- Prompts force the model to clone length, slang, emoji use, and tone from memory.

### 3. Better chat extraction
- Filters obvious UI chrome ("Send", "Type a message", etc.).
- Includes package name for context.
- Caps message lines to keep prompts tight.

## Required user setup
1. Enable Cluely as the system keyboard.
2. Enable **Cluely on-demand text access** in Android Accessibility settings (critical for stealth).
3. Configure API key (OpenCode Zen / OpenRouter / custom).
4. Grant overlay permission.
5. Use AI key inside chats. Accept suggestions so memory learns your voice.

## Build
This environment has no Android SDK, so APK must be built on a machine with Android Studio or command-line SDK:

```bash
cd cluely-keyboard-stealth
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Or open the folder in Android Studio → Build → Build APK(s).

## Files touched
- `data/accessibility/CluelyAccessibilityService.kt` — stealth tree reader
- `data/memory/ConversationMemory.kt` — automatic style memory
- `data/api/OpenRouterClient.kt` — prompts that clone your voice
- `ui/ime/CluelyIMEService.kt` — accessibility-first comment path
