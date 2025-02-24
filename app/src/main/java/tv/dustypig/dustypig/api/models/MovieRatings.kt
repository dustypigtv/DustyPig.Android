package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(MovieRatings.JsonAdapter::class)
enum class MovieRatings (val value: Int) {
    None(0),
    G(1),
    PG(2),
    PG13(3) { override fun toString() = "PG-13" },
    R(4),
    NC17(5) { override fun toString() = "NC-17" },
    Unrated(6);

    class JsonAdapter : TypeAdapter<MovieRatings>() {
        override fun write(writer: JsonWriter?, value: MovieRatings?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): MovieRatings? {
            val value = reader?.nextInt() ?: -1
            return MovieRatings.values().firstOrNull { it.value == value }
        }
    }
}
