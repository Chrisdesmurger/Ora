package com.ora.wellbeing.data.remote.client

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (ApiException) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(exception)
    return this
}

fun <T> ApiResult<T>.getOrNull(): T? {
    return when (this) {
        is ApiResult.Success -> data
        else -> null
    }
}

fun <T> ApiResult<T>.getOrThrow(): T {
    return when (this) {
        is ApiResult.Success -> data
        is ApiResult.Error -> throw exception
        is ApiResult.Loading -> throw IllegalStateException("Result is still loading")
    }
}