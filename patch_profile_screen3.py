import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onSave = viewModel::saveUser,", "onSave = { viewModel.onAction(ProfileAction.SaveUser) },")
content = content.replace("onCancel = viewModel::cancelEdit", "onCancel = { viewModel.onAction(ProfileAction.CancelEdit) }")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

