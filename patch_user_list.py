import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/userlist/UserListScreen.kt", "r") as f:
    content = f.read()

# Replace imports
content = content.replace("// import androidx.compose.material3.pulltorefresh.PullToRefreshContainer", "import androidx.compose.material3.pulltorefresh.PullToRefreshBox")
content = content.replace("// import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState\n", "")

# Remove the commented LaunchedEffect and state
content = re.sub(r"\s*// val pullToRefreshState = rememberPullToRefreshState\(\)\n", "", content)
content = re.sub(r"\s*// LaunchedEffect\(pullToRefreshState\.isRefreshing\) \{\n\s*//     if \(pullToRefreshState\.isRefreshing\) \{\n\s*//         viewModel\.refreshUsers\(\)\n\s*//         pullToRefreshState\.endRefresh\(\)\n\s*//     \}\n\s*// \}\n", "", content)

# Replace the LazyColumn wrapped in Surface with PullToRefreshBox
old_surface = """                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(ResponsiveUtils.getResponsivePadding()),
                            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
                        ) {
                            items(users) { user ->
                                UserItem(
                                    user = user,
                                    onUserClick = { viewModel.onUserClick(user.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(user.id) }
                                )
                            }
                        }
                    }
                    
                    // PullToRefreshContainer(
                    //     state = pullToRefreshState,
                    //     modifier = Modifier.align(Alignment.TopCenter)
                    // )"""

new_box = """                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshUsers() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(ResponsiveUtils.getResponsivePadding()),
                            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
                        ) {
                            items(users) { user ->
                                UserItem(
                                    user = user,
                                    onUserClick = { viewModel.onUserClick(user.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(user.id) }
                                )
                            }
                        }
                    }"""

content = content.replace(old_surface, new_box)

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/userlist/UserListScreen.kt", "w") as f:
    f.write(content)

