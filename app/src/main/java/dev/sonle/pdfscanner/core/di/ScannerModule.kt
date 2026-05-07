package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.core.scanner.detection.DocumentDetector
import dev.sonle.pdfscanner.core.scanner.detection.TFLiteDocumentDetector
import dev.sonle.pdfscanner.core.scanner.postprocess.QuadExtractor
import dev.sonle.pdfscanner.core.scanner.postprocess.SubpixelCornerRefiner
import dev.sonle.pdfscanner.core.scanner.smoothing.QuadKalmanSmoother
import dev.sonle.pdfscanner.core.scanner.stability.StabilityTracker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val scannerModule = module {
    single { QuadExtractor() }
    factory { SubpixelCornerRefiner() }
    factory { QuadKalmanSmoother() }
    factory { StabilityTracker() }
    single<DocumentDetector> { TFLiteDocumentDetector(androidContext(), get()) }
}
