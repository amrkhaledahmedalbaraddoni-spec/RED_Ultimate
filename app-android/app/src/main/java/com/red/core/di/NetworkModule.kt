package com.red.core.di

import com.red.core.delivery.ClientIdentity
import com.red.core.delivery.DevelopedWebSocketClient
import com.red.core.delivery.DevelopedWebSocketClientImpl
import com.red.core.delivery.MessageDeliveryManager
import com.red.feature.auth.AuthApi
import com.red.feature.pstn.DuminApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  private const val BASE_URL = "http://192.168.1.50:8080/"

  @Provides
  @Singleton
  fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

  @Provides
  @Singleton
  fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder().build()

  @Provides
  @Singleton
  fun provideRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

  @Provides
  @Singleton
  fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

  @Provides
  @Singleton
  fun provideDuminApi(retrofit: Retrofit): DuminApi = retrofit.create(DuminApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DeliveryBindings {
  @Binds
  @Singleton
  abstract fun bindWebSocketClient(impl: DevelopedWebSocketClientImpl): DevelopedWebSocketClient
}
