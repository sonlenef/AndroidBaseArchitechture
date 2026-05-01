package dev.sonle.androidbasearchitecture.core.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber

/**
 * String extensions
 */
fun String.isValidEmail(): Boolean = ValidationUtils.isValidEmail(this)

fun String.isValidPassword(): Boolean = ValidationUtils.isValidPassword(this)

fun String.isValidName(): Boolean = ValidationUtils.isValidName(this)

fun String.isValidPhone(): Boolean = ValidationUtils.isValidPhone(this)

fun String.isValidWebsite(): Boolean = ValidationUtils.isValidWebsite(this)

fun String.isNotBlank(): Boolean = ValidationUtils.isNotBlank(this)

fun String.trimAndValidate(): String = this.trim().takeIf { it.isNotBlank() } ?: ""

/**
 * Context extensions
 */
fun Context.getSharedPreferences(): SharedPreferences {
    return getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
}

/**
 * Flow extensions
 */
fun <T> Flow<T>.logErrors(tag: String = "Flow"): Flow<T> {
    return this.catch { throwable ->
        Timber.e(throwable, "Error in $tag flow")
        throw throwable
    }
}

/**
 * Safe flow creation with error handling
 */
fun <T> safeFlow(block: suspend () -> T): Flow<T> {
    return flow {
        try {
            emit(block())
        } catch (e: Exception) {
            Timber.e(e, "Error in safe flow")
            throw e
        }
    }
}

/**
 * String utility extensions
 */
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

fun String.removeExtraSpaces(): String {
    return this.trim().replace(Regex("\\s+"), " ")
}

fun String.maskEmail(): String {
    if (!this.isValidEmail()) return this
    val parts = this.split("@")
    if (parts.size != 2) return this
    
    val username = parts[0]
    val domain = parts[1]
    
    return when {
        username.length <= 2 -> this
        username.length <= 4 -> "${username.first()}***@$domain"
        else -> "${username.take(2)}***${username.takeLast(1)}@$domain"
    }
}

fun String.maskPhone(): String {
    if (this.length < 4) return this
    return "${this.take(3)}***${this.takeLast(1)}"
}

/**
 * Number extensions
 */
fun Int.toFormattedString(): String {
    return when {
        this >= 1_000_000 -> "${this / 1_000_000}M"
        this >= 1_000 -> "${this / 1_000}K"
        else -> this.toString()
    }
}

fun Long.toFormattedString(): String {
    return when {
        this >= 1_000_000_000 -> "${this / 1_000_000_000}B"
        this >= 1_000_000 -> "${this / 1_000_000}M"
        this >= 1_000 -> "${this / 1_000}K"
        else -> this.toString()
    }
}

/**
 * Boolean extensions
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

fun Int.toBoolean(): Boolean = this != 0

/**
 * Collection extensions
 */
fun <T> List<T>.isNotEmpty(): Boolean = !this.isEmpty()

fun <T> List<T>.safeGet(index: Int): T? = if (index in 0 until size) get(index) else null

fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return if (size <= 0) emptyList()
    else (0 until this.size step size).map { i ->
        this.subList(i, minOf(i + size, this.size))
    }
}

/**
 * Null safety extensions
 */
fun <T> T?.orDefault(default: T): T = this ?: default

fun <T> T?.orEmpty(): T where T : CharSequence = this ?: "" as T

fun <T> T?.isNotNull(): Boolean = this != null

fun <T> T?.isNull(): Boolean = this == null
