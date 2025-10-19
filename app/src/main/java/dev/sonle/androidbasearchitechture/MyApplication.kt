package dev.sonle.androidbasearchitechture

import android.app.Application
import android.os.StrictMode
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.hilt.android.HiltAndroidApp
import dev.sonle.androidbasearchitechture.core.analytics.AnalyticsManager
import dev.sonle.androidbasearchitechture.core.config.EnvironmentConfig
import dev.sonle.androidbasearchitechture.core.config.RemoteConfigManager
import dev.sonle.androidbasearchitechture.core.crashlytics.CrashlyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class with Hilt dependency injection and Firebase initialization
 */
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    
    @Inject
    lateinit var crashlyticsManager: CrashlyticsManager
    
    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Configure environment-specific settings
        configureEnvironment()
        
        // Initialize logging
        initializeLogging()
        
        // Configure Firebase services based on environment
        configureFirebaseServices()
        
        // Initialize Firebase managers
        initializeFirebaseManagers()
    }
    
    private fun configureEnvironment() {
        if (EnvironmentConfig.enableStrictMode) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
    
    private fun initializeLogging() {
        if (EnvironmentConfig.enableDebugLogging) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("Application started in ${EnvironmentConfig.environment} environment")
    }
    
    private fun configureFirebaseServices() {
        // Configure Crashlytics
        if (!EnvironmentConfig.crashlyticsEnabled) {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        }
        
        // Configure Performance Monitoring
        if (!EnvironmentConfig.performanceMonitoringEnabled) {
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = false
        }
        
        Timber.d("Firebase services configured - Crashlytics: ${EnvironmentConfig.crashlyticsEnabled}, " +
                "Analytics: ${EnvironmentConfig.analyticsEnabled}, Performance: ${EnvironmentConfig.performanceMonitoringEnabled}")
    }
    
    private fun initializeFirebaseManagers() {
        applicationScope.launch {
            try {
                // Initialize Remote Config
                remoteConfigManager.initialize()
                remoteConfigManager.fetchAndActivate()
                
                // Set up Crashlytics environment info
                crashlyticsManager.setEnvironmentInfo()
                
                // Log app start event
                analyticsManager.logEvent("app_started", mapOf(
                    "environment" to EnvironmentConfig.environment,
                    "version" to "1.0.0"
                ))
                
                Timber.d("Firebase managers initialized successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize Firebase managers")
            }
        }
    }
}
