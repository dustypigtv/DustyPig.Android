package tv.dustypig.dustypig.api.repositories

import retrofit2.Response
import tv.dustypig.dustypig.api.models.ResponseWrapper
import tv.dustypig.dustypig.api.models.ResponseWrapperOf
import tv.dustypig.dustypig.api.models.SimpleValue
import tv.dustypig.dustypig.global_managers.AuthManager
import tv.dustypig.dustypig.logToCrashlytics
import java.io.IOException

abstract class RepositoryBase constructor(
    private val authManager: AuthManager
) {

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
            ex.logToCrashlytics()
            throw Exception("Not connected to the internet")
        }
        catch (ex: Exception) {
            ex.logToCrashlytics()
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
            ex.logToCrashlytics()
            throw Exception("Not connected to the internet")
        }
        catch (ex: Exception) {
            ex.logToCrashlytics()
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
            ex.logToCrashlytics()
            throw Exception("Not connected to the internet")
        }
        catch (ex: Exception) {
            ex.logToCrashlytics()
            if (ex.localizedMessage.isNullOrBlank()) {
                throw Exception("Unknown Error")
            }
            throw ex
        }
    }

}