package tv.dustypig.dustypig.api.repositories

import android.util.Log
import retrofit2.Response
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.global_managers.AuthManager
import java.io.IOException

abstract class RepositoryBase constructor(
    private val authManager: AuthManager
) {

    companion object {
        private const val TAG = "RepositoryBase"
    }

    internal suspend fun wrapAPICall(call: suspend () -> (Response<ResponseWrapper>)) {
        try{
            val response = call.invoke()
            if(!response.isSuccessful) {
                if(response.code() == 401) {
                    authManager.setAuthState("", 0, false)
                    return
                } else {
                    throw Exception(response.message())
                }
            }
            val rw = response.body()!!
            if(!rw.success)
                throw Exception(rw.error)
        }
        catch (ex: IOException) {
            Log.e(TAG, ex.localizedMessage, ex)
            throw Exception("Not connected to the internet")
        }
        catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            if(ex.localizedMessage.isNullOrBlank()) {
                throw Exception("Unknown Error")
            }
            throw ex
        }
    }

    internal suspend fun <T> wrapAPICallWithReturnData(call: suspend () -> (Response<ResponseWrapperOf<T>>)): T {

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
        }
        catch (ex: IOException) {
            Log.e(TAG, ex.localizedMessage, ex)
            throw Exception("Not connected to the internet")
        }
        catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            if (ex.localizedMessage.isNullOrBlank()) {
                throw Exception("Unknown Error")
            }
            throw ex
        }
    }

    internal suspend fun <T> wrapAPICallWithReturnSimpleValue(call: suspend () -> (Response<ResponseWrapperOf<SimpleValue<T>>>)): T {

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
            return response.body()!!.data!!.value
        }
        catch (ex: IOException) {
            Log.e(TAG, ex.localizedMessage, ex)
            throw Exception("Not connected to the internet")
        }
        catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            if (ex.localizedMessage.isNullOrBlank()) {
                throw Exception("Unknown Error")
            }
            throw ex
        }
    }

}