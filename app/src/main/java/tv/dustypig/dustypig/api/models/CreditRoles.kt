package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(CreditRoles.JsonAdapter::class)
enum class CreditRoles (val value: Int) {
    Cast(1),
    Director(2),
    Producer(3),
    Writer(4),
    ExecutiveProducer(5);

    class JsonAdapter : TypeAdapter<CreditRoles>() {
        override fun write(writer: JsonWriter?, value: CreditRoles?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): CreditRoles? {
            val value = reader?.nextInt() ?: -1
            return CreditRoles.values().firstOrNull { it.value == value }
        }
    }
}
