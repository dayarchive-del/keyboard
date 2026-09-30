# 🤝 Contributing to Cluely Keyboard

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to the Cluely Keyboard project.

## 📋 Code of Conduct

We're committed to providing a welcoming and inclusive environment. Please be respectful and constructive in all interactions.

## 🚀 Getting Started

### 1. Fork & Clone
```bash
git clone https://github.com/YOUR_USERNAME/cluely-keyboard-android.git
cd cluely-keyboard-android
```

### 2. Create Feature Branch
```bash
git checkout -b feature/your-feature-name
# Or for bug fixes:
git checkout -b fix/bug-name
```

### 3. Make Changes
- Follow Kotlin naming conventions (camelCase)
- Keep functions focused and testable
- Add comments for complex logic
- Update README if needed

### 4. Test Your Changes
```bash
# Build and run on device
./gradlew installDebug

# Check for lint issues
./gradlew lint
```

### 5. Commit with Clear Messages
```bash
git commit -m "feat: add dark mode theme"
# Or:
git commit -m "fix: screenshot crash on rotation"
git commit -m "docs: update setup instructions"
```

### 6. Push & Create Pull Request
```bash
git push origin feature/your-feature-name
```
Then create a PR on GitHub with:
- Clear title and description
- Link to any related issues
- Screenshots if UI changes

## 📝 Commit Message Format

```
<type>: <subject>
<blank line>
<body>
<blank line>
<footer>
```

### Types
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style (formatting, missing semicolons, etc.)
- **refactor**: Code refactoring without functionality change
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **chore**: Build, dependencies, etc.

### Example
```
feat: add voice input for chat questions

Implement voice-to-text conversion for AI queries using
Android's SpeechRecognizer API. Adds new VoiceInputService
and UI controls in ChatOverlayService.

Closes #42
```

## 🎯 What to Contribute

### Good First Issues
- ✅ Bug fixes
- ✅ Documentation improvements
- ✅ Code cleanup
- ✅ Performance optimization
- ✅ New keyboard layouts

### Features We Welcome
- ✅ Chat history persistence
- ✅ Additional AI models
- ✅ Keyboard themes
- ✅ Accessibility improvements
- ✅ Better error handling

### Before Starting
1. Check if issue already exists
2. Comment on issue saying you'll work on it
3. Discuss major changes before coding
4. Keep PRs focused (one feature per PR)

## 🔍 Code Review Process

Your PR will be reviewed for:
- **Functionality** - Does it work as intended?
- **Code Quality** - Is it readable and maintainable?
- **Testing** - Are edge cases handled?
- **Documentation** - Is it documented?
- **Performance** - Does it impact performance?

### Review Feedback
We appreciate patience during review. Please:
- Respond to comments promptly
- Make requested changes
- Ask questions if unclear
- Don't take feedback personally

## 📚 Architecture & Patterns

### MVVM + Clean Architecture
```
UI Layer (Composables, Activities)
  ↓
ViewModel/Presenter
  ↓
Use Cases (Domain)
  ↓
Repository (Data)
  ↓
Data Sources (API, Database, Storage)
```

### Dependency Injection
Use Hilt for all dependencies:
```kotlin
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
    @Inject
    lateinit var myService: MyService
}
```

### Data Models
Use sealed classes for results:
```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    class Loading<T> : Result<T>()
}
```

## ✅ Checklist Before Submitting PR

- [ ] Code compiles without errors
- [ ] All lint checks pass (`./gradlew lint`)
- [ ] Manual testing completed
- [ ] README updated (if needed)
- [ ] Changes documented in code comments
- [ ] Commit messages are clear
- [ ] No sensitive data committed
- [ ] PR description explains the changes

## 🐛 Reporting Bugs

### Before Creating Issue
1. Check existing issues
2. Try reproducing on latest build
3. Test on multiple devices if possible

### Bug Report Template
```markdown
## Description
Clear description of the bug

## Steps to Reproduce
1. First step
2. Second step
3. ...

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Device: [e.g., Pixel 6]
- Android Version: [e.g., 13]
- App Version: [e.g., 1.0.0]

## Logs
```
adb logcat output here
```

## Screenshots
[If applicable]
```

## 🎓 Learning Resources

### Android Development
- [Android Developers](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Documentation](https://developer.android.com/jetpack)

### Project Specific
- [Architecture Guide](./ARCHITECTURE.md) (if exists)
- [API Documentation](./docs/) (if exists)
- [Setup Guide](./SETUP.md)

## 💬 Questions?

- Check existing GitHub discussions
- Ask in PR comments
- Create a new discussion
- Email: [Your email] (if provided)

## 📜 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for contributing to Cluely Keyboard! 🎉**