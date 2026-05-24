package dev.sonle.pdfscanner.data.repository

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import dev.sonle.pdfscanner.domain.model.PdfViewerDocument
import dev.sonle.pdfscanner.domain.model.PdfViewerError
import dev.sonle.pdfscanner.domain.repository.PdfViewerException
import dev.sonle.pdfscanner.domain.repository.PdfViewerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * [PdfRenderer]-backed repository with a memory-bounded LRU page cache.
 * Only a small number of rendered pages are kept in memory at once.
 */
class PdfViewerRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher
) : PdfViewerRepository {

    private val sessionLock = Any()

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var document: PdfViewerDocument? = null

    private val pageCache: LruCache<Int, Bitmap> = object : LruCache<Int, Bitmap>(maxCacheBytes()) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount

        override fun entryRemoved(
            evicted: Boolean,
            key: Int,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (evicted && !oldValue.isRecycled) {
                oldValue.recycle()
            }
        }
    }

    override suspend fun openDocument(absolutePath: String): Result<PdfViewerDocument> =
        withContext(ioDispatcher) {
            synchronized(sessionLock) {
                closeLocked()
                val file = File(absolutePath)
                if (!file.exists() || !file.isFile) {
                    return@withContext Result.failure(
                        PdfViewerException(PdfViewerError.FileNotFound)
                    )
                }
                try {
                    val descriptor = ParcelFileDescriptor.open(
                        file,
                        ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = PdfRenderer(descriptor)
                    if (renderer.pageCount <= 0) {
                        renderer.close()
                        descriptor.close()
                        return@withContext Result.failure(
                            PdfViewerException(PdfViewerError.OpenFailed)
                        )
                    }
                    val opened = PdfViewerDocument(
                        fileName = file.name,
                        absolutePath = file.absolutePath,
                        pageCount = renderer.pageCount,
                        fileSizeBytes = file.length()
                    )
                    fileDescriptor = descriptor
                    pdfRenderer = renderer
                    document = opened
                    Result.success(opened)
                } catch (e: IOException) {
                    Timber.w(e, "Failed to open PDF: %s", absolutePath)
                    closeLocked()
                    Result.failure(PdfViewerException(PdfViewerError.OpenFailed))
                } catch (e: SecurityException) {
                    Timber.w(e, "Security error opening PDF: %s", absolutePath)
                    closeLocked()
                    Result.failure(PdfViewerException(PdfViewerError.OpenFailed))
                }
            }
        }

    override suspend fun renderPage(
        pageIndex: Int,
        targetWidthPx: Int
    ): Result<Bitmap> = withContext(ioDispatcher) {
        synchronized(sessionLock) {
            val renderer = pdfRenderer
                ?: return@withContext Result.failure(
                    PdfViewerException(PdfViewerError.OpenFailed)
                )
            if (pageIndex !in 0 until renderer.pageCount) {
                return@withContext Result.failure(
                    PdfViewerException(PdfViewerError.RenderFailed(pageIndex))
                )
            }
            pageCache.get(pageIndex)?.let { cached ->
                if (!cached.isRecycled) {
                    return@withContext Result.success(cached)
                }
                pageCache.remove(pageIndex)
            }
            try {
                val page = renderer.openPage(pageIndex)
                try {
                    val scale = (targetWidthPx.toFloat() / page.width.toFloat())
                        .coerceAtLeast(MIN_RENDER_SCALE)
                        .coerceAtMost(MAX_RENDER_SCALE)
                    val width = (page.width * scale).toInt().coerceAtLeast(1)
                    val height = (page.height * scale).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pageCache.put(pageIndex, bitmap)
                    Result.success(bitmap)
                } finally {
                    page.close()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to render PDF page %d", pageIndex)
                Result.failure(PdfViewerException(PdfViewerError.RenderFailed(pageIndex)))
            }
        }
    }

    override fun releasePage(pageIndex: Int) {
        synchronized(sessionLock) {
            pageCache.remove(pageIndex)
        }
    }

    override fun releasePagesExcept(keepPageIndices: Set<Int>) {
        synchronized(sessionLock) {
            val snapshot = pageCache.snapshot()
            snapshot.keys.filter { it !in keepPageIndices }.forEach { pageCache.remove(it) }
        }
    }

    override fun close() {
        synchronized(sessionLock) {
            closeLocked()
        }
    }

    private fun closeLocked() {
        pageCache.evictAll()
        pdfRenderer?.close()
        pdfRenderer = null
        try {
            fileDescriptor?.close()
        } catch (e: IOException) {
            Timber.w(e, "Error closing PDF file descriptor")
        }
        fileDescriptor = null
        document = null
    }

    private fun maxCacheBytes(): Int {
        val maxMemory = Runtime.getRuntime().maxMemory().toInt()
        return (maxMemory / CACHE_MEMORY_FRACTION).coerceIn(MIN_CACHE_BYTES, MAX_CACHE_BYTES)
    }

    companion object {
        private const val CACHE_MEMORY_FRACTION = 8
        private const val MIN_CACHE_BYTES = 4 * 1024 * 1024
        private const val MAX_CACHE_BYTES = 32 * 1024 * 1024
        private const val MIN_RENDER_SCALE = 0.5f
        private const val MAX_RENDER_SCALE = 3f
    }
}
