package com.alirnp.domain.core

sealed class Resource<T>(open val data: T? = null, open val message: String? = null) {
    class Loading<T> : Resource<T>()
    class Success<T>(override val data: T) : Resource<T>(data = data)
    class Error<T>(override val message: String) : Resource<T>(message = message)
}
