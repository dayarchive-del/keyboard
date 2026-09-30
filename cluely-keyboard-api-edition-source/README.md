
# Cluely Keyboard — API Edition

A modified Android fork of Cluely Keyboard focused on a smooth OpenRouter API setup.

## Current architecture

`Chat screen -> screen capture -> OpenRouter vision model -> 3 reply suggestions -> tap -> insert`

The app also keeps a small local rolling memory of previous AI-generated reply/context records so later requests can be prompted with continuity.

## API setup

1. Install the APK.
2. Open **Cluely Keyboard**.
3. Enter your OpenRouter API key.
4. Leave the model as `openrouter/free` to start.
5. Tap **Save & test connection**.
6. Enable Cluely as your Android keyboard.
7. Grant overlay and screen-capture permissions when requested.
8. Open a chat and tap the **AI** key.

OpenRouter exposes an OpenAI-compatible API and supports multimodal image input through the Chat Completions API. The `openrouter/free` router currently accepts text and images and selects an available free model compatible with the request.

## Security

- The API key is stored using AndroidX Security `EncryptedSharedPreferences`, backed by Android Keystore.
- Request-body logging is disabled.
- The API key is never written to source code.
- The screenshot is sent to OpenRouter only when the user requests an AI suggestion.
- Local conversation memory remains on the device.
- The app provides a Clear conversation memory action.

## Model

Default:

`openrouter/free`

You can replace it with any OpenRouter model slug that supports image input. The app does not hard-code a paid model.

## Current context mechanism

The current API edition uses the visible screenshot as the primary conversation context. This is intentional for this phase because it gives the vision model the actual UI and message layout.

A later accessibility edition can replace screenshot reading with Android AccessibilityService text extraction where the target app exposes its UI content.

## Error handling

The overlay translates common API failures:

- 401/403 -> API key rejected
- 402 -> selected model needs credits
- 429 -> rate limit
- Other errors -> readable request error

## Build

Requirements:
- Android Studio
- JDK 17
- Android SDK
- Internet access for Gradle dependency resolution

```bash
./gradlew assembleDebug
```

Output:

`app/build/outputs/apk/debug/app-debug.apk`

A GitHub Actions workflow is included under `.github/workflows/build-apk.yml`.

## Privacy limitation

An OpenRouter-based build is not fully local. The screenshot and prompt context are transmitted to the selected model provider through OpenRouter. Do not use this mode for sensitive conversations unless you are comfortable with that data flow.

## Upstream

https://github.com/filiksyos/cluely-keyboard-android
