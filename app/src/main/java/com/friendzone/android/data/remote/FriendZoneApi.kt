package com.friendzone.android.data.remote

import com.friendzone.android.data.remote.dto.AuthLoginRequest
import com.friendzone.android.data.remote.dto.AuthRegisterRequest
import com.friendzone.android.data.remote.dto.AuthResponse
import com.friendzone.android.data.remote.dto.CreateFriendRequest
import com.friendzone.android.data.remote.dto.EventDto
import com.friendzone.android.data.remote.dto.FriendRequestDto
import com.friendzone.android.data.remote.dto.LocationBatchRequest
import com.friendzone.android.data.remote.dto.LocationRequest
import com.friendzone.android.data.remote.dto.LocationResponse
import com.friendzone.android.data.remote.dto.RemoteUserDto
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
    suspend fun registerUser(@Body request: AuthRegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): AuthResponse

    @GET("users/me")
    suspend fun getCurrentUser(): RemoteUserDto

    @GET("users/search")
    suspend fun searchUsers(@Query("login") login: String): List<RemoteUserDto>

    @POST("friend-requests")
    suspend fun sendFriendRequest(@Body request: CreateFriendRequest): FriendRequestDto

    @GET("friend-requests/incoming")
    suspend fun getIncomingFriendRequests(): List<FriendRequestDto>

    @GET("friend-requests/outgoing")
    suspend fun getOutgoingFriendRequests(): List<FriendRequestDto>

    @POST("friend-requests/{id}/accept")
    suspend fun acceptFriendRequest(@Path("id") requestId: String): FriendRequestDto

    @POST("friend-requests/{id}/decline")
    suspend fun declineFriendRequest(@Path("id") requestId: String): FriendRequestDto

    @GET("friends")
    suspend fun getFriends(): List<RemoteUserDto>

    @DELETE("friends/{friendId}")
    suspend fun removeFriend(@Path("friendId") friendId: String)

    @POST("zones")
    suspend fun createZone(@Body request: ZoneCreateRequest): ZoneDto

    @GET("zones")
    suspend fun getZones(): List<ZoneDto>

    @PATCH("zones/{id}")
    suspend fun updateZone(
        @Path("id") zoneId: String,
        @Body request: ZoneUpdateRequest
    ): ZoneDto

    @DELETE("zones/{id}")
    suspend fun deleteZone(@Path("id") zoneId: String)

    @POST("locations")
    suspend fun sendLocation(@Body request: LocationRequest): LocationResponse

    @POST("locations/batch")
    suspend fun sendLocations(@Body request: LocationBatchRequest): LocationResponse

    @GET("events")
    suspend fun getEvents(@Query("after") after: String? = null): List<EventDto>
}

