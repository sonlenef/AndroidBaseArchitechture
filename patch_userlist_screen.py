import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/userlist/UserListScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onRetry = { viewModel.loadUsers() },", "onRetry = { viewModel.onAction(UserListAction.LoadUsers) },")
content = content.replace("onRefresh = { viewModel.refreshUsers() },", "onRefresh = { viewModel.onAction(UserListAction.RefreshUsers) },")
content = content.replace("onUserClick = { viewModel.onUserClick(user.id) },", "onUserClick = { viewModel.onAction(UserListAction.UserClicked(user.id)) },")
content = content.replace("onFavoriteClick = { viewModel.toggleFavorite(user.id) }", "onFavoriteClick = { viewModel.onAction(UserListAction.ToggleFavorite(user.id)) }")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/userlist/UserListScreen.kt", "w") as f:
    f.write(content)

