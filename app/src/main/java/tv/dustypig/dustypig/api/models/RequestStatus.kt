package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(RequestStatus.JsonAdapter::class)
enum class RequestStatus (val value: Int) {
    NotRequested(0),
    RequestSentToMain(1),
    RequestSentToAccount(2),
    Denied(3),
    Pending(4),
    Fulfilled(5);

    class JsonAdapter : TypeAdapter<RequestStatus>() {
        override fun write(writer: JsonWriter?, value: RequestStatus?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): RequestStatus? {
            val value = reader?.nextInt() ?: -1
            return RequestStatus.values().firstOrNull { it.value == value }
        }
    }
}
