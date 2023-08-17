package tv.dustypig.dustypig.api

import retrofit2.Response
import tv.dustypig.dustypig.api.models.Ratings
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import kotlin.math.roundToInt


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

fun Double.toTimeString(): String {
    val hours = (this / 3600).toInt()
    val minutes = ((this % 3600) / 60).roundToInt()

    var ret: String = ""
    if(hours > 0) {
        ret = hours.toString() + "h"
        if(minutes > 0)
            ret += " $minutes" + "m"
    } else if(minutes > 0) {
        ret = minutes.toString() + "m"
    }

    return ret
}

fun Ratings.asString(): String {

    return when(this) {
        Ratings.G -> "G"
        Ratings.PG -> "PG"
        Ratings.PG_13 -> "PG-13"
        Ratings.R -> "R"
        Ratings.NC_17 -> "NC-17"
        Ratings.Unrated -> "Unrated"
        Ratings.TV_Y -> "TV-Y"
        Ratings.TV_Y7 -> "TV-Y7"
        Ratings.TV_G -> "TV-G"
        Ratings.TV_PG -> "TV-PG"
        Ratings.TV_14 -> "TV-14"
        Ratings.TV_MA -> "TV-MA"
        else -> "Not Rated"
    }
}

