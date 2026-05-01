import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileViewModel.kt", "r") as f:
    content = f.read()

# Add import
content = content.replace("import dev.sonle.androidbasearchitecture.domain.usecase.UpdateUserUseCase", "import dev.sonle.androidbasearchitecture.domain.usecase.UpdateUserUseCase\nimport dev.sonle.androidbasearchitecture.domain.usecase.ai.SummarizeUserUseCase")

# Inject SummarizeUserUseCase
content = content.replace("private val updateUserUseCase: UpdateUserUseCase", "private val updateUserUseCase: UpdateUserUseCase,\n    private val summarizeUserUseCase: SummarizeUserUseCase")

# Add Action handler
content = content.replace("is ProfileAction.NavigateBack -> navigateBack()", "is ProfileAction.NavigateBack -> navigateBack()\n            is ProfileAction.SummarizeProfile -> summarizeProfile()")

# Add summarizeProfile method
summarize_method = """
    private fun summarizeProfile() {
        val currentUser = _uiState.value.user ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, aiSummary = null) }
            
            summarizeUserUseCase(currentUser).collect { result ->
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isSummarizing = false, 
                            aiSummary = result.getOrNull()
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isSummarizing = false, 
                            error = "AI Summarization failed: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            }
        }
    }
"""
content = content.replace("private fun navigateBack() {\n        NavigatorManager.getNavigator().navigateBack()\n    }", "private fun navigateBack() {\n        NavigatorManager.getNavigator().navigateBack()\n    }\n" + summarize_method)

# Update state
content = content.replace("val websiteError: String? = null", "val websiteError: String? = null,\n    val isSummarizing: Boolean = false,\n    val aiSummary: String? = null")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileViewModel.kt", "w") as f:
    f.write(content)

