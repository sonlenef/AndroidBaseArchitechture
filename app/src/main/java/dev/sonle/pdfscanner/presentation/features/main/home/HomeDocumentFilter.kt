package dev.sonle.pdfscanner.presentation.features.main.home

import dev.sonle.pdfscanner.domain.model.RecentScan

/**
 * Filters the home document list by user search query (file name, case-insensitive).
 */
internal object HomeDocumentFilter {

    fun filter(scans: List<RecentScan>, query: String): List<RecentScan> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return scans
        return scans.filter { scan -> scan.fileName.lowercase().contains(normalizedQuery) }
    }
}
