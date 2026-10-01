
# Setup — API Edition

## 1. Get an OpenRouter key

Create an API key from your OpenRouter dashboard.

The key normally looks like:

`sk-or-v1-...`

Do not put the key in the source code.

## 2. First launch

Open Cluely Keyboard.

You will see:

- OpenRouter API key
- Model
- Save & test connection
- Clear conversation memory

Enter the key and keep:

`openrouter/free`

for the initial setup.

Tap **Save & test connection**.

The app calls the OpenRouter models endpoint. If it succeeds, the UI reports how many models are available.

## 3. Enable the keyboard

Android Settings -> System -> Languages & input -> On-screen keyboard -> enable Cluely Keyboard.

Select Cluely when you want to use it.

## 4. Permissions

The first AI request needs the screen-capture permission. Android shows its system confirmation dialog.

The app also needs overlay permission so the three suggestions can be displayed above the chat.

## 5. Using AI replies

1. Open WhatsApp, Instagram, Telegram, or another chat app.
2. Focus the message field.
3. Switch to Cluely Keyboard.
4. Tap `AI`.
5. Confirm screen capture if Android asks.
6. Cluely sends the captured screen to the selected OpenRouter vision-capable model.
7. Three replies appear.
8. Tap one.
9. The selected reply is inserted into the current text field.

## 6. Free model

`openrouter/free` is the default.

It automatically selects a currently available free model and filters for capabilities required by the request, including image understanding.

If you enter a paid model, the account must have the required credits.

## 7. API key storage

The key is stored using AndroidX Security's encrypted preferences, backed by Android Keystore.

The key is not:
- hard-coded;
- stored in plain text preferences;
- printed to logs;
- included in the APK source.

## 8. Memory

The app keeps a rolling local memory window. It is used as additional prompt context.

Use:

`Cluely Keyboard -> Clear conversation memory`

to erase it.

## 9. Important limitation

This version intentionally uses screenshots for context.

The next improvement should be:

`AccessibilityService -> actual chat text -> memory -> OpenRouter`

with screenshot fallback.

Android's AccessibilityService can query active-window content when the service is configured with window-content retrieval, but individual apps can expose incomplete or changing accessibility trees. Therefore a fallback is still useful.

## 10. Build

```bash
./gradlew assembleDebug
```

If Gradle cannot download its distribution/dependencies, build from Android Studio or GitHub Actions with internet access.
