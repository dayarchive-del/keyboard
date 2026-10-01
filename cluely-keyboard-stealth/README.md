
# Cluely Keyboard — API Edition

A modified Android fork of Cluely Keyboard focused on a lightweight, explicit AI-reply flow.

## Current architecture

`Chat screen -> tap AI -> optional on-demand accessibility text read -> OpenAI-compatible model -> 3 reply suggestions -> tap -> insert`

OpenCode Zen is the default provider. OpenRouter and custom OpenAI-compatible endpoints remain available in Settings.

The app also keeps a bounded local memory of accepted replies and style preferences so later requests can better match the user's writing style.

## API setup

1. Install the APK.
2. Open **Cluely Keyboard**.
3. Choose **OpenCode Zen** (the default) or another provider and enter its API key.
4. Leave the model as the suggested free model to start.
5. Tap **Save & test connection**.
6. Enable Cluely as your Android keyboard.
7. Optionally enable **Cluely on-demand text access** in Android Accessibility settings.
8. Grant overlay permission. Screen capture is only a fallback and uses Android's normal consent prompt.
9. Open a chat and tap the **AI** key.

OpenRouter exposes an OpenAI-compatible API and supports multimodal image input through the Chat Completions API. The `openrouter/free` router currently accepts text and images and selects an available free model compatible with the request.

## Security

- The API key is stored using AndroidX Security `EncryptedSharedPreferences`, backed by Android Keystore.
- Request-body logging is disabled.
- The API key is never written to source code.
- Text or a screenshot is sent to the selected provider only when the user requests an AI suggestion.
- Accessibility text is read only once after an AI-key tap; there is no event subscription or passive monitoring.
- Local reply-style memory remains on the device, is editable, and can be cleared.
- The app does not disable Android or another app's screenshot indicators.
- The app provides a Clear conversation memory action.

## Model

Default:

`mimo-v2.5-free` (OpenCode Zen)

You can replace it with any OpenRouter model slug that supports image input. The app does not hard-code a paid model.

## Current context mechanism

When the optional accessibility service is enabled and exposes text, structured visible text is used as the primary context. Otherwise the existing screenshot flow is used after Android's screen-capture consent.

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

This build is not fully local when a remote provider is selected. The selected context and bounded local style summary are transmitted only after the user taps AI. Do not use it for sensitive conversations unless you are comfortable with that data flow.

## Upstream

https://github.com/filiksyos/cluely-keyboard-android
