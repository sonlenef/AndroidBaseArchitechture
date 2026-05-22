package dev.sonle.pdfscanner.domain.util

/**
 * Normalizes user-provided document names into safe PDF file names.
 */
object DocumentFileNameNormalizer {

    private val INVALID_CHARS = Regex("""[\\/:*?"<>|]""")
    private const val MAX_FILE_NAME_LENGTH = 120

    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val withoutExtension = trimmed.removeSuffix(".pdf").removeSuffix(".PDF").trim()
        if (withoutExtension.isEmpty()) return null

        val sanitized = withoutExtension
            .replace(INVALID_CHARS, "_")
            .replace(Regex("""\s+"""), " ")
            .trim()
        if (sanitized.isEmpty()) return null

        val fileName = "$sanitized.pdf"
        return fileName.takeIf { it.length <= MAX_FILE_NAME_LENGTH }
    }

    fun displayNameWithoutExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
    }
}
