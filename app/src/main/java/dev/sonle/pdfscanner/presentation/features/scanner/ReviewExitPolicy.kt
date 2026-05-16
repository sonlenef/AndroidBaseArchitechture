package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode

/** Single-page flow should reset when leaving review; multi-page keeps the stack on camera. */
internal fun clearsScanSessionOnReviewBack(mode: PageMode): Boolean =
    mode == PageMode.SINGLE

/**
 * After a page is committed from capture (processed photo or filter editor confirm),
 * single mode opens Page Review; multi mode returns to the camera to continue scanning.
 */
internal fun opensPageReviewAfterNewPageCommitted(mode: PageMode): Boolean =
    mode == PageMode.SINGLE

/**
 * After confirming the filter step: same as [opensPageReviewAfterNewPageCommitted] for new pages,
 * but multi mode still opens review when the user was editing an existing page from Page Review.
 */
internal fun opensPageReviewAfterFilterConfirm(mode: PageMode, wasEditingExistingPage: Boolean): Boolean =
    opensPageReviewAfterNewPageCommitted(mode) || (mode == PageMode.MULTI && wasEditingExistingPage)
