package dev.sonle.pdfscanner.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AnalyticsManagerTest {

    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private val analyticsManager = AnalyticsManager(firebaseAnalytics)

    @Test
    fun `setCollectionEnabled should update Firebase Analytics`() {
        analyticsManager.setCollectionEnabled(false)
        verify { firebaseAnalytics.setAnalyticsCollectionEnabled(false) }
    }
}
