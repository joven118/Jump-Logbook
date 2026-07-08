package com.V2Skydivejump.app

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun BluetoothPermissionHandler(onPermissionsGranted: @Composable () -> Unit) {
    var permissionsGranted by remember { mutableStateOf(false) }

    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        launcher.launch(bluetoothPermissions)
    }

    if (permissionsGranted) {
        onPermissionsGranted()
    } else {
        // Simple error or info screen
        androidx.compose.material3.Text("Bluetooth permissions required to scan for altimeters.")
    }
}

@Composable
actual fun rememberPhotoPickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                // Request persistent URI permission to access the file later and in different processes
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                onResult(uri.toString())
            } catch (e: Exception) {
                println("DIAGNOSTIC ERROR: Failed to take persistable URI permission: ${e.message}")
                onResult(uri.toString()) // Still return URI, fallback to non-persistent
            }
        } else {
            onResult(null)
        }
    }
    return { launcher.launch(arrayOf("image/*")) }
}

@Composable
actual fun rememberVideoPickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                onResult(uri.toString())
            } catch (e: Exception) {
                onResult(uri.toString())
            }
        } else {
            onResult(null)
        }
    }
    return { launcher.launch(arrayOf("video/*")) }
}
