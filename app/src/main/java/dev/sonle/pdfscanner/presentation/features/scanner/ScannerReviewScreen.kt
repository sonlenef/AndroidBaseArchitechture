package dev.sonle.pdfscanner.presentation.features.scanner

import androidx.compose.runtime.Composable
import dev.sonle.pdfscanner.presentation.features.scanner.components.PageReviewView
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannedPage

@Composable
fun ScannerReviewScreen(
    pages: List<ScannedPage>,
    selectedPageIndex: Int,
    pageMode: PageMode,
    onSelectPage: (Int) -> Unit,
    onEditPage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onAddMorePages: () -> Unit,
    onSavePdf: () -> Unit,
    onBack: () -> Unit
) {
    PageReviewView(
        pages = pages,
        selectedPageIndex = selectedPageIndex,
        pageMode = pageMode,
        onSelectPage = onSelectPage,
        onEditPage = onEditPage,
        onDeletePage = onDeletePage,
        onAddMorePages = onAddMorePages,
        onSavePdf = onSavePdf,
        onBack = onBack
    )
}
