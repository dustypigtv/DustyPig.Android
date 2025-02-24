package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(TMDBMediaTypes.JsonAdapter::class)
enum class TMDBMediaTypes (val value: Int) {
    Movie(0),
    Series(1);

    class JsonAdapter : TypeAdapter<TMDBMediaTypes>() {
        override fun write(writer: JsonWriter?, value: TMDBMediaTypes?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): TMDBMediaTypes? {
            val value = reader?.nextInt() ?: -1
            return TMDBMediaTypes.values().firstOrNull { it.value == value }
        }
    }
}
