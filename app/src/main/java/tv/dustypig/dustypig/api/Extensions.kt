package tv.dustypig.dustypig.api

import retrofit2.Response
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf


inline fun <reified T> Response<T>.throwIfError(){

    if(!this.isSuccessful)
        throw Exception(this.message())

    if(this.body() == null)
        return

    //Someday, figure out why if I assign bool vals, the if statement works
    //correctly, but if I call isAssignableFrom directly in the if statement
    //then EVERYTHING evaluates to true. Gotta be something to do with inlining

    val rw = T::class.java.isAssignableFrom(ResponseWrapper::class.java)
    val wrOf = T::class.java.isAssignableFrom(ResponseWrapperOf::class.java)

    if(wrOf)
        (this.body()!! as ResponseWrapperOf<Any>).throwIfError()
    else if(rw)
        (this.body()!! as ResponseWrapper).throwIfError()
}
