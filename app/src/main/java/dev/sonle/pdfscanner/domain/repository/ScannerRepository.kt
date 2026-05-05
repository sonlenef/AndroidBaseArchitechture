package dev.sonle.pdfscanner.domain.repository

import android.graphics.Bitmap
import java.io.File

interface ScannerRepository {
    /**
     * Chuyển đổi danh sách Bitmap thành một file PDF duy nhất.
     * @param bitmaps Danh sách ảnh đã xử lý.
     * @param fileName Tên file PDF muốn lưu.
     * @return File PDF đã được tạo.
     */
    suspend fun generatePdf(bitmaps: List<Bitmap>, fileName: String): Result<File>
}
