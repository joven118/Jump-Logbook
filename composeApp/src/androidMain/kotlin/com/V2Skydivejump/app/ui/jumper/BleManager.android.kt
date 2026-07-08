package com.V2Skydivejump.app.ui.jumper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.V2Skydivejump.app.appContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

actual class BleManager {
    private val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    @SuppressLint("MissingPermission")
    actual fun scan(): Flow<BleDevice> = callbackFlow {
        val scanner = adapter?.bluetoothLeScanner
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = BleDevice(
                    name = result.device.name,
                    address = result.device.address,
                    rssi = result.rssi
                )
                trySend(device)
            }
        }
        
        scanner?.startScan(callback)
        awaitClose {
            scanner?.stopScan(callback)
        }
    }

    actual suspend fun connect(address: String): Flow<String> = flow {
        emit("Connecting to $address...")
        // Simplified mock connection logic for KMP baseline
        kotlinx.coroutines.delay(1000)
        emit("Connected")
    }

    actual suspend fun stopScan() {
        // Handled by awaitClose in scan()
    }

    actual fun getTelemetryFlow(): Flow<ByteArray> = flow {
        // Mock telemetry notifications
        while (true) {
            kotlinx.coroutines.delay(5000)
            // Mock packet: Jump #42, Exit 13500, Deploy 3500
            val mockPacket = byteArrayOf(
                0x2A, 0x00,                         // 42
                0xBC.toByte(), 0x34, 0x00, 0x00,   // 13500
                0xAC.toByte(), 0x0D, 0x00, 0x00    // 3500
            )
            emit(mockPacket)
        }
    }
}
