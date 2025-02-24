package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter


@JsonAdapter(LoginTypes.JsonAdapter::class)
enum class LoginTypes (val value: Int) {
    Account(0),
    MainProfile(1),
    SubProfile(2);

    class JsonAdapter : TypeAdapter<LoginTypes>() {
        override fun write(writer: JsonWriter?, value: LoginTypes?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): LoginTypes? {
            val value = reader?.nextInt() ?: -1
            return LoginTypes.values().firstOrNull { it.value == value }
        }
    }
}