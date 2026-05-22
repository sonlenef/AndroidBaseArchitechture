package dev.sonle.pdfscanner.core.config

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFlagsTest {

    private val remoteConfigManager: RemoteConfigManager = mockk()
    private val featureFlags = FeatureFlags(remoteConfigManager)

    @Test
    fun `isMaintenanceMode should return remote config value`() {
        every { remoteConfigManager.getBoolean(FeatureFlags.Keys.MAINTENANCE_MODE) } returns true
        assertTrue(featureFlags.isMaintenanceMode())
    }

    @Test
    fun `maintenanceMessage should use default when remote string is blank`() {
        every { remoteConfigManager.getString(FeatureFlags.Keys.MAINTENANCE_MESSAGE) } returns ""
        assertEquals(FeatureFlags.DEFAULT_MAINTENANCE_MESSAGE, featureFlags.maintenanceMessage())
    }

    @Test
    fun `isNewUiEnabled should return remote config value`() {
        every { remoteConfigManager.getBoolean(FeatureFlags.Keys.FEATURE_NEW_UI) } returns false
        assertFalse(featureFlags.isNewUiEnabled())
    }
}
