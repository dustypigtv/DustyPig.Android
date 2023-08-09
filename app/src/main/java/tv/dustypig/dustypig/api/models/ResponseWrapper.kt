package tv.dustypig.dustypig.api.models

data class ResponseWrapper (
    val success: Boolean,
    val error: String
){
    fun throwIfError() {
        if(!success)
            throw Exception(error)
    }
}

data class ResponseWrapperOf<T> (
    val success: Boolean,
    val error: String,
    val data: T
){
    fun throwIfError() {
        if(!success)
            throw Exception(error)
    }
}

