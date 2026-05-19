package dev.sonle.pdfscanner.data.repository

import androidx.room.Room
import dev.sonle.pdfscanner.TestData
import dev.sonle.pdfscanner.core.database.AppDatabase
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class RecentScanRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RecentScanRepositoryImpl
    private lateinit var documentsDir: File

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RecentScanRepositoryImpl(database.recentScanDao(), context)
        documentsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)!!
        documentsDir.mkdirs()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `deleteRecentScanById should remove db row and delete pdf file`() = runTest {
        val pdfFile = File(documentsDir, "delete_me.pdf").apply {
            writeText("pdf")
        }
        val scan = TestData.sampleRecentScan.copy(
            id = 0,
            filePath = pdfFile.absolutePath
        )
        repository.upsertRecentScan(scan)
        val saved = repository.observeRecentScans().first().first()

        val result = repository.deleteRecentScanById(saved.id)

        assertEquals(RecentScanDeleteResult.Success, result)
        assertTrue(repository.observeRecentScans().first().isEmpty())
        assertFalse(pdfFile.exists())
    }

    @Test
    fun `upsertRecentScan should prune oldest when exceeding max`() = runTest {
        repeat(RecentScanRepositoryImpl.MAX_RECENT_SCANS + 3) { index ->
            val file = File(documentsDir, "scan_$index.pdf").apply { writeText("x") }
            repository.upsertRecentScan(
                TestData.sampleRecentScan.copy(
                    id = 0,
                    fileName = "scan_$index.pdf",
                    filePath = file.absolutePath,
                    savedAt = index.toLong()
                )
            )
        }

        val items = repository.observeRecentScans().first()
        assertEquals(RecentScanRepositoryImpl.MAX_RECENT_SCANS, items.size)
    }
}
