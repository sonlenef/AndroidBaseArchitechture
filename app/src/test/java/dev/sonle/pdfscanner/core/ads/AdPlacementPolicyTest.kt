package dev.sonle.pdfscanner.core.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPlacementPolicyTest {

    @Test
    fun `allowsBanner only on home and settings`() {
        assertTrue(AdPlacementPolicy.allowsBanner(AdPlacementPolicy.Screen.HOME))
        assertTrue(AdPlacementPolicy.allowsBanner(AdPlacementPolicy.Screen.SETTINGS))
        assertFalse(AdPlacementPolicy.allowsBanner(AdPlacementPolicy.Screen.SCANNER))
        assertFalse(AdPlacementPolicy.allowsBanner(AdPlacementPolicy.Screen.PDF_VIEWER))
        assertFalse(AdPlacementPolicy.allowsBanner(AdPlacementPolicy.Screen.SAVE_SUCCESS))
    }

    @Test
    fun `shouldHideBannerForSelectionMode when selecting documents`() {
        assertTrue(AdPlacementPolicy.shouldHideBannerForSelectionMode(isSelectionMode = true))
        assertFalse(AdPlacementPolicy.shouldHideBannerForSelectionMode(isSelectionMode = false))
    }
}
