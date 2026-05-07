package dev.sonle.pdfscanner.core.scanner.model

data class NormalizedPoint(
    val x: Float,
    val y: Float
)

data class DocumentQuad(
    val tl: NormalizedPoint,
    val tr: NormalizedPoint,
    val br: NormalizedPoint,
    val bl: NormalizedPoint,
    val confidence: Float
) {
    fun points(): List<NormalizedPoint> = listOf(tl, tr, br, bl)
}
