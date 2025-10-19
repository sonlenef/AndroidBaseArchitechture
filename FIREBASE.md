# Firebase Integration Guide

This document provides a comprehensive guide to the Firebase integration in the Android Compose base architecture project.

## Overview

The project integrates all major Firebase services to provide a complete backend solution:

- **Firebase Authentication** - User authentication with multiple providers
- **Cloud Firestore** - NoSQL database with real-time sync
- **Firebase Cloud Messaging** - Push notifications
- **Firebase Analytics** - User behavior tracking
- **Firebase Crashlytics** - Crash reporting and monitoring
- **Firebase Remote Config** - Dynamic configuration management
- **Firebase Storage** - File storage and management
- **Firebase Performance Monitoring** - App performance tracking

## Architecture

### Firebase Services Integration

```
┌─────────────────────────────────────────────────────────────┐
│                    Firebase Services                        │
├─────────────────────────────────────────────────────────────┤
│  Authentication  │  Firestore  │  Storage  │  Analytics    │
│  FCM            │  Remote Config │  Crashlytics │  Performance │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 Data Layer                                  │
├─────────────────────────────────────────────────────────────┤
│  FirebaseAuthDataSource  │  FirestoreDataSource  │  ...    │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 Domain Layer                                │
├─────────────────────────────────────────────────────────────┤
│  Use Cases  │  Repository Interfaces  │  Domain Models     │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 Presentation Layer                          │
├─────────────────────────────────────────────────────────────┤
│  ViewModels  │  Composables  │  Navigation  │  Theme       │
└─────────────────────────────────────────────────────────────┘
```

## Firebase Services

### 1. Firebase Authentication

**Location**: `app/src/main/java/com/example/baseapp/data/auth/`

**Features**:
- Email/Password authentication
- Google Sign-In
- Phone authentication
- Anonymous authentication
- Password reset
- Email verification

**Key Files**:
- `FirebaseAuthDataSource.kt` - Main authentication data source
- `AuthState.kt` - Authentication state management
- `AuthResult.kt` - Authentication result handling

**Usage**:
```kotlin
@Inject
lateinit var authDataSource: FirebaseAuthDataSource

// Sign in with email
val result = authDataSource.signInWithEmailAndPassword(email, password)

// Sign in with Google
val result = authDataSource.signInWithGoogle(idToken)

// Get auth state
authDataSource.getAuthState().collect { state ->
    when (state) {
        is AuthState.Authenticated -> // User is signed in
        is AuthState.Unauthenticated -> // User is signed out
        is AuthState.Error -> // Handle error
    }
}
```

### 2. Cloud Firestore

**Location**: `app/src/main/java/com/example/baseapp/data/firestore/`

**Features**:
- Real-time data synchronization
- Offline persistence
- Query support
- Batch operations
- Pagination

**Collections**:
- `users/{userId}` - User data
- `user_profiles/{userId}` - Extended user profiles
- `notifications/{notificationId}` - Push notifications

**Key Files**:
- `FirestoreDataSource.kt` - Main Firestore operations
- `FirestoreUser.kt` - User data models
- `FirestoreUserProfile.kt` - Profile data models

**Usage**:
```kotlin
@Inject
lateinit var firestoreDataSource: FirestoreDataSource

// Get users with real-time updates
firestoreDataSource.getUsers().collect { result ->
    when (result) {
        is NetworkResult.Success -> // Handle users
        is NetworkResult.Error -> // Handle error
    }
}

// Add user
val result = firestoreDataSource.addUser(user)

// Search users
firestoreDataSource.searchUsers("John").collect { result ->
    // Handle search results
}
```

### 3. Firebase Cloud Messaging (FCM)

**Location**: `app/src/main/java/com/example/baseapp/core/fcm/`

**Features**:
- Push notifications
- Notification channels
- Background/Foreground handling
- Token management
- Action buttons

**Key Files**:
- `MyFirebaseMessagingService.kt` - FCM service
- `NotificationHelper.kt` - Notification utilities
- `FcmTokenManager.kt` - Token management

