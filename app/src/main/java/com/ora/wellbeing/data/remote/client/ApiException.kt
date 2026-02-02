package com.ora.wellbeing.data.remote.client

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NetworkException(message: String, cause: Throwable? = null) : ApiException(message, cause)

    class ServerException(message: String, cause: Throwable? = null) : ApiException(message, cause)

    class AuthenticationException(message: String, cause: Throwable? = null) : ApiException(message, cause)

    class ClientException(message: String, cause: Throwable? = null) : ApiException(message, cause)

    class UnknownException(message: String, cause: Throwable? = null) : ApiException(message, cause)
}