package dev.sonle.pdfscanner.core.ads

/**
 * Defines where ads may appear so we stay within [Google AdMob policy](https://support.google.com/admob/answer/6128543):
 * - No ads on primary-task screens (camera, crop, filter, PDF reading)
 * - No ads adjacent to interactive controls (FAB, scan button, selection actions)
 * - Banners only on stable, non-blocking surfaces with adequate spacing
 */
object AdPlacementPolicy {

    /** Minimum spacing between banner and nearest clickable UI (policy: avoid accidental clicks). */
    const val MIN_CLICKABLE_SEPARATION_DP = 16

    /** Reserved height for adaptive banner + separation above bottom nav. */
    const val BANNER_SLOT_HEIGHT_DP = 68

    enum class Screen {
        HOME,
        SETTINGS,
        SCANNER,
        PDF_VIEWER,
        SAVE_SUCCESS
    }

    enum class AdFormat {
        BANNER,
        INTERSTITIAL
    }

    fun allowsBanner(screen: Screen): Boolean =
        screen == Screen.HOME || screen == Screen.SETTINGS

    fun allowsInterstitial(screen: Screen): Boolean = false

    /**
     * Interstitial only after user explicitly leaves scanner flow via "Back to home"
     * on save success — a natural break, not mid-scan.
     */
    fun allowsPostScanInterstitial(): Boolean = true

    fun shouldHideBannerForSelectionMode(isSelectionMode: Boolean): Boolean = isSelectionMode
}
