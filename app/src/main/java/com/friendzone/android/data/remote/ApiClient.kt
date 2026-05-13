package com.friendzone.android.data.remote

import com.friendzone.android.data.local.AppPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.atomic.AtomicReference

class ApiClient(
    baseUrlProvider: ApiBaseUrlProvider,
    prefs: AppPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val accessTokenRef = AtomicReference<String?>(null)

    init {
        scope.launch {
            prefs.accessToken.collectLatest { value ->
                accessTokenRef.set(value?.takeIf(String::isNotBlank))
            }
        }
    }

    private val baseUrlInterceptor = Interceptor { chain ->
        val original = chain.request()
        val rawBaseUrl = baseUrlProvider.get()
        val parsed = runCatching { rawBaseUrl.toHttpUrl() }.getOrNull()
        if (parsed == null) {
            return@Interceptor chain.proceed(original)
        }
        val mergedPath = mergeEncodedPaths(parsed.encodedPath, original.url.encodedPath)
        val newUrl = original.url.newBuilder()
            .scheme(parsed.scheme)
            .host(parsed.host)
            .port(parsed.port)
            .encodedPath(mergedPath)
            .build()
        chain.proceed(original.newBuilder().url(newUrl).build())
    }

    private val authInterceptor = Interceptor { chain ->
        val token = accessTokenRef.get()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(baseUrlInterceptor)
        .addInterceptor(authInterceptor)
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

    private fun mergeEncodedPaths(basePath: String, requestPath: String): String {
        val normalizedBase = basePath.trimEnd('/').ifBlank { "" }
        val normalizedRequest = requestPath.trimStart('/')
        val merged = buildString {
            append('/')
            if (normalizedBase.isNotEmpty()) {
                append(normalizedBase.trimStart('/'))
                if (normalizedRequest.isNotEmpty()) {
                    append('/')
                }
            }
            append(normalizedRequest)
        }
        return merged.replace("//", "/")
    }
}


