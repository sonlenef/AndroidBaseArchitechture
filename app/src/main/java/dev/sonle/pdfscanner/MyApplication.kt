package dev.sonle.pdfscanner

import android.app.Application
import android.os.StrictMode
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import dev.sonle.pdfscanner.core.di.appModule
import dev.sonle.pdfscanner.core.di.databaseModule
import dev.sonle.pdfscanner.core.di.dispatcherModule
import dev.sonle.pdfscanner.core.di.firebaseModule
import dev.sonle.pdfscanner.core.di.navigationModule
import dev.sonle.pdfscanner.core.di.networkModule
import dev.sonle.pdfscanner.core.di.repositoryModule
import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.core.config.RemoteConfigManager
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import org.opencv.android.OpenCVLoader
// no inject

class MyApplication : Application() {
    
    val analyticsManager: AnalyticsManager by inject()
    val crashlyticsManager: CrashlyticsManager by inject()
    val remoteConfigManager: RemoteConfigManager by inject()
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModule,
                databaseModule,
                dispatcherModule,
                firebaseModule,
                navigationModule,
                networkModule,
                repositoryModule
            )
        }
        
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
        
        // Initialize OpenCV
        initializeOpenCV()
    }
    
    private fun initializeOpenCV() {
        if (OpenCVLoader.initDebug()) {
            Timber.d("OpenCV initialized successfully")
        } else {
            Timber.e("OpenCV initialization failed")
        }
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
