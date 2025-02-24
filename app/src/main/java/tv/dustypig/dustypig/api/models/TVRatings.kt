package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(TVRatings.JsonAdapter::class)
enum class TVRatings (val value: Int) {
    None(0),
    Y(1) { override fun toString() = "TV-Y" },
    Y7(2) { override fun toString() = "TV-Y7" },
    G(3) { override fun toString() = "TV-G" },
    PG(4) { override fun toString() = "TV-PG" },
    TV_14(5) { override fun toString() = "TV-14" },
    MA(6) { override fun toString() = "TV-MA" },
    NotRated(7) { override fun toString() = "Not Rated" };

    class JsonAdapter : TypeAdapter<TVRatings>() {
        override fun write(writer: JsonWriter?, value: TVRatings?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): TVRatings? {
            val value = reader?.nextInt() ?: -1
            return TVRatings.values().firstOrNull { it.value == value }
        }
    }
}
