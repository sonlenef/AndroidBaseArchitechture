package dev.sonle.androidbasearchitechture.core.network

/**
 * Sealed class representing the result of a network operation
 */
sealed class NetworkResult<out T> {
    
    /**
     * Represents a successful network operation with data
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()
    
    /**
     * Represents a failed network operation with error message
     */
    data class Error(val message: String, val throwable: Throwable? = null) : NetworkResult<Nothing>()
    
    /**
     * Represents a loading state during network operation
     */
    object Loading : NetworkResult<Nothing>()
    
    /**
     * Returns true if the result is successful
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Returns true if the result is an error
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Returns true if the result is loading
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Returns the data if successful, null otherwise
     */
    fun getDataOrNull(): T? = if (this is Success) data else null
    
    /**
     * Returns the error message if error, null otherwise
     */
    fun getErrorMessageOrNull(): String? = if (this is Error) message else null
}

/**
 * Extension function to map the data of a successful result
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> this
        is NetworkResult.Loading -> this
    }
}

/**
 * Extension function to handle different result states
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * Extension function to handle error states
 */
inline fun <T> NetworkResult<T>.onError(action: (String, Throwable?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message, throwable)
    }
    return this
}

/**
 * Extension function to handle loading states
 */
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}