**Usage**:
```kotlin
@Inject
lateinit var fcmTokenManager: FcmTokenManager

// Get FCM token
val token = fcmTokenManager.getToken()

// Subscribe to topic
fcmTokenManager.subscribeToTopic("news")

// Show notification
notificationHelper.showNotification(
    title = "New Message",
    body = "You have a new message",
    type = "message"
)
```

### 4. Firebase Analytics

**Location**: `app/src/main/java/com/example/baseapp/core/analytics/`

**Features**:
- Event tracking
- User properties
- Screen tracking
- Custom events
- Conversion tracking

**Key Files**:
- `AnalyticsManager.kt` - Analytics wrapper
- `AnalyticsEvents.kt` - Event constants
- `AnalyticsExtensions.kt` - Compose extensions

**Usage**:
```kotlin
@Inject
lateinit var analyticsManager: AnalyticsManager

// Log screen view
analyticsManager.logScreenView("user_list")

// Log user action
analyticsManager.logUserAction("edit_profile", "profile")

// Log custom event
analyticsManager.logEvent("feature_used", mapOf(
    "feature" to "image_upload",
    "success" to true
))

// In Compose
@Composable
fun UserListScreen() {
    TrackScreenView("user_list")
    // Screen content
}
```

### 5. Firebase Crashlytics

**Location**: `app/src/main/java/com/example/baseapp/core/crashlytics/`

**Features**:
- Automatic crash reporting
- Non-fatal exception logging
- Custom keys/logs
- User identification
- Breadcrumb tracking

**Key Files**:
- `CrashlyticsManager.kt` - Crashlytics wrapper
- `CrashlyticsLogger.kt` - Custom logging

**Usage**:
```kotlin
@Inject
lateinit var crashlyticsManager: CrashlyticsManager

// Set user ID
crashlyticsManager.setUserId(userId)

// Set custom key
crashlyticsManager.setCustomKey("screen", "user_list")

// Log non-fatal exception
crashlyticsManager.recordException(exception)

// Log message
crashlyticsManager.log("User performed action: edit_profile")
```

### 6. Firebase Remote Config

**Location**: `app/src/main/java/com/example/baseapp/core/config/`

**Features**:
- Dynamic configuration
- Feature flags
- A/B testing
- Real-time updates
- Default values

**Key Files**:
- `RemoteConfigManager.kt` - Remote Config wrapper
- `FeatureFlags.kt` - Feature flag definitions
- `RemoteConfigDefaults.kt` - Default values

**Usage**:
```kotlin
@Inject
lateinit var remoteConfigManager: RemoteConfigManager

// Initialize
remoteConfigManager.initialize()

// Get feature flag
val isNewUIEnabled = remoteConfigManager.isFeatureEnabled("enable_new_ui")

// Get configuration value
val maxItems = remoteConfigManager.getMaxItemsPerPage()

// Check maintenance mode
if (remoteConfigManager.isMaintenanceMode()) {
    val message = remoteConfigManager.getMaintenanceMessage()
    // Show maintenance message
}
```

### 7. Firebase Storage

**Location**: `app/src/main/java/com/example/baseapp/data/storage/`

**Features**:
- File upload/download
- Image compression
- Progress tracking
- Metadata management
- Security rules

**Key Files**:
- `FirebaseStorageDataSource.kt` - Storage operations
- `ImageUploader.kt` - Image upload utilities

**Usage**:
```kotlin
@Inject
lateinit var imageUploader: ImageUploader

// Upload profile image
val result = imageUploader.uploadAndCompressImage(
    imageUri = imageUri,
    userId = userId,
    maxWidth = 800,
    maxHeight = 600,
    quality = 80
)

when (result) {
    is StorageResult.Success -> {
        val downloadUrl = result.data
        // Update user profile with image URL
    }
    is StorageResult.Error -> {
        // Handle error
    }
}
```

### 8. Firebase Performance Monitoring

**Location**: `app/src/main/java/com/example/baseapp/core/performance/`

**Features**:
- Automatic trace monitoring
- Custom traces
- Network request monitoring
- Screen rendering traces
- Memory usage tracking

**Key Files**:
- `PerformanceMonitor.kt` - Performance wrapper
- `PerformanceTraces.kt` - Custom traces

