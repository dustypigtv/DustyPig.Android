package tv.dustypig.dustypig.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.dustypig.dustypig.api.ApiService
import tv.dustypig.dustypig.global_managers.auth_manager.AuthManager
import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedAPIService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnAuthenticatedAPIService


@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {

    companion object {
        private const val baseUrl = "https://service.dustypig.tv/api/v3/"

        private val gsonConverter = GsonConverterFactory.create(
            GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()
        )
    }

    @AuthenticatedAPIService
    @Provides
    fun provideAuthenticatedAPIService(authManager: AuthManager): ApiService {
        return Retrofit.Builder()
            .client(
                OkHttpClient()
                    .newBuilder()
                    .addInterceptor {
                        it.proceed(
                            it.request()
                                .newBuilder()
                                .header(
                                    "Authorization", "Bearer " + authManager.currentToken
                                ).build()
                        )
                    }
                    .build()
            )
            .baseUrl(baseUrl)
            .addConverterFactory(gsonConverter)
            .build()
            .create(ApiService::class.java)
    }

    @UnAuthenticatedAPIService
    @Provides
    fun provideUnAuthenticatedAPIService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(gsonConverter)
            .build()
            .create(ApiService::class.java)
    }
}
