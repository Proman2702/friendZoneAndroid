package com.friendzone.android.data.remote

import com.friendzone.android.data.remote.dto.AuthLoginRequest
import com.friendzone.android.data.remote.dto.AuthRegisterRequest
import com.friendzone.android.data.remote.dto.AuthUserResponse
import com.friendzone.android.data.remote.dto.ClientRegisterRequest
import com.friendzone.android.data.remote.dto.ClientRegisterResponse
import com.friendzone.android.data.remote.dto.EventDto
import com.friendzone.android.data.remote.dto.LocationBatchRequest
import com.friendzone.android.data.remote.dto.LocationRequest
import com.friendzone.android.data.remote.dto.LocationResponse
import com.friendzone.android.data.remote.dto.PasswordRecoveryRequest
import com.friendzone.android.data.remote.dto.PasswordRecoveryResponse
import com.friendzone.android.data.remote.dto.ZoneCreateRequest
import com.friendzone.android.data.remote.dto.ZoneDto
import com.friendzone.android.data.remote.dto.ZoneUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendZoneApi {
    @POST("auth/register")
    suspend fun registerUser(@Body request: AuthRegisterRequest): AuthUserResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): AuthUserResponse

    @POST("auth/forgot-password")
    suspend fun recoverPassword(@Body request: PasswordRecoveryRequest): PasswordRecoveryResponse

    @POST("clients/register")
    suspend fun register(@Body request: ClientRegisterRequest): ClientRegisterResponse

    @POST("zones")
    suspend fun createZone(@Body request: ZoneCreateRequest): ZoneDto

    @GET("zones")
    suspend fun getZones(@Query("clientId") clientId: String): List<ZoneDto>

    @PATCH("zones/{id}")
    suspend fun updateZone(
        @Path("id") zoneId: String,
        @Body request: ZoneUpdateRequest
    ): ZoneDto

    @DELETE("zones/{id}")
    suspend fun deleteZone(
        @Path("id") zoneId: String,
        @Query("clientId") clientId: String
    )

    @POST("locations")
    suspend fun sendLocation(@Body request: LocationRequest): LocationResponse

    @POST("locations/batch")
    suspend fun sendLocations(@Body request: LocationBatchRequest): LocationResponse

    @GET("events")
    suspend fun getEvents(@Query("clientId") clientId: String): List<EventDto>
}


