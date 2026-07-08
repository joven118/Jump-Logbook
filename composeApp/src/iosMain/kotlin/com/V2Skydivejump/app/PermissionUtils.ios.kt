package com.V2Skydivejump.app

import androidx.compose.runtime.Composable

@Composable
actual fun BluetoothPermissionHandler(onPermissionsGranted: @Composable () -> Unit) {
    // iOS handles Bluetooth permissions automatically when the API is first accessed.
    // Ensure you have added 'NSBluetoothAlwaysUsageDescription' to your Info.plist.
    onPermissionsGranted()
}

@Composable
actual fun rememberPhotoPickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    return { /* iOS file picker placeholder */ }
}

@Composable
actual fun rememberVideoPickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    return { /* iOS file picker placeholder */ }
}
