# Multi-Environment Setup Guide

## Overview

This Android project supports three professional environments:
- **Development (dev)**: For local development and testing
- **Staging (staging)**: For pre-production testing and QA
- **Production (prod)**: For live production deployment

Each environment has its own Firebase project, API endpoints, and configuration settings.

## Architecture

### Product Flavors

The project uses Android product flavors to create environment-specific builds:

```kotlin
productFlavors {
    create("dev") {
        dimension = "environment"
        applicationIdSuffix = ".dev"
        versionNameSuffix = "-dev"
        resValue("string", "app_name", "BaseApp Dev")
        buildConfigField("String", "API_BASE_URL", "\"https://dev-api.example.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"development\"")
    }
    
    create("staging") {
        dimension = "environment"
        applicationIdSuffix = ".staging"
        versionNameSuffix = "-staging"
        resValue("string", "app_name", "BaseApp Staging")
        buildConfigField("String", "API_BASE_URL", "\"https://staging-api.example.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"staging\"")
    }
    
    create("prod") {
        dimension = "environment"
        resValue("string", "app_name", "BaseApp")
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"production\"")
    }
}
```

### Build Variants

The following build variants are available:

| Variant | Environment | Signing | Purpose |
|---------|-------------|---------|---------|
| `devDebug` | Development | Debug | Local development |
| `devRelease` | Development | Release | Dev testing |
| `stagingDebug` | Staging | Debug | Staging testing |
| `stagingRelease` | Staging | Release | Pre-production |
| `prodDebug` | Production | Debug | Production testing |
| `prodRelease` | Production | Release | Live production |

## Firebase Configuration

### Separate Firebase Projects

Each environment uses its own Firebase project for complete isolation:

- **Development**: `baseapp-dev`
- **Staging**: `baseapp-staging`
- **Production**: `baseapp-prod`

### Configuration Files

Firebase configuration files are placed in flavor-specific directories:

```
app/
├── src/
│   ├── dev/
│   │   └── google-services.json      (Dev Firebase project)
│   ├── staging/
│   │   └── google-services.json      (Staging Firebase project)
│   └── prod/
│       └── google-services.json      (Production Firebase project)
```

### Firebase Services Configuration

Each Firebase service is configured based on the environment:

```kotlin
object EnvironmentConfig {
    val crashlyticsEnabled: Boolean = !isDevelopment
    val analyticsEnabled: Boolean = isProduction || isStaging
    val performanceMonitoringEnabled: Boolean = isProduction || isStaging
    val fcmEnabled: Boolean = isProduction || isStaging
    val firebaseAuthEnabled: Boolean = true
    val firestoreEnabled: Boolean = true
    val firebaseStorageEnabled: Boolean = true
    val remoteConfigEnabled: Boolean = true
}
```

## Environment-Specific Features

### Development Environment
- **Debug logging**: Enabled
- **Strict mode**: Enabled
- **Crashlytics**: Disabled
- **Analytics**: Disabled
- **Performance monitoring**: Disabled
- **API timeout**: 30 seconds
- **Max items per page**: 10

### Staging Environment
- **Debug logging**: Enabled
- **Strict mode**: Disabled
- **Crashlytics**: Enabled
- **Analytics**: Enabled
- **Performance monitoring**: Enabled
- **API timeout**: 15 seconds
- **Max items per page**: 20

### Production Environment
- **Debug logging**: Disabled
- **Strict mode**: Disabled
- **Crashlytics**: Enabled
- **Analytics**: Enabled
- **Performance monitoring**: Enabled
- **API timeout**: 10 seconds
- **Max items per page**: 50

## Building and Running

### Local Development

Build specific variants:

```bash
# Development builds
./gradlew assembleDevDebug
./gradlew assembleDevRelease

# Staging builds
./gradlew assembleStagingDebug
./gradlew assembleStagingRelease

# Production builds
./gradlew assembleProdDebug
./gradlew assembleProdRelease
```

Install specific variants:

```bash
# Install development debug build
./gradlew installDevDebug

# Install staging debug build
./gradlew installStagingDebug

# Install production debug build
./gradlew installProdDebug
```

