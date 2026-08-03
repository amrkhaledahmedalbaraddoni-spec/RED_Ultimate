package com.red.feature.pstn

import com.squareup.moshi.JsonClass
import org.thoughtcrime.securesms.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PstnCallRequest(
  val number: String,
  val duminIp: String = BuildConfig.RED_DUMIN_IP
)

@JsonClass(generateAdapter = true)
data class PstnCallResponse(
  val callId: String?,
  val status: String,
  val duration: Long = 0L
)

/** Retrofit API for the Dumin/GSM gateway (System B). */
interface DuminApi {
  @POST("api/pstn/dial")
  suspend fun startCall(@Body request: PstnCallRequest): Response<PstnCallResponse>

  @GET("api/pstn/call/{callId}")
  suspend fun getCallStatus(@Path("callId") callId: String): Response<PstnCallResponse>

  @POST("api/pstn/hangup")
  suspend fun hangup(@Query("callId") callId: String): Response<Unit>

  @GET("api/pstn/sim")
  suspend fun simStatus(): Response<Map<String, String>>
}

/** UI state for the PSTN call screen. */
sealed interface PstnCallState {
  data object Idle : PstnCallState
  data class Dialing(val number: String) : PstnCallState
  data class Ringing(val number: String) : PstnCallState
  data class Active(val number: String, val duration: Long) : PstnCallState
  data class Ended(val reason: String) : PstnCallState
}
