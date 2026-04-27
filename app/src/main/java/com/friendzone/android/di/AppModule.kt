package com.friendzone.android.di

import android.content.Context
import com.friendzone.android.data.AppPreferences
import com.friendzone.android.data.AuthRepository
import com.friendzone.android.data.ClientRepository
import com.friendzone.android.data.EventRepository
import com.friendzone.android.data.LocationRepository
import com.friendzone.android.data.ZoneRepository
import com.friendzone.android.location.LocationProvider
import com.friendzone.android.location.LocationUploader
import com.friendzone.android.location.OsmdroidLocationProvider
import com.friendzone.android.network.ApiClient
import com.friendzone.android.network.ApiBaseUrlProvider
import com.friendzone.android.network.FriendZoneApi
import com.friendzone.android.notifications.Notifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBaseUrlProvider(prefs: AppPreferences): ApiBaseUrlProvider =
        ApiBaseUrlProvider(prefs)

    @Provides
    @Singleton
    fun provideApi(baseUrlProvider: ApiBaseUrlProvider): FriendZoneApi =
        ApiClient(baseUrlProvider).api

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): AppPreferences =
        AppPreferences(context)

    @Provides
    @Singleton
    fun provideClientRepository(
        api: FriendZoneApi,
        prefs: AppPreferences
    ): ClientRepository = ClientRepository(api, prefs)

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: FriendZoneApi,
        prefs: AppPreferences
    ): AuthRepository = AuthRepository(api, prefs)

    @Provides
    @Singleton
    fun provideZoneRepository(
        api: FriendZoneApi,
        prefs: AppPreferences
    ): ZoneRepository = ZoneRepository(api, prefs)

    @Provides
    @Singleton
    fun provideEventRepository(
        api: FriendZoneApi,
        prefs: AppPreferences
    ): EventRepository = EventRepository(api, prefs)

    @Provides
    @Singleton
    fun provideLocationRepository(
        api: FriendZoneApi,
        prefs: AppPreferences
    ): LocationRepository = LocationRepository(api, prefs)

    @Provides
    @Singleton
    fun provideLocationProvider(@ApplicationContext context: Context): LocationProvider =
        OsmdroidLocationProvider(context)

    @Provides
    @Singleton
    fun provideLocationUploader(
        locationProvider: LocationProvider,
        locationRepository: LocationRepository,
        notifier: Notifier
    ): LocationUploader = LocationUploader(locationProvider, locationRepository, notifier)

    @Provides
    @Singleton
    fun provideNotifier(@ApplicationContext context: Context): Notifier = Notifier(context)
}
