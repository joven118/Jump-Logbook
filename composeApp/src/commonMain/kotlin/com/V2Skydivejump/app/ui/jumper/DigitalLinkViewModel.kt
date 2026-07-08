package com.V2Skydivejump.app.ui.jumper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.utils.TelemetryParser
import com.V2Skydivejump.app.utils.TelemetryParser.toHexString
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int
)

data class DigitalLinkUiState(
    val isScanning: Boolean = false,
    val foundDevices: List<BleDevice> = emptyList(),
    val connectionStatus: String = "Idle",
    val pendingJump: JumpLogEntity? = null,
    val latestTelemetryJump: JumpLogEntity? = null,
    val errorMessage: String? = null
)

// Expect/Actual for the actual BLE implementation
expect class BleManager() {
    fun scan(): Flow<BleDevice>
    suspend fun connect(address: String): Flow<String>
    suspend fun stopScan()
    fun getTelemetryFlow(): Flow<ByteArray>
}

class DigitalLinkViewModel(private val database: AppDatabase) : ViewModel() {

    private val bleManager = BleManager()
    private val _uiState = MutableStateFlow(DigitalLinkUiState())
    val uiState: StateFlow<DigitalLinkUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, foundDevices = emptyList()) }
            bleManager.scan()
                .catch { cause -> _uiState.update { it.copy(errorMessage = cause.message, isScanning = false) } }
                .collect { device ->
                    _uiState.update { state ->
                        if (state.foundDevices.none { it.address == device.address }) {
                            state.copy(foundDevices = state.foundDevices + device)
                        } else state
                    }
                }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            bleManager.stopScan()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun connectToDevice(device: BleDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = "Connecting to ${device.name}...") }
            bleManager.connect(device.address)
                .catch { e -> _uiState.update { it.copy(connectionStatus = "Error: ${e.message}") } }
                .collect { status ->
                    _uiState.update { it.copy(connectionStatus = status) }
                    if (status == "Connected") listenToTelemetry()
                }
        }
    }

    private fun listenToTelemetry() {
        viewModelScope.launch {
            bleManager.getTelemetryFlow()
                .collect { bytes ->
                    val jumpLog = TelemetryParser.parseFdsPacket(bytes, "current_user")
                    if (jumpLog != null) {
                        _uiState.update { it.copy(pendingJump = jumpLog, latestTelemetryJump = jumpLog) }
                    }
                }
        }
    }

    fun savePendingJump() {
        viewModelScope.launch {
            uiState.value.pendingJump?.let { jump ->
                database.jumpLogDao().insertJump(jump)
                _uiState.update { state -> 
                    state.copy(pendingJump = null, connectionStatus = "Jump #${jump.jumpNumber} Saved!") 
                }
            }
        }
    }

    fun discardPendingJump() {
        _uiState.update { it.copy(pendingJump = null) }
    }

    fun importCsv(csvContent: String) {
        viewModelScope.launch {
            val jumps = TelemetryParser.parseAltimeterCsv(csvContent, "current_user")
            jumps.forEach { database.jumpLogDao().insertJump(it) }
            _uiState.update { it.copy(connectionStatus = "Imported ${jumps.size} jumps from CSV") }
        }
    }
}
