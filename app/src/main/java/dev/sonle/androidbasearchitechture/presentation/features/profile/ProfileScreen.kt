package dev.sonle.androidbasearchitechture.ui.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sonle.androidbasearchitechture.ui.components.CustomTextField
import dev.sonle.androidbasearchitechture.ui.components.ErrorView
import dev.sonle.androidbasearchitechture.ui.components.LoadingIndicator
import dev.sonle.androidbasearchitechture.util.ResponsiveUtils
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Profile screen implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.loadUser(userId.toLong())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = viewModel::navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.user != null) {
                        if (uiState.isEditMode) {
                            IconButton(
                                onClick = { viewModel.saveUser() },
                                enabled = !uiState.isSaving
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save"
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        showBackground = true
                    )
                }
                
                uiState.error != null && uiState.user == null -> {
                    ErrorView(
                        message = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.loadUser(userId.toLong()) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.user != null -> {
                    ProfileContent(
                        uiState = uiState,
                        onNameChange = viewModel::onNameChange,
                        onEmailChange = viewModel::onEmailChange,
                        onPhoneChange = viewModel::onPhoneChange,
                        onWebsiteChange = viewModel::onWebsiteChange,
                        onSave = viewModel::saveUser,
                        onCancel = viewModel::cancelEdit
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onWebsiteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val user = uiState.user ?: return
    val editedUser = uiState.editedUser ?: user
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ResponsiveUtils.getResponsivePadding())
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
    ) {
        // User avatar and basic info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
            ) {
                Surface(
                    modifier = Modifier
                        .size(ResponsiveUtils.getResponsiveIconSize() * 2)
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
                            modifier = Modifier.size(ResponsiveUtils.getResponsiveIconSize())
                        )
                    }
                }
                
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // User details form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding()),
                verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
            ) {
                Text(
                    text = "User Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Name field
                CustomTextField(
                    value = editedUser.name,
                    onValueChange = onNameChange,
                    label = "Name",
                    isError = uiState.nameError != null,
                    errorMessage = uiState.nameError,
                    enabled = uiState.isEditMode
                )
                
                // Email field
                CustomTextField(
                    value = editedUser.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    enabled = uiState.isEditMode
                )
                
                // Phone field
                CustomTextField(
                    value = editedUser.phone,
                    onValueChange = onPhoneChange,
                    label = "Phone",
                    isError = uiState.phoneError != null,
                    errorMessage = uiState.phoneError,
                    enabled = uiState.isEditMode
                )
                
                // Website field
                CustomTextField(
                    value = editedUser.website,
                    onValueChange = onWebsiteChange,
                    label = "Website",
                    isError = uiState.websiteError != null,
                    errorMessage = uiState.websiteError,
                    enabled = uiState.isEditMode
                )
                
                // Action buttons
                if (uiState.isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = onSave,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
        
        // Error message
        if (uiState.error != null) {
            ErrorView(
                message = uiState.error,
                showBackground = false
            )
        }
    }
}
