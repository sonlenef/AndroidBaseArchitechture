package dev.sonle.pdfscanner.core.config

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteConfigManagerTest {

    private val firebaseRemoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private val firebaseCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val firebasePerformance: FirebasePerformance = mockk(relaxed = true)

    private val manager = RemoteConfigManager(
        firebaseRemoteConfig,
        firebaseAnalytics,
        firebaseCrashlytics,
        firebasePerformance
    )

    @Before
    fun setup() {
        mockkObject(EnvironmentConfig)
        every { EnvironmentConfig.firebaseConfig } returns FirebaseConfig(
            crashlyticsEnabled = true,
            analyticsEnabled = true,
            performanceMonitoringEnabled = true,
            fcmEnabled = true,
            firebaseAuthEnabled = true,
            firestoreEnabled = true,
            firebaseStorageEnabled = true,
            remoteConfigEnabled = true
        )
        every { EnvironmentConfig.analyticsEnabled } returns true
        every { EnvironmentConfig.crashlyticsEnabled } returns true
        every { EnvironmentConfig.performanceMonitoringEnabled } returns true
    }

    @After
    fun tearDown() {
        unmockkObject(EnvironmentConfig)
    }

    @Test
    fun `applyRemoteServiceFlags should sync analytics when remote disables collection`() {
        every { firebaseRemoteConfig.getBoolean(FeatureFlags.Keys.ENABLE_ANALYTICS) } returns false
        every { firebaseRemoteConfig.getBoolean(FeatureFlags.Keys.ENABLE_CRASHLYTICS) } returns true
        every { firebaseRemoteConfig.getBoolean(FeatureFlags.Keys.ENABLE_PERFORMANCE_MONITORING) } returns true

        manager.applyRemoteServiceFlags()

        verify { firebaseAnalytics.setAnalyticsCollectionEnabled(false) }
    }

    @Test
    fun `getBoolean should delegate to FirebaseRemoteConfig`() {
        every { firebaseRemoteConfig.getBoolean("feature_new_ui") } returns true
        assertTrue(manager.getBoolean("feature_new_ui"))
    }
}
