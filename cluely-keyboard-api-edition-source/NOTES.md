# 📋 Developer Notes & Known Limitations

## Current MVP Status

### What Works
✅ **Core Functionality**
- Basic QWERTY keyboard
- Text input and deletion
- System-wide IME integration
- MediaProjection-based screenshots
- OpenRouter Vision API integration
- Floating overlay chat
- Hilt dependency injection
- DataStore secure settings

✅ **Architecture**
- Clean MVVM pattern
- Service-based architecture
- Coroutine-based async
- Proper lifecycle management

### Known Limitations

⚠️ **Current MVP Limitations**

1. **Keyboard Features**
   - Basic layout only (no number row)
   - No autocomplete/suggestions
   - No custom layouts yet
   - Limited key types (letters, backspace, screenshot)

2. **Screenshot Capture**
   - Screenshot size fixed (no optimization)
   - One screenshot at a time
   - No screenshot history
   - No annotation tools

3. **Chat Interface**
   - Read-only responses (can't continue conversation yet)
   - No chat history storage
   - Basic UI (no animations)
   - Fixed overlay size

4. **AI Features**
   - Single model (configurable but hardcoded)
   - No local models (requires internet)
   - No offline support
   - Limited vision analysis (text and images only)

5. **Permissions**
   - Requires internet always
   - Needs screen overlay permission
   - Needs API key setup

## Architecture Notes

### Why These Choices?

**Hilt for DI**
- Standard in Android
- Works well with IME services
- Easy to test
- Integrates with Android lifecycle

**Ktor for HTTP**
- Lightweight
- Good for vision APIs
- Easy JSON serialization
- Coroutine-native

**Jetpack Compose for Overlay**
- Modern UI toolkit
- Handles state well
- Easier to maintain than XML
- Better animation support

**MediaProjection for Screenshots**
- Only way to capture full screen in Android
- Requires user permission (good for privacy)
- System-level capture (works across apps)
- More reliable than accessibility service

### What We Didn't Include (Yet)

- **Room Database** - No chat persistence in MVP
- **Retrofit** - Used Ktor instead (lighter)
- **WorkManager** - No background tasks needed yet
- **Firebase** - No analytics/crash reporting
- **Google Play Services** - Not needed for MVP
- **WebView** - Not needed for MVP

## Performance Considerations

### Current Bottlenecks

1. **Screenshot Encoding**
   - PNG compression takes ~500-1000ms
   - Base64 encoding adds ~200ms
   - **Solution**: Switch to JPEG for faster encoding

2. **API Response Time**
   - Network latency: 500ms-2s (depends on model/API)
   - Model inference: 1-3s
   - **Solution**: Show "analyzing..." state sooner

3. **Overlay Rendering**
   - Compose recomposition can be slow with large text
   - **Solution**: Implement proper content rendering

### Memory Usage

- IME Service: ~10-15MB
- Screenshot (bitmap in memory): ~5-20MB (depends on density)
- Overlay Service: ~5-10MB
- Total: ~20-45MB peak

**Note**: Screenshots are compressed and not stored, so memory freed immediately after encoding.

## Testing Notes

### Manual Testing Checklist

```
☐ Keyboard appears when text field tapped
☐ All letters type correctly
☐ Backspace deletes characters
☐ Space adds space
☐ Keyboard hides when another IME selected
☐ Screenshot captures screen correctly
☐ Overlay appears with analysis
☐ Can dismiss overlay
☐ Settings persist after app restart
☐ API key is stored securely
☐ Works on multiple Android versions
☐ Works on multiple screen sizes
```

### Automated Testing

Currently NO automated tests (MVP stage). Future improvements:
- Unit tests for OpenRouterClient
- UI tests for KeyboardView
- Service tests for Screenshot/Overlay
- Integration tests for full flow

## Security Considerations

### What We Do Right

✅ API key is encrypted in DataStore
✅ No analytics or tracking
✅ Screenshots not persisted to storage
✅ HTTPS for all API calls
✅ Open source (audit-friendly)
✅ No third-party SDKs with network access

### What Could Be Better

⚠️ SSL pinning not implemented
⚠️ No biometric auth for API key
⚠️ Encryption algorithm not verified
⚠️ No code obfuscation for production

### Privacy Notes

- Screenshots sent to OpenRouter (review their privacy)
- Understand what happens to your screenshot data
- Consider using Claude (Anthropic has strong privacy)
- Don't screenshot sensitive data (passwords, PII, etc.)

## Future Improvements Priority List

### High Priority (v1.1)
- [ ] Chat history storage (Room DB)
- [ ] Follow-up question capability
- [ ] Multiple model support
- [ ] Better error messages
- [ ] Settings UI improvements

### Medium Priority (v1.2-v1.3)
- [ ] Custom keyboard layouts
- [ ] Screenshot optimization
- [ ] Animated transitions
- [ ] Dark mode
- [ ] Screenshot annotation

### Lower Priority (v2.0+)
- [ ] Offline vision model
- [ ] Voice input
- [ ] Widget support
- [ ] iOS version
- [ ] Desktop support

## Debugging Tips

### View Logs
```bash
# All Cluely logs
adb logcat | grep -i cluely

# Specific component
adb logcat | grep OpenRouterClient
adb logcat | grep ScreenshotService
adb logcat | grep ChatOverlayService
```

### Check Permissions
```bash
# View all permissions
adb shell pm list permissions

# Grant permission
adb shell pm grant dev.cluely.keyboard android.permission.SYSTEM_ALERT_WINDOW
```

### Screenshot Debugging
```kotlin
// In ScreenshotService, add logging
Log.d("ScreenshotService", "Screenshot size: $width x $height")
Log.d("ScreenshotService", "Base64 length: ${base64.length}")
```

## Known Bugs (to Fix)

1. **Overlay sometimes doesn't disappear**
   - Need to implement proper dismiss logic
   - Add timeout for auto-dismiss

2. **API key not immediately available**
   - DataStore has async loading
   - Need to wait or show loading state

3. **Keyboard crashes on screen rotation**
   - IME state not preserved
   - Need to implement onSaveInstanceState

## Performance Profiling

To profile the app:

```bash
# Record trace
adb shell perfetto -o /data/traces/my_trace.perfetto-trace --time 5s

# Pull trace
adb pull /data/traces/my_trace.perfetto-trace

# View in Android Studio or go.perfetto.dev
```

## Code Quality Notes

### Lint Status
- Run: `./gradlew lint`
- Currently aiming for 0 errors, few warnings
- ProGuard rules configured for release builds

### Code Style
- Following Kotlin conventions
- Using linting rules from build.gradle
- IDE configured with Kotlin code style

### Naming Conventions
- Services: `XyzService`
- Activities: `XyzActivity`
- ViewModels: `XyzViewModel`
- Data models: `Xyz`
- Private methods: `_method()`

## Deployment Notes

### Version Management
- Current: v1.0.0
- In `app/build.gradle.kts`
- Follow semantic versioning

### Release Checklist
1. Update version code & name
2. Update CHANGELOG
3. Test on multiple devices
4. Run full lint check
5. Build release APK
6. Sign with release key
7. Tag git release
8. Create GitHub release

---

**Questions about architecture?** Check the code comments or create an issue!