package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter


@JsonAdapter(OverrideState.JsonAdapter::class)
enum class OverrideState(val value: Int) {

    //Default is used by the server only. If you try to set it in a call to setTitlePermissions, the server will return a validation error
    //Default(0),

    Allow(1),
    Block(2);


    class JsonAdapter : TypeAdapter<OverrideState>() {
        override fun write(writer: JsonWriter, value: OverrideState?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value.value)
        }

        override fun read(reader: JsonReader): OverrideState? {
            val value = reader.nextInt()
            return OverrideState.values().firstOrNull { it.value == value }
        }
    }
}