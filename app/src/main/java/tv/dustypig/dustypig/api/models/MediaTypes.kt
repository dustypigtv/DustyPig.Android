package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(MediaTypes.JsonAdapter::class)
enum class MediaTypes (val value: Int) {
    Movie(1),
    Series(2),
    Episode(3),
    Playlist(4);

    companion object {

        fun getByVal(value: String?): MediaTypes? {
            val intVal = value?.toIntOrNull() ?: return null
            return MediaTypes.values().firstOrNull { it.value == intVal }
        }

    }

    class JsonAdapter : TypeAdapter<MediaTypes>() {
        override fun write(writer: JsonWriter?, value: MediaTypes?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): MediaTypes? {
            val value = reader?.nextInt() ?: -1
            return MediaTypes.values().firstOrNull { it.value == value }
        }
    }
}