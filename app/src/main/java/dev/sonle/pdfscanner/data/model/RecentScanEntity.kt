package dev.sonle.pdfscanner.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recent_scans",
    indices = [Index(value = ["filePath"], unique = true)]
)
data class RecentScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val savedAt: Long
)
