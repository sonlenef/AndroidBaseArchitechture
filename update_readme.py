import re

with open("README.md", "r") as f:
    content = f.read()

# Update Intro
content = content.replace(
    "A production-ready Android application built with Clean Architecture, MVVM pattern, and modern Android development practices.",
    "A production-ready Android application built with Clean Architecture, MVI/MVVM pattern, and modern 2026 Android development practices."
)

# Update Features
content = content.replace(
    "- **MVVM Pattern** with Jetpack Compose UI\n- **Material 3 Design**",
    "- **MVI Architecture** (Model-View-Intent) for predictable state management\n- **Material 3 Design**"
)
content = content.replace(
    "- **Navigation** with Navigation Compose",
    "- **Type-Safe Navigation** with Navigation Compose 2.8+ and Kotlin Serialization"
)

# Update Architecture Diagram
content = content.replace(
    "│  (UI Components, ViewModels, Screens)   │",
    "│  (UI, ViewModels, States, Actions)      │"
)
content = content.replace(
    "- **Presentation Layer**: Jetpack Compose UI with ViewModels",
    "- **Presentation Layer**: Jetpack Compose UI with ViewModels (MVI approach)"
)

# Update Tech Stack
content = content.replace(
    "- **Navigation Compose** - Jetpack Navigation for Compose",
    "- **Navigation Compose** - Type-safe routing\n- **Kotlinx Serialization** - Type-safe data objects"
)

# Write back
with open("README.md", "w") as f:
    f.write(content)
