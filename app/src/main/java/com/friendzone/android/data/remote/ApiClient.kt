package com.friendzone.android.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ApiClient(
    baseUrlProvider: ApiBaseUrlProvider
) {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val baseUrlInterceptor = Interceptor { chain ->
        val original = chain.request()
        val rawBaseUrl = baseUrlProvider.get()
        val parsed = runCatching { rawBaseUrl.toHttpUrl() }.getOrNull()
        if (parsed == null) {
            return@Interceptor chain.proceed(original)
        }
        val newUrl = original.url.newBuilder()
            .scheme(parsed.scheme)
            .host(parsed.host)
            .port(parsed.port)
            .build()
        chain.proceed(original.newBuilder().url(newUrl).build())
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(baseUrlInterceptor)
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: FriendZoneApi = retrofit.create(FriendZoneApi::class.java)
}


