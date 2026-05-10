package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode

/** Single-page flow should reset when leaving review; multi-page keeps the stack on camera. */
internal fun clearsScanSessionOnReviewBack(mode: PageMode): Boolean =
    mode == PageMode.SINGLE
