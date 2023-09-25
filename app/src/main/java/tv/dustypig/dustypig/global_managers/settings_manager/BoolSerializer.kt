package tv.dustypig.dustypig.global_managers.settings_manager

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class BoolSerializer: Serializer<Boolean> {
    override val defaultValue: Boolean = false

    override suspend fun readFrom(input: InputStream): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                input.read() > 0
            }
        } catch (_: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: Boolean, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(if(t) 1 else 0)
        }
    }

}