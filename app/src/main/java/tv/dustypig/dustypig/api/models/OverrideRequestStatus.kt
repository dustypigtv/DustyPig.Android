package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(OverrideRequestStatus.JsonAdapter::class)
enum class OverrideRequestStatus (val value: Int) {
    NotRequested(0),
    Requested(1),
    Denied(2),
    Granted(3);

    class JsonAdapter : TypeAdapter<OverrideRequestStatus>() {
        override fun write(writer: JsonWriter?, value: OverrideRequestStatus?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): OverrideRequestStatus? {
            val value = reader?.nextInt() ?: -1
            return OverrideRequestStatus.values().firstOrNull { it.value == value }
        }
    }
}
