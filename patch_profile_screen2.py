import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace("IconButton(onClick = viewModel::navigateBack) {", "IconButton(onClick = { viewModel.onAction(ProfileAction.NavigateBack) }) {")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

