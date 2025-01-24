package tv.dustypig.dustypig.api.models

data class Result(
    val success: Boolean,
    val error: String
)

data class ResultOf<T>(
    val success: Boolean,
    val error: String,
    val data: T?
)