package dev.sonle.pdfscanner.domain.repository

import dev.sonle.pdfscanner.domain.model.RecentScan

sealed class RecentScanRenameResult {
    data class Success(val scan: RecentScan) : RecentScanRenameResult()
    data object NotFound : RecentScanRenameResult()
    data object InvalidName : RecentScanRenameResult()
    data object NameAlreadyExists : RecentScanRenameResult()
    data object Failed : RecentScanRenameResult()
}
