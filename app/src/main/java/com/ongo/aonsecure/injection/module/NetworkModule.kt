package com.ongo.aonsecure.injection.module

import android.util.Log
import androidx.core.net.toUri
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.BASE_URL
import com.ongo.aonsecure.utils.SharedPref
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import pdf.app.library.utils.Constants
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Module which provides all required dependencies about network
 */
@Module
// Safe here as we are dealing with a Dagger 2 module
@Suppress("unused")
object NetworkModule {
    /**
     * Provides the Post service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Post service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun providePostApi(retrofit: Retrofit): PostApi {
        return retrofit.create(PostApi::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(): Retrofit {

        // Existing logging code
        val logger = object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("OkHttp", message.toUri().toString())
            }
        }

        val interceptor = HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val token = SharedPref.read(Constants.PREF_TOKEN, "")

                if (isTokenExpired(token)) {
                    Log.d("3245890-", "Token Expired ...............: ")
                }

                val requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()

                return chain.proceed(requestWithToken)
            }
        }

        // ✅ Enforce TLS 1.2 and TLS 1.3 only (SECURITY FIX)
        val connectionSpec = okhttp3.ConnectionSpec.Builder(okhttp3.ConnectionSpec.MODERN_TLS)
            .tlsVersions(
                okhttp3.TlsVersion.TLS_1_2,
                okhttp3.TlsVersion.TLS_1_3
            )
            .allEnabledCipherSuites()
            .build()

        // Build OkHttp with secure spec
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(authInterceptor)

            // ✅ Apply secure TLS config
            .connectionSpecs(listOf(connectionSpec))

            // ✅ Block HTTP URLs entirely (Audit Issue #6)
            .addInterceptor { chain ->
                val request = chain.request()
                if (!request.url.toString().startsWith("https://")) {
                    throw SecurityException("Insecure HTTP URL blocked by security policy")
                }
                chain.proceed(request)
            }

            .build()

        // Retrofit builder unchanged
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create().asLenient())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token != null) {
            if (token.contains("The Token has expired")){
                return true
            }
        }
        return false

    }
}