### Testing

Run tests for specific environments:

```bash
# Unit tests
./gradlew testDevDebugUnitTest
./gradlew testStagingDebugUnitTest
./gradlew testProdDebugUnitTest

# UI tests
./gradlew connectedDevDebugAndroidTest
./gradlew connectedStagingDebugAndroidTest
./gradlew connectedProdDebugAndroidTest
```

### Coverage Reports

Generate coverage reports for each environment:

```bash
./gradlew jacocoDevDebugTestReport
./gradlew jacocoStagingDebugTestReport
./gradlew jacocoProdDebugTestReport
```

## CI/CD Integration

### GitHub Actions

The CI/CD pipeline automatically builds and tests all environments:

#### Continuous Integration (`.github/workflows/ci.yml`)
- **Lint**: Checks code quality
- **Unit Tests**: Matrix build for all flavors (dev, staging, prod)
- **UI Tests**: Matrix build for all flavors and API levels
- **Coverage**: Generates coverage reports for each environment
- **Build**: Creates APK and AAB files for all environments

#### Release Pipeline (`.github/workflows/release.yml`)
- **Development Release**: Creates prerelease for dev environment
- **Staging Release**: Creates prerelease for staging environment
- **Production Release**: Creates production release with Play Store upload

### Environment-Specific Secrets

Configure GitHub Secrets for each environment:

#### Development Environment
- `DEV_FIREBASE_CONFIG`: Base64 encoded dev google-services.json
- `DEV_API_KEY`: Development API key

#### Staging Environment
- `STAGING_FIREBASE_CONFIG`: Base64 encoded staging google-services.json
- `STAGING_API_KEY`: Staging API key

#### Production Environment
- `PROD_FIREBASE_CONFIG`: Base64 encoded prod google-services.json
- `PROD_API_KEY`: Production API key
- `KEYSTORE_FILE`: Base64 encoded release keystore
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password
- `PLAY_STORE_JSON`: Google Play Console service account JSON

## Environment Switching

### Development Environment Badge

The app displays an environment badge in non-production builds:

```kotlin
@Composable
fun EnvironmentBadge() {
    if (!EnvironmentConfig.isProduction) {
        Surface(
            color = if (EnvironmentConfig.isDevelopment) Color.Green else Color.Orange,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = EnvironmentConfig.environment.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
```

### Environment Information

For debugging, use the `EnvironmentInfo` composable:

```kotlin
@Composable
fun EnvironmentInfo() {
    if (!EnvironmentConfig.isProduction) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = buildString {
                    appendLine("Environment: ${EnvironmentConfig.environment}")
                    appendLine("API URL: ${EnvironmentConfig.apiBaseUrl}")
                    appendLine("Debug Logging: ${EnvironmentConfig.enableDebugLogging}")
                    appendLine("Strict Mode: ${EnvironmentConfig.enableStrictMode}")
                    appendLine("Crashlytics: ${EnvironmentConfig.crashlyticsEnabled}")
                    appendLine("Analytics: ${EnvironmentConfig.analyticsEnabled}")
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
```

## Remote Config

Each environment has its own Remote Config defaults:

### Development (`app/src/dev/res/xml/remote_config_defaults.xml`)
```xml
<defaultsMap>
    <entry>
        <key>enable_new_ui</key>
        <value>true</value>
    </entry>
    <entry>
        <key>max_items_per_page</key>
        <value>10</value>
    </entry>
    <entry>
        <key>enable_debug_logs</key>
        <value>true</value>
    </entry>
    <entry>
        <key>enable_crashlytics</key>
        <value>false</value>
    </entry>
</defaultsMap>
```

### Staging (`app/src/staging/res/xml/remote_config_defaults.xml`)
```xml
<defaultsMap>
    <entry>
        <key>enable_new_ui</key>
        <value>true</value>
    </entry>
    <entry>
        <key>max_items_per_page</key>
        <value>20</value>
    </entry>
    <entry>
        <key>enable_debug_logs</key>
        <value>true</value>
    </entry>
    <entry>
        <key>enable_crashlytics</key>
        <value>true</value>
    </entry>
</defaultsMap>
```

