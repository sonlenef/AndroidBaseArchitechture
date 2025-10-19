package dev.sonle.androidbasearchitechture.ui.features.userlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
// import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
// import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.ui.components.ErrorView
import dev.sonle.androidbasearchitechture.ui.components.LoadingIndicator
import dev.sonle.androidbasearchitechture.util.ResponsiveUtils
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * UserList screen implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen() {
    val viewModel: UserListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val users by viewModel.users.collectAsState()
    
    // val pullToRefreshState = rememberPullToRefreshState()
    
    // LaunchedEffect(pullToRefreshState.isRefreshing) {
    //     if (pullToRefreshState.isRefreshing) {
    //         viewModel.refreshUsers()
    //         pullToRefreshState.endRefresh()
    //     }
    // }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && users.isEmpty() -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        showBackground = true
                    )
                }
                
                uiState.error != null && users.isEmpty() -> {
                    ErrorView(
                        message = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.loadUsers() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    Surface(
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
                    // )
                }
            }
        }
    }
}

@Composable
private fun UserItem(
    user: User,
    onUserClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ResponsiveUtils.getResponsiveElevation()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveUtils.getResponsivePadding()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(ResponsiveUtils.getResponsiveIconSize())
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(ResponsiveUtils.getResponsiveIconSize() / 2)
                    )
                }
            }
            
            // User info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ResponsiveUtils.getResponsiveSpacing())
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = user.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Favorite button
            IconButton(
                onClick = onFavoriteClick
            ) {
                Icon(
                    imageVector = if (user.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (user.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (user.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
