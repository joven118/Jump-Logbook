package com.V2Skydivejump.app

import androidx.compose.runtime.Composable

@Composable
actual fun BluetoothPermissionHandler(onPermissionsGranted: @Composable () -> Unit) {
    // Desktop (JVM) usually doesn't have a standardized permission model like Android/iOS for BLE.
    onPermissionsGranted()
}

@Composable
actual fun rememberPhotoPickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    return { /* Desktop file picker placeholder */ }
}

@Composable
actual fun rememberVideoPickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    return { /* Desktop file picker placeholder */ }
}
