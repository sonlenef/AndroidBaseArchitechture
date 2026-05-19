package dev.sonle.pdfscanner.presentation.features.main.settings

import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.DocumentFilterPreset
import dev.sonle.pdfscanner.domain.model.ScanPageLayout
import dev.sonle.pdfscanner.domain.model.ScannerCaptureMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ImageFilter
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannerMode

object ScannerSettingsMapper {

    fun AppSettings.toScannerMode(): ScannerMode = when (defaultCaptureMode) {
        ScannerCaptureMode.AUTO -> ScannerMode.AUTO
        ScannerCaptureMode.MANUAL -> ScannerMode.MANUAL
    }

    fun AppSettings.toPageMode(): PageMode = when (defaultPageLayout) {
        ScanPageLayout.SINGLE -> PageMode.SINGLE
        ScanPageLayout.MULTI -> PageMode.MULTI
    }

    fun AppSettings.toDefaultImageFilter(): ImageFilter = when (defaultFilter) {
        DocumentFilterPreset.ORIGINAL -> ImageFilter.ORIGINAL
        DocumentFilterPreset.BLACK_WHITE -> ImageFilter.BLACK_WHITE
        DocumentFilterPreset.GRAYSCALE -> ImageFilter.GRAYSCALE
        DocumentFilterPreset.MAGIC_COLOR -> ImageFilter.MAGIC_COLOR
        DocumentFilterPreset.SHARPEN -> ImageFilter.SHARPEN
    }

    fun ScannerMode.toCaptureMode(): ScannerCaptureMode = when (this) {
        ScannerMode.AUTO -> ScannerCaptureMode.AUTO
        ScannerMode.MANUAL -> ScannerCaptureMode.MANUAL
    }

    fun PageMode.toPageLayout(): ScanPageLayout = when (this) {
        PageMode.SINGLE -> ScanPageLayout.SINGLE
        PageMode.MULTI -> ScanPageLayout.MULTI
    }

    fun ImageFilter.toFilterPreset(): DocumentFilterPreset = when (this) {
        ImageFilter.ORIGINAL -> DocumentFilterPreset.ORIGINAL
        ImageFilter.BLACK_WHITE -> DocumentFilterPreset.BLACK_WHITE
        ImageFilter.GRAYSCALE -> DocumentFilterPreset.GRAYSCALE
        ImageFilter.MAGIC_COLOR -> DocumentFilterPreset.MAGIC_COLOR
        ImageFilter.SHARPEN -> DocumentFilterPreset.SHARPEN
    }
}
