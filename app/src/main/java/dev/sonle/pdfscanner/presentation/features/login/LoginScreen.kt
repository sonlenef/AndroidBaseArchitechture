package dev.sonle.pdfscanner.presentation.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sonle.pdfscanner.presentation.components.CustomButton
import dev.sonle.pdfscanner.presentation.components.CustomTextField
import dev.sonle.pdfscanner.presentation.components.EnvironmentBadge
import dev.sonle.pdfscanner.presentation.components.ErrorView
import dev.sonle.pdfscanner.presentation.components.LoadingIndicator
import dev.sonle.pdfscanner.util.ResponsiveUtils
import dev.sonle.pdfscanner.presentation.navigation.LocalNavigator
import org.koin.androidx.compose.koinViewModel

/**
 * Login screen implementation
 */
@Composable
fun LoginScreen() {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    LoginContent(
        uiState = uiState,
        onEmailChange = { viewModel.onAction(LoginAction.EmailChanged(it)) },
        onPasswordChange = { viewModel.onAction(LoginAction.PasswordChanged(it)) },
        onLoginClick = { viewModel.onAction(LoginAction.LoginClicked) }
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold(
        topBar = {
            EnvironmentBadge()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ResponsiveUtils.getResponsivePadding())
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Sign in to your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Login form
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                    ) {
                        // Email field
                        CustomTextField(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            label = "Email",
                            placeholder = "Enter your email",
                            isError = uiState.emailError != null,
                            errorMessage = uiState.emailError,
                            keyboardType = KeyboardType.Email
                        )
                        
                        // Password field
                        CustomTextField(
                            value = uiState.password,
                            onValueChange = onPasswordChange,
                            label = "Password",
                            placeholder = "Enter your password",
                            isError = uiState.passwordError != null,
                            errorMessage = uiState.passwordError,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardType = KeyboardType.Password
                        )
                        
                        // Login button
                        CustomButton(
                            text = "Sign In",
                            onClick = onLoginClick,
                            isLoading = uiState.isLoading,
                            enabled = !uiState.isLoading
                        )
                    }
                }
                
                // Demo credentials info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())
                    ) {
                        Text(
                            text = "Demo Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Email: any valid email format\nPassword: at least 6 characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Error overlay
            if (uiState.error != null && !uiState.isLoading) {
                ErrorView(
                    message = uiState.error,
                    onRetry = onLoginClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    showBackground = true
                )
            }
        }
    }
}
