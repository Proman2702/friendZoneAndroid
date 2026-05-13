package com.friendzone.android.data.repository

import com.friendzone.android.data.remote.dto.ApiError
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

private val errorAdapter = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
    .adapter(ApiError::class.java)

fun Throwable.toReadableException(): Exception {
    if (this is HttpException) {
        val parsed = response()?.errorBody()?.string()?.let(errorAdapter::fromJson)
        return Exception(parsed?.message ?: (message ?: "Server error"))
    }
    return Exception(message ?: "Network error")
}
