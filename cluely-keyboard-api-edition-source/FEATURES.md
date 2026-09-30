# 🎯 Cluely Keyboard Features & Roadmap

## ✅ MVP Features (v1.0)

### Core Functionality
- [x] **Android IME Integration** - System-wide keyboard access
- [x] **QWERTY Keyboard** - Basic text input
- [x] **Screenshot Capture** - One-tap screen capture via MediaProjection
- [x] **OpenRouter Integration** - Vision API calls with Ktor
- [x] **Chat Overlay** - Floating window with Jetpack Compose
- [x] **Hilt Dependency Injection** - Clean architecture
- [x] **DataStore for Settings** - Secure API key storage

### Privacy & Security
- [x] Local API key storage (no cloud sync)
- [x] On-demand screenshot capture
- [x] No image persistence
- [x] No analytics tracking
- [x] Open source

## 🚀 Planned Features

### v1.1 - Enhanced Chat
- [ ] **Chat History** - Persist conversation in local Room database
- [ ] **Copy to Clipboard** - Quick copy of AI responses
- [ ] **Follow-up Questions** - Context-aware conversation
- [ ] **Multi-Model Support** - Switch between Claude, GPT-4V, Gemini
- [ ] **Settings UI Improvements** - Better settings screen

### v1.2 - Keyboard Enhancements
- [ ] **Custom Keyboard Layouts** - Dvorak, Colemak, other layouts
- [ ] **Emoji Picker** - Quick emoji insertion
- [ ] **Gesture Support** - Swipe gestures for common actions
- [ ] **Theme Support** - Light/dark mode, custom colors
- [ ] **Haptic Feedback** - Vibration on key press

### v1.3 - Screenshot Features
- [ ] **Screenshot Annotation** - Draw, highlight, crop before analysis
- [ ] **OCR Mode** - Extract text from screenshots
- [ ] **Comparison Mode** - Analyze two screenshots together
- [ ] **Screenshot History** - Browse recent captures (local only)
- [ ] **Quick Actions** - "Translate", "Summarize", "Extract data" buttons

### v1.4 - Voice & Accessibility
- [ ] **Voice Input for Questions** - Voice-to-text for AI queries
- [ ] **Text-to-Speech Responses** - Read AI responses aloud
- [ ] **Accessibility Support** - Better TalkBack support
- [ ] **One-Handed Mode** - Optimized keyboard layout
- [ ] **High Contrast Theme** - WCAG compliance

### v2.0 - Advanced Features
- [ ] **Offline Vision Model** - On-device image analysis (TensorFlow Lite)
- [ ] **Quick Action Buttons** - Customizable toolbars
- [ ] **Widget Integration** - Home screen widget
- [ ] **Shortcuts** - Quick access to previous analyses
- [ ] **Floating Bubble** - Alternative to overlay (Android 13+)
- [ ] **Context Menu Integration** - Share to Cluely from any app

## 🛠️ Technical Improvements

- [ ] **Unit Tests** - Core logic testing
- [ ] **Integration Tests** - API and service testing
- [ ] **Automated Screenshots** - CI/CD testing
- [ ] **Performance Optimization** - Faster screenshot processing
- [ ] **Memory Management** - Reduce RAM footprint
- [ ] **Kotlin Multiplatform** - iOS version

## 📊 Analytics & Usage

*Note: All analytics are optional and local-only*

- [ ] **Local Usage Stats** - Feature usage tracking (device-only)
- [ ] **Crash Reporting** - Optional error logs
- [ ] **Feature Popularity** - Which features are used most

## 🎨 UI/UX Improvements

- [ ] **Animated Transitions** - Smooth overlay animations
- [ ] **Skeleton Loading** - Loading states for analysis
- [ ] **Toast Notifications** - Success/error feedback
- [ ] **Dark Mode** - Full dark mode support
- [ ] **Dynamic Colors** - Material You theming (Android 12+)

## 🔒 Security Enhancements

- [ ] **Encrypted Storage** - Encrypt API keys at rest
- [ ] **SSL Pinning** - Protect against MITM attacks
- [ ] **Rate Limiting** - Prevent accidental spam
- [ ] **Biometric Auth** - Optional fingerprint for API key

## 📱 Device Support

- **Current**: Android 10+ (API 29+)
- [ ] **Tablet Support** - Optimized layouts for tablets
- [ ] **Foldable Support** - Handle foldable phones
- [ ] **TV Support** - Android TV adaptation

## 🌍 Internationalization

- [ ] **Multi-language Support** - UI translations
- [ ] **RTL Language Support** - Arabic, Hebrew, etc.
- [ ] **Regional Models** - Language-specific AI models

---

## How to Vote on Features

Which features would you like to see first? Open a GitHub discussion or react to issues with:
- 👍 - I want this
- ❤️ - Critical for me
- 🎉 - Excited about this

Your feedback helps prioritize development!