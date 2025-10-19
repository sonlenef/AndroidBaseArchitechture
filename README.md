# Android Base Architecture

A production-ready Android application built with Clean Architecture, MVVM pattern, and modern Android development practices.

## 🚀 Features

- **Clean Architecture** with clear separation of concerns
- **MVVM Pattern** with Jetpack Compose UI
- **Material 3 Design** with responsive layouts
- **Multi-Environment Support** (dev/staging/prod)
- **Firebase Integration** (Auth, Firestore, Analytics, Crashlytics, FCM, Performance)
- **Offline-First** data strategy with Room database
- **Dependency Injection** with Hilt
- **Navigation** with Navigation Compose
- **Comprehensive Testing** (Unit + UI tests)
- **CI/CD Ready** with GitHub Actions

## 🏗️ Architecture

This project follows **Clean Architecture** principles with the following layers:

```
┌─────────────────────────────────────────┐
│              Presentation               │
│  (UI Components, ViewModels, Screens)  │
├─────────────────────────────────────────┤
│               Domain                    │
│     (Use Cases, Models, Interfaces)    │
├─────────────────────────────────────────┤
│                Data                     │
│  (Repositories, Data Sources, DTOs)    │
└─────────────────────────────────────────┘
```

### Key Components

- **Presentation Layer**: Jetpack Compose UI with ViewModels
- **Domain Layer**: Business logic and use cases
- **Data Layer**: Repository pattern with Room + Retrofit
- **Core Layer**: Utilities, DI modules, and shared components

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **Hilt** - Dependency injection
- **Navigation Compose** - Jetpack Navigation for Compose

### Data & Network
- **Room** - Local database
- **Retrofit** - Network client
- **Gson** - JSON serialization
- **Kotlin Coroutines** - Asynchronous programming

### Firebase Services
- **Authentication** - User management
- **Firestore** - Cloud database
- **Analytics** - User behavior tracking
- **Crashlytics** - Crash reporting
- **Cloud Messaging** - Push notifications
- **Performance Monitoring** - App performance
- **Remote Config** - Feature flags

### Testing
- **JUnit** - Unit testing
- **MockK** - Mocking framework
- **Turbine** - Flow testing
- **Compose Test** - UI testing
- **Hilt Testing** - DI testing

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK 24+ (Android 7.0)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/AndroidBaseArchitechture.git
   cd AndroidBaseArchitechture
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure Firebase** (Optional)
   - Create Firebase projects for each environment (dev/staging/prod)
   - Download `google-services.json` files
   - Place them in respective flavor directories:
     - `app/src/dev/google-services.json`
     - `app/src/staging/google-services.json`
     - `app/src/prod/google-services.json`

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Build Variants

The project supports multiple build variants:

| Variant | Environment | Debug Logging | Analytics | Crashlytics |
|---------|-------------|---------------|-----------|-------------|
| devDebug | Development | ✅ | ❌ | ❌ |
| devRelease | Development | ✅ | ❌ | ❌ |
| stagingDebug | Staging | ✅ | ✅ | ✅ |
| stagingRelease | Staging | ✅ | ✅ | ✅ |
| prodDebug | Production | ❌ | ✅ | ✅ |
| prodRelease | Production | ❌ | ✅ | ✅ |

## 📱 Screenshots

### Login Screen
- Clean, modern login interface
- Form validation with error messages
- Demo credentials information

### User List Screen
- Responsive user list with pull-to-refresh
- Favorite functionality
- Search capabilities
- Material 3 design

### Profile Screen
- User profile details
- Edit mode with validation
- Save/cancel functionality

## 🧪 Testing

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run tests for specific variant
./gradlew testDevDebugUnitTest

# Generate test coverage report
./gradlew jacocoTestReport
```

### Test Coverage

- **ViewModels**: ≥90% coverage
- **Use Cases**: 100% coverage
- **Repositories**: ≥85% coverage
- **Utilities**: 100% coverage

## 🏗️ Project Structure

```
app/src/main/java/dev/sonle/androidbasearchitechture/
├── core/                          # Core functionality
│   ├── analytics/                 # Analytics management
│   ├── config/                    # Configuration
│   ├── crashlytics/              # Crash reporting
│   ├── database/                  # Room database
│   ├── di/                        # Dependency injection
│   ├── fcm/                       # Firebase Cloud Messaging
│   ├── network/                   # Network utilities
│   ├── performance/               # Performance monitoring
│   └── util/                      # Utility classes
├── data/                          # Data layer
│   ├── local/                     # Local data sources
│   ├── remote/                    # Remote data sources
│   ├── mapper/                    # Data mappers
│   └── repository/                # Repository implementations
├── domain/                        # Domain layer
│   ├── model/                     # Domain models
│   ├── repository/                # Repository interfaces
│   └── usecase/                   # Use cases
└── presentation/                  # Presentation layer
    ├── components/                # Reusable UI components
    ├── features/                  # Feature modules
    ├── navigation/                # Navigation
    ├── theme/                     # Theme and styling
    └── util/                      # Presentation utilities
```

## 🔧 Configuration

### Environment Configuration

The app supports multiple environments through build flavors:

- **Development**: Debug logging enabled, relaxed security
- **Staging**: Production-like with analytics enabled
- **Production**: Full security, analytics, and monitoring

### Remote Config

Feature flags and configuration are managed through Firebase Remote Config:

- Feature toggles
- API timeouts
- UI settings
- Security policies

## 📊 Monitoring & Analytics

### Firebase Analytics
- User behavior tracking
- Screen view analytics
- Custom event logging
- User properties

### Crashlytics
- Crash reporting
- Custom logging
- User identification
- Environment tracking

### Performance Monitoring
- Screen rendering performance
- Network request timing
- User action traces
- Custom metrics

## 🚀 Deployment

### Build Commands

```bash
# Build all variants
./gradlew assemble

# Build specific variant
./gradlew assembleProdRelease

# Generate signed APK
./gradlew assembleProdRelease
```

### CI/CD

The project includes GitHub Actions workflows for:

- Automated testing
- Code quality checks
- Build verification
- Deployment automation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow the existing code style
- Write tests for new features
- Update documentation
- Ensure all tests pass
- Follow Clean Architecture principles

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Android Developers](https://developer.android.com/) for excellent documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI toolkit
- [Material Design](https://material.io/) for design system
- [Firebase](https://firebase.google.com/) for backend services
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) for navigation library

## 📞 Support

If you have any questions or need help, please:

1. Check the [Issues](https://github.com/yourusername/AndroidBaseArchitechture/issues) page
2. Create a new issue if your question isn't answered
3. Contact the maintainers

---

**Happy Coding! 🎉**