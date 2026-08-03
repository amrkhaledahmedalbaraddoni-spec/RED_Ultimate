package com.red.core.di

import org.thoughtcrime.securesms.BuildConfig
import com.red.core.auth.TokenStore
import com.red.core.delivery.ClientIdentity
import com.red.core.delivery.DevelopedWebSocketClient
import com.red.core.delivery.DevelopedWebSocketClientImpl
import com.red.core.security.CertificatePinner
import com.red.feature.auth.AuthApi
import com.red.feature.chat.ChatApi
import com.red.feature.chat.ContactApi
import com.red.feature.chat.BlockApi
import com.red.feature.chat.ChatMetaApi
import com.red.feature.chat.DisappearingMessageApi
import com.red.feature.chat.GroupApi
import com.red.feature.chat.NotificationApi
import com.red.feature.chat.PinApi
import com.red.feature.chat.RateLimitApi
import com.red.feature.chat.ReactionApi
import com.red.feature.chat.UserStatusApi
import com.red.feature.pstn.DuminApi
import com.red.feature.stories.StoryApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  // RED has its own local REST/WebSocket endpoint. Keeping it in BuildConfig avoids
  // shipping a hard-coded LAN address and avoids redirecting Signal's native service client.
  private val BASE_URL = BuildConfig.RED_SERVER_URL.trimEnd('/') + "/"
  private val SERVER_HOST = requireNotNull(URI(BASE_URL).host) {
    "RED server URL must contain a valid host: $BASE_URL"
  }

  @Provides
  @Singleton
  fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

  @Provides
  @Singleton
  fun provideAuthInterceptor(identity: ClientIdentity): Interceptor = Interceptor { chain ->
    val request = if (identity.token.isNotBlank()) {
      chain.request().newBuilder()
        .header("Authorization", "Bearer ${identity.token}")
        .build()
    } else chain.request()
    chain.proceed(request)
  }

  @Provides
  @Singleton
  fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  @Provides
  @Singleton
  fun provideOkHttp(
    authInterceptor: Interceptor,
    loggingInterceptor: HttpLoggingInterceptor,
    certificatePinner: CertificatePinner
  ): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .certificatePinner(certificatePinner.buildCertificatePinner(SERVER_HOST))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

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

  @Provides
  @Singleton
  fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

  @Provides
  @Singleton
  fun provideStoryApi(retrofit: Retrofit): StoryApi = retrofit.create(StoryApi::class.java)

  @Provides
  @Singleton
  fun provideContactApi(retrofit: Retrofit): ContactApi = retrofit.create(ContactApi::class.java)

  @Provides
  @Singleton
  fun provideBlockApi(retrofit: Retrofit): BlockApi = retrofit.create(BlockApi::class.java)

  @Provides
  @Singleton
  fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

  @Provides
  @Singleton
  fun provideGroupApi(retrofit: Retrofit): GroupApi = retrofit.create(GroupApi::class.java)

  @Provides
  @Singleton
  fun provideChatMetaApi(retrofit: Retrofit): ChatMetaApi = retrofit.create(ChatMetaApi::class.java)

  @Provides
  @Singleton
  fun provideReactionApi(retrofit: Retrofit): ReactionApi = retrofit.create(ReactionApi::class.java)

  @Provides
  @Singleton
  fun provideDisappearingMessageApi(retrofit: Retrofit): DisappearingMessageApi = retrofit.create(DisappearingMessageApi::class.java)

  @Provides
  @Singleton
  fun provideUserStatusApi(retrofit: Retrofit): UserStatusApi = retrofit.create(UserStatusApi::class.java)

  @Provides
  @Singleton
  fun providePinApi(retrofit: Retrofit): PinApi = retrofit.create(PinApi::class.java)

  @Provides
  @Singleton
  fun provideRateLimitApi(retrofit: Retrofit): RateLimitApi = retrofit.create(RateLimitApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DeliveryBindings {
  @Binds
  @Singleton
  abstract fun bindWebSocketClient(impl: DevelopedWebSocketClientImpl): DevelopedWebSocketClient
}
