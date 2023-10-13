package tv.dustypig.dustypig.api.models

data class ResponseWrapper (
    val success: Boolean,
    val error: String
)

data class ResponseWrapperOf<T> (
    val success: Boolean,
    val error: String,
    val data: T?
)