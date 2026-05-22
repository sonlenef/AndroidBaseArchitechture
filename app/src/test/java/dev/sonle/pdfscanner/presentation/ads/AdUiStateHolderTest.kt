package dev.sonle.pdfscanner.presentation.ads

import dev.sonle.pdfscanner.core.config.FeatureFlags
import dev.sonle.pdfscanner.core.config.RemoteConfigManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdUiStateHolderTest {

    private lateinit var remoteConfigManager: RemoteConfigManager
    private lateinit var featureFlags: FeatureFlags
    private lateinit var holder: AdUiStateHolder

    @Before
    fun setup() {
        remoteConfigManager = mockk(relaxed = true)
        featureFlags = FeatureFlags(remoteConfigManager)
        holder = AdUiStateHolder(featureFlags)
    }

    @Test
    fun `adsEnabled is true when remote flags allow banner ads`() {
        every { remoteConfigManager.getBoolean("enable_ads") } returns true
        every { remoteConfigManager.getBoolean("enable_banner_ads") } returns true

        holder.refresh()

        assertTrue(holder.adsEnabled)
    }

    @Test
    fun `refresh updates adsEnabled when remote disables ads`() {
        every { remoteConfigManager.getBoolean("enable_ads") } returns true
        every { remoteConfigManager.getBoolean("enable_banner_ads") } returns true
        holder.refresh()
        assertTrue(holder.adsEnabled)

        every { remoteConfigManager.getBoolean("enable_ads") } returns false
        holder.refresh()

        assertFalse(holder.adsEnabled)
    }
}
