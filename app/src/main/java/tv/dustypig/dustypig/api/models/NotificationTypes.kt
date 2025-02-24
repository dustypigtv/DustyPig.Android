package tv.dustypig.dustypig.api.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(NotificationTypes.JsonAdapter::class)
enum class NotificationTypes (val value: Int) {
    NewMediaRequested(1),
    NewMediaPending(2),
    NewMediaFulfilled(3),
    NewMediaRejected(4),
    NewMediaAvailable(5),
    OverrideMediaRequested(6),
    OverrideMediaGranted(7),
    OverrideMediaRejected(8),
    FriendshipInvited(9),
    FriendshipAccepted(10);

    companion object {
        fun getByVal(value: String?): NotificationTypes? {
            val intVal = value?.toIntOrNull() ?: return null
            return NotificationTypes.values().firstOrNull { it.value == intVal }
        }
    }

    class JsonAdapter : TypeAdapter<NotificationTypes>() {
        override fun write(writer: JsonWriter?, value: NotificationTypes?) {
            if(value == null)
                writer?.nullValue()
            else
                writer?.value(value.value)
        }

        override fun read(reader: JsonReader?): NotificationTypes? {
            val value = reader?.nextInt() ?: -1
            return NotificationTypes.values().firstOrNull { it.value == value }
        }
    }
}
