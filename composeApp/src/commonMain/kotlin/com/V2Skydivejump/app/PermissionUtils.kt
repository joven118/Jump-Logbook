package com.V2Skydivejump.app

import androidx.compose.runtime.Composable

@Composable
expect fun BluetoothPermissionHandler(onPermissionsGranted: @Composable () -> Unit)

@Composable
expect fun rememberPhotoPickerLauncher(onResult: (String?) -> Unit): () -> Unit

@Composable
expect fun rememberVideoPickerLauncher(onResult: (String?) -> Unit): () -> Unit
