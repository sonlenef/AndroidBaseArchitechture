package dev.sonle.pdfscanner.domain.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val defaultCaptureMode: ScannerCaptureMode = ScannerCaptureMode.AUTO,
    val defaultPageLayout: ScanPageLayout = ScanPageLayout.SINGLE,
    val defaultFilter: DocumentFilterPreset = DocumentFilterPreset.SHARPEN,
    val pdfOutputQuality: PdfOutputQuality = PdfOutputQuality.STANDARD
) {
    companion object {
        val Default = AppSettings()
    }
}
