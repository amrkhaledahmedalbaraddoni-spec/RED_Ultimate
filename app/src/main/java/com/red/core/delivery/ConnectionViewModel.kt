package com.red.core.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Bridges the WebSocket transport state to Compose without leaking transport details into UI. */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
  private val deliveryManager: MessageDeliveryManager
) : ViewModel() {
  val isConnected: StateFlow<Boolean> = deliveryManager.connectionState
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

  fun retry() {
    deliveryManager.start()
  }
}
