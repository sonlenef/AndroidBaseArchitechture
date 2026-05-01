import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

# Add a button in the layout (find "Profile info" card)
old_content = """                // Profile info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ResponsiveUtils.getResponsiveSpacing()),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = ResponsiveUtils.getResponsiveElevation()
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding()),
                        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
                    ) {"""

new_content = """                // AI Summary Section
                if (uiState.aiSummary != null || uiState.isSummarizing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = ResponsiveUtils.getResponsiveSpacing()),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨ AI Analysis (On-Device)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                if (uiState.isSummarizing) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp).padding(start = 8.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            if (uiState.aiSummary != null) {
                                Text(
                                    text = uiState.aiSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Profile info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ResponsiveUtils.getResponsiveSpacing()),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = ResponsiveUtils.getResponsiveElevation()
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding()),
                        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
                    ) {"""
content = content.replace(old_content, new_content)

# Add AI summarize button below normal Profile info fields
old_button_section = """                        ProfileField(
                            icon = Icons.Default.Language,
                            label = "Website",
                            value = user.website
                        )
                    }
                }"""

new_button_section = """                        ProfileField(
                            icon = Icons.Default.Language,
                            label = "Website",
                            value = user.website
                        )
                    }
                }
                
                // AI Action
                if (!uiState.isEditMode) {
                    CustomButton(
                        text = "Summarize with Gemini Nano",
                        onClick = { viewModel.onAction(ProfileAction.SummarizeProfile) },
                        isLoading = uiState.isSummarizing,
                        enabled = !uiState.isSummarizing && !uiState.isLoading
                    )
                }"""
content = content.replace(old_button_section, new_button_section)

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileScreen.kt", "w") as f:
    f.write(content)

