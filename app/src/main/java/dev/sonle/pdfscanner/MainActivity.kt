package dev.sonle.pdfscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
// removed hilt
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.presentation.navigation.AppNavigation
import dev.sonle.pdfscanner.presentation.theme.AndroidBaseArchitechtureTheme
import timber.log.Timber

/**
 * Main Activity with Navigation Compose and Hilt dependency injection
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Timber.d("MainActivity created in ${EnvironmentConfig.environment} environment")
        
        setContent {
            AndroidBaseArchitechtureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    AndroidBaseArchitechtureTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Android Base Architecture",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}