package dev.sonle.pdfscanner.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import dev.sonle.pdfscanner.domain.repository.ScannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ScannerRepositoryImpl(
    private val context: Context
) : ScannerRepository {

    override suspend fun generatePdf(bitmaps: List<Bitmap>, fileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            
            bitmaps.forEachIndexed { index, bitmap ->
                // Tạo thông tin trang (kích thước bằng kích thước ảnh)
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                
                // Vẽ bitmap vào trang PDF
                val canvas: Canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                
                pdfDocument.finishPage(page)
            }

            // Lưu file vào thư mục Documents (Scoped Storage)
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf")
            
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            
            pdfDocument.close()
            outputStream.close()
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
