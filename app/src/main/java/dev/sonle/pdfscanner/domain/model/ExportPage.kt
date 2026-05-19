package dev.sonle.pdfscanner.domain.model

/**
 * Platform-neutral page payload for PDF export (ARGB pixels, row-major).
 */
data class ExportPage(
    val width: Int,
    val height: Int,
    val pixels: IntArray
) {
    init {
        require(width > 0 && height > 0) { "Export page dimensions must be positive" }
        require(pixels.size == width * height) {
            "Pixel buffer size must equal width * height"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ExportPage
        return width == other.width &&
            height == other.height &&
            pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}