### Production (`app/src/prod/res/xml/remote_config_defaults.xml`)
```xml
<defaultsMap>
    <entry>
        <key>enable_new_ui</key>
        <value>false</value>
    </entry>
    <entry>
        <key>max_items_per_page</key>
        <value>50</value>
    </entry>
    <entry>
        <key>enable_debug_logs</key>
        <value>false</value>
    </entry>
    <entry>
        <key>enable_crashlytics</key>
        <value>true</value>
    </entry>
</defaultsMap>
```

## Security Considerations

### Firebase Security Rules

Implement different security rules for each environment:

#### Development
- Relaxed rules for easy testing
- Allow all operations for development

#### Staging
- Production-like rules
- Additional logging for debugging

#### Production
- Strict security rules
- Minimal permissions
- Rate limiting

### API Security

- Use different API keys for each environment
- Implement environment-specific rate limiting
- Use HTTPS for all environments
- Implement proper authentication and authorization

### Signing Configuration

- **Development/Staging**: Use debug keystore
- **Production**: Use release keystore with proper security

## Troubleshooting

### Common Issues

#### Build Failures
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDevDebug
```

#### Firebase Configuration Issues
- Verify `google-services.json` files are in correct directories
- Check Firebase project settings
- Ensure package names match in Firebase console

#### Environment-Specific Issues
- Check `EnvironmentConfig.kt` for correct environment detection
- Verify BuildConfig fields are properly set
- Check Firebase service initialization logs

### Debug Commands

```bash
# Check build variants
./gradlew tasks --group="build"

# Check available flavors
./gradlew tasks --group="android"

# Run specific test
./gradlew testDevDebugUnitTest --tests="*LoginViewModelTest*"

# Generate debug info
./gradlew assembleDevDebug --info
```

## Best Practices

### Development
1. Always use development environment for local development
2. Test all features in development before moving to staging
3. Use environment badges to avoid confusion
4. Enable debug logging for troubleshooting

### Staging
1. Use staging environment for QA and testing
2. Test with production-like data
3. Verify all Firebase services work correctly
4. Test release builds before production deployment

### Production
1. Only deploy thoroughly tested code
2. Monitor Firebase services and analytics
3. Use proper release signing
4. Implement proper error handling and logging

### Code Organization
1. Use `EnvironmentConfig` for all environment-specific logic
2. Avoid hardcoding environment-specific values
3. Use BuildConfig fields for configuration
4. Implement proper error handling for each environment

## Migration Guide

### From Single Environment to Multi-Environment

1. **Create Firebase Projects**: Set up separate Firebase projects for each environment
2. **Update Build Configuration**: Add product flavors to `build.gradle.kts`
3. **Create Configuration Files**: Add `google-services.json` files for each environment
4. **Update Code**: Replace hardcoded values with `EnvironmentConfig` usage
5. **Update CI/CD**: Modify workflows to support multiple environments
6. **Test**: Verify all environments work correctly

### Adding New Environment

1. **Add Product Flavor**: Add new flavor to `build.gradle.kts`
2. **Create Firebase Project**: Set up new Firebase project
3. **Add Configuration**: Create `google-services.json` file
4. **Update EnvironmentConfig**: Add new environment logic
5. **Update CI/CD**: Add new environment to workflows
6. **Test**: Verify new environment works correctly

## Support

For issues related to multi-environment setup:

1. Check this documentation first
2. Review Firebase console for each environment
3. Check CI/CD logs for build issues
4. Verify environment-specific configuration
5. Test with different build variants

## References

- [Android Product Flavors](https://developer.android.com/studio/build/build-variants)
- [Firebase Multi-Project Setup](https://firebase.google.com/docs/projects/multiprojects)
- [GitHub Actions Matrix Strategy](https://docs.github.com/en/actions/using-jobs/using-a-matrix-for-your-jobs)
- [Android Build Variants](https://developer.android.com/studio/build/build-variants)
