package dev.sonle.pdfscanner.presentation.features.scanner.model

import dev.sonle.pdfscanner.R

/**
 * Available image filters for scanned documents.
 */
enum class ImageFilter(val labelResId: Int) {
    /** No filter applied — original colors */
    ORIGINAL(R.string.scanner_filter_original),
    /** Adaptive threshold black & white — best for text documents */
    BLACK_WHITE(R.string.scanner_filter_bw),
    /** Simple grayscale conversion */
    GRAYSCALE(R.string.scanner_filter_grayscale),
    /** Enhanced color using CLAHE — vivid color correction */
    MAGIC_COLOR(R.string.scanner_filter_magic),
    /** Unsharp mask sharpening — enhances detail clarity */
    SHARPEN(R.string.scanner_filter_sharpen)
}
