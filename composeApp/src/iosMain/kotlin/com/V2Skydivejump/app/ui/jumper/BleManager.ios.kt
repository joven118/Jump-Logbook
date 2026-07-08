package com.V2Skydivejump.app.ui.jumper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.CoreBluetooth.*
import platform.Foundation.*

actual class BleManager {
    actual fun scan(): Flow<BleDevice> = flow {
        // iOS scanning logic using CBCentralManager would go here.
        // For the baseline, we'll emit a discovery status.
    }

    actual suspend fun connect(address: String): Flow<String> = flow {
        emit("Connecting on iOS...")
        emit("Connected")
    }

    actual suspend fun stopScan() {}

    actual fun getTelemetryFlow(): Flow<ByteArray> = flow {
        // iOS BLE telemetry notifications are not implemented yet.
    }
}
