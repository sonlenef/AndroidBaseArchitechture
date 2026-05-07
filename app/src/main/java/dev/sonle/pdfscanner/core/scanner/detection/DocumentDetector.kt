package dev.sonle.pdfscanner.core.scanner.detection

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import java.nio.ByteBuffer

interface DocumentDetector {
    suspend fun detect(
        rgbaBuffer: ByteBuffer,
        width: Int,
        height: Int
    ): DocumentQuad?
}
