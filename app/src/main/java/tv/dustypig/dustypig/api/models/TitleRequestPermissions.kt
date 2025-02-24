package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(TitleRequestPermissions.JsonAdapter::class)
enum class TitleRequestPermissions (val value: Int) {
    Enabled(0),
    Disabled(1),
    RequiresAuthorization(2);

    class JsonAdapter : TypeAdapter<TitleRequestPermissions>() {
        override fun write(writer: JsonWriter?, value: TitleRequestPermissions?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): TitleRequestPermissions? {
            val value = reader?.nextInt() ?: -1
            return TitleRequestPermissions.values().firstOrNull { it.value == value }
        }
    }
}