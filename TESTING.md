# Testing Documentation

## Overview

This document describes the comprehensive testing strategy implemented for the Android Compose Base Architecture project, including both Unit Tests and UI Tests.

## Test Structure

### Unit Tests (`app/src/test/`)

#### Core Utilities Tests
- **ValidationUtilsTest**: Tests all validation logic for email, password, name, phone, and website validation
  - Email validation (valid/invalid formats)
  - Password validation (length requirements)
  - Name validation (minimum length)
  - Phone validation (format validation)
  - Website validation (URL format)

#### Domain Layer Tests
- **GetUsersUseCaseTest**: Tests user retrieval from repository
- **RefreshUsersUseCaseTest**: Tests user refresh functionality with network results
- **UpdateUserUseCaseTest**: Tests user update operations
- **LoginUseCaseTest**: Tests login validation and authentication logic
- **GetUserByIdUseCaseTest**: Tests individual user retrieval

#### Data Layer Tests
- **UserRepositoryImplTest**: Tests repository implementation with mocked data sources
  - Local data source integration
  - Remote data source integration
  - Data mapping between DTOs and domain entities
  - Error handling and propagation

#### Presentation Layer Tests
- **LoginViewModelTest**: Tests login screen state management
  - Initial state validation
  - Input field updates
  - Validation error handling
  - Loading states
  - Success/error states
- **UserListViewModelTest**: Tests user list functionality
  - User loading from repository
  - Refresh functionality
  - Error handling and retry
- **ProfileViewModelTest**: Tests profile screen functionality
  - User data loading
  - Edit mode management
  - Form validation
  - Save operations
  - Success/error feedback

### UI Tests (`app/src/androidTest/`)

#### Component Tests
- **CustomComponentsTest**: Tests reusable UI components
  - CustomButton (normal, loading, disabled states)
  - CustomTextField (input, validation errors)
  - LoadingIndicator
  - ErrorView

#### Screen Tests
- **LoginScreenTest**: Tests login screen UI interactions
  - Element display
  - Input field interactions
  - Validation error display
  - Loading states
  - Button states and interactions
- **UserListScreenTest**: Tests user list screen functionality
  - Loading indicator display
  - User list rendering
  - Error view display
  - User item interactions
  - Retry functionality
- **ProfileScreenTest**: Tests profile screen functionality
  - User details display
  - Edit mode UI
  - Form interactions
  - Validation error display
  - Save/cancel operations
  - Success/error feedback

## Testing Tools & Libraries

### Unit Testing
- **JUnit 4**: Core testing framework
- **Kotlinx Coroutines Test**: Testing coroutines and flows
- **Turbine**: Testing Kotlin Flows with proper emission handling
- **MockK**: Mocking library for Kotlin with comprehensive mocking capabilities
- **Architecture Components Testing**: LiveData and ViewModel testing utilities

### UI Testing
- **Compose Test**: Testing Compose UI components and interactions
- **Espresso**: Android UI testing framework
- **Hilt Testing**: Testing with dependency injection
- **MockK Android**: Android-specific mocking capabilities

## Test Utilities

### TestData
- **TestData.kt**: Centralized test data objects
  - Sample User DTOs, Entities, and Domain models
  - Multiple user scenarios for comprehensive testing
  - Consistent test data across all test classes

### TestDispatcher
- **TestDispatcher.kt**: Custom test dispatcher for coroutine testing
  - MainDispatcherRule for proper coroutine testing
  - UnconfinedTestDispatcher for immediate execution
  - Proper setup and teardown for test isolation

### Hilt Testing Setup
- **HiltTestRunner.kt**: Custom test runner for Hilt integration
- **TestApplication.kt**: Test application class with Hilt support

## Test Coverage

### Unit Test Coverage
- **Validation Logic**: 100% coverage of all validation scenarios
- **Use Cases**: Complete business logic testing
- **Repository**: Full data flow testing with mocked dependencies
- **ViewModels**: Comprehensive state management testing
- **Error Handling**: All error scenarios and edge cases

### UI Test Coverage
- **Component States**: All component states (loading, error, success)
- **User Interactions**: All user input and interaction scenarios
- **Navigation**: Screen transitions and navigation flows
- **Form Validation**: Real-time validation feedback
- **Error States**: Error display and retry mechanisms

## Best Practices Implemented

### Test Naming
- **Descriptive Names**: `should_ExpectedBehavior_When_StateUnderTest`
- **Clear Intent**: Each test clearly describes what it's testing
- **Consistent Format**: Uniform naming across all test classes

### Test Structure
- **AAA Pattern**: Arrange, Act, Assert structure
- **Single Responsibility**: Each test focuses on one specific behavior
- **Proper Setup/Teardown**: Clean test environment for each test

### Mocking Strategy
- **External Dependencies**: All external dependencies are mocked
- **Repository Pattern**: Repository interfaces mocked for clean testing
- **Network Layer**: API calls mocked for predictable testing
- **Database Layer**: Room database operations mocked

### State Testing
- **StateFlow Testing**: Proper testing of reactive state management
- **Turbine Integration**: Comprehensive Flow testing with emission handling
- **State Transitions**: Testing all possible state transitions
- **Error Propagation**: Testing error states and recovery

## Running Tests

### Unit Tests
```bash
./gradlew test
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

### Specific Test Classes
```bash
./gradlew test --tests "dev.sonle.pdfscanner.presentation.features.scanner.ScannerViewModelTest"
./gradlew connectedAndroidTest --tests "dev.sonle.pdfscanner.MainNavigationSmokeTest"
```

## Test Reports

Test reports are generated in:
- **Unit Tests**: `app/build/reports/tests/testDebugUnitTest/`
- **UI Tests**: `app/build/reports/androidTests/connected/`

## Continuous Integration

The test suite is designed to run in CI/CD pipelines:
- **Fast Execution**: Unit tests run quickly for immediate feedback
- **Reliable**: UI tests are stable and don't flake
- **Comprehensive**: Full coverage of critical functionality
- **Maintainable**: Easy to update and extend

## Future Enhancements

### Potential Additions
- **Integration Tests**: End-to-end testing with real database
- **Performance Tests**: Testing app performance under load
- **Accessibility Tests**: Testing accessibility features
- **Screenshot Tests**: Visual regression testing
- **Network Tests**: Testing offline/online scenarios

### Test Data Management
- **Dynamic Test Data**: Generate test data dynamically
- **Test Fixtures**: More sophisticated test data management
- **Database Seeding**: Pre-populate test database with known data

## Conclusion

The testing implementation provides:
- **High Coverage**: Comprehensive testing of all critical functionality
- **Reliability**: Stable tests that provide consistent results
- **Maintainability**: Well-structured tests that are easy to understand and modify
- **Quality Assurance**: Confidence in code changes through automated testing
- **Documentation**: Tests serve as living documentation of expected behavior

This testing strategy ensures the Android Compose Base Architecture maintains high quality and reliability as it evolves and grows.
