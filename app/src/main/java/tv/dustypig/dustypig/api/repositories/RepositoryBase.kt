package tv.dustypig.dustypig.api.repositories

import android.util.Log
import retrofit2.Response
import tv.dustypig.dustypig.api.models.Result
import tv.dustypig.dustypig.api.models.ResultOf
import tv.dustypig.dustypig.global_managers.AuthManager
import java.io.IOException

abstract class RepositoryBase(
    private val authManager: AuthManager
) {

    companion object {
        private const val TAG = "RepositoryBase"
    }

    internal suspend fun wrapAPICall(call: suspend () -> (Response<Result>)) {
        try {
            val response = call.invoke()
            if (!response.isSuccessful) {
                if (response.code() == 401) {
                    authManager.logout()
                    return
                } else {
                    throw Exception(response.message())
                }
            }
            val rw = response.body()!!
            if (!rw.success)
                throw Exception(rw.error)
        } catch (ex: IOException) {
            Log.e(TAG, ex.localizedMessage, ex)
            throw Exception("Not connected to the internet")
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            if (ex.localizedMessage.isNullOrBlank()) {
                throw Exception("Unknown Error")
            }
            throw ex
        }
    }

    internal suspend fun <T> wrapAPICallWithReturnData(call: suspend () -> (Response<ResultOf<T>>)): T {

        try {
            val response = call.invoke()
            if (!response.isSuccessful) {
                if (response.code() == 401) {
                    authManager.logout()
                    throw Exception(response.message())
                } else {
                    throw Exception(response.message())
                }
            }
            val rw = response.body()!!
            if (!rw.success) {
                throw Exception(rw.error)
            }
            return response.body()!!.data!!
        } catch (ex: IOException) {
            Log.e(TAG, ex.localizedMessage, ex)
            throw Exception("Not connected to the internet")
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            if (ex.localizedMessage.isNullOrBlank()) {
                throw Exception("Unknown Error")
            }
            throw ex
        }
    }

}