package dev.sonle.pdfscanner.core.review

/**
 * Debug-only flag set via adb:
 * `adb shell am start -n dev.sonle.pdfscanner.dev/dev.sonle.pdfscanner.MainActivity --ez debug_show_review true`
 */
object ReviewPromptDebug {
    var forceShowOnHome: Boolean = false
}
