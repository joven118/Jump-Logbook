package com.V2Skydivejump.app.ui.jumper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

actual class BleManager {
    actual fun scan(): Flow<BleDevice> = flow {
        // Desktop BLE logic placeholder
    }

    actual suspend fun connect(address: String): Flow<String> = flow {
        emit("Connecting on Desktop...")
    }

    actual suspend fun stopScan() {}

    actual fun getTelemetryFlow(): Flow<ByteArray> = flow {
        // Desktop BLE telemetry is not implemented yet.
    }
}