**Usage**:
```kotlin
@Inject
lateinit var performanceTraces: PerformanceTraces

// Trace user action
val trace = performanceTraces.traceUserAction("edit_profile", "profile")
// Perform action
performanceTraces.stopTrace(trace)

// Trace network request
val networkTrace = performanceTraces.traceNetworkRequest(url, "GET")
// Make request
performanceTraces.stopTrace(networkTrace)

// Trace screen rendering
val screenTrace = performanceTraces.traceScreenRendering("user_list")
// Render screen
performanceTraces.stopTrace(screenTrace)
```

## Configuration

### 1. Firebase Project Setup

1. Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your project
3. Download `google-services.json` and place it in `app/` directory
4. Configure the package name and SHA-1 fingerprint

### 2. Dependencies

The project uses Firebase BoM for version management:

```kotlin
// In app/build.gradle.kts
dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    
    // Firebase services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-perf")
}
```

### 3. Security Rules

#### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    match /user_profiles/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    match /notifications/{notificationId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

#### Storage Security Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    
    match /notification_images/{notificationId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
  }
}
```

## Testing

### 1. Unit Tests

Firebase services can be mocked for unit testing:

```kotlin
@MockK
lateinit var mockFirebaseAuth: FirebaseAuth

@MockK
lateinit var mockFirestore: FirebaseFirestore

@Test
fun `login should return success when credentials are valid`() = runTest {
    // Given
    coEvery { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns
        mockk<AuthResult> {
            every { user } returns mockk()
        }
    
    // When
    val result = authDataSource.signInWithEmailAndPassword(email, password)
    
    // Then
    assertTrue(result is AuthResult.Success)
}
```

### 2. Integration Tests

Use Firebase emulator suite for integration testing:

```kotlin
@HiltAndroidTest
class FirebaseIntegrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        // Start Firebase emulators
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
    }
    
    @Test
    fun testUserAuthentication() {
        // Test authentication flow
    }
}
```

## Best Practices

### 1. Error Handling

Always handle Firebase errors gracefully:

```kotlin
try {
    val result = firebaseOperation()
    when (result) {
        is Success -> // Handle success
        is Error -> // Handle error with user-friendly message
    }
} catch (e: Exception) {
    // Log error and show user-friendly message
    crashlyticsManager.recordException(e)
    analyticsManager.logError("operation_failed", e.message)
}
```

### 2. Offline Support

Firebase provides automatic offline support, but handle it explicitly:

```kotlin
firestoreDataSource.getUsers().collect { result ->
    when (result) {
        is NetworkResult.Success -> {
            // Show data
        }
        is NetworkResult.Error -> {
            if (result.message.contains("offline")) {
                // Show cached data or offline message
            } else {
                // Show error message
            }
        }
    }
}
```

### 3. Performance Optimization

Use Firebase Performance Monitoring to track and optimize:

```kotlin
// Trace expensive operations
val trace = performanceTraces.traceDataLoading("users", "firestore")
try {
    val users = loadUsers()
    performanceTraces.addMetric(trace, "user_count", users.size.toLong())
} finally {
    performanceTraces.stopTrace(trace)
}
```

### 4. Security

Follow Firebase security best practices:

- Use security rules to protect data
- Validate data on the client and server
- Use Firebase App Check for additional security
- Implement proper authentication flows

## Troubleshooting

### Common Issues

1. **Firebase not initialized**: Ensure `FirebaseInitializer` is called in `Application.onCreate()`
2. **Authentication errors**: Check Firebase Console configuration
3. **Firestore permission denied**: Verify security rules
4. **FCM not working**: Check device token and server configuration
5. **Analytics not showing**: Wait 24-48 hours for data to appear

### Debug Mode

Enable debug mode for development:

```kotlin
// In Application.onCreate()
if (BuildConfig.DEBUG) {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
    FirebasePerformance.getInstance().setPerformanceCollectionEnabled(false)
}
```

## Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android SDK](https://firebase.google.com/docs/android/setup)
- [Firebase Console](https://console.firebase.google.com/)
- [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite)
- [Firebase Security Rules](https://firebase.google.com/docs/rules)
