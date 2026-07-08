package com.V2Skydivejump.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SkydiveColorScheme = lightColorScheme(
    primary = SkydiveDarkBlue,
    onPrimary = SkydiveWhite,
    primaryContainer = SkydiveElectricBlue,
    onPrimaryContainer = SkydiveWhite,
    secondary = SkydiveElectricBlue,
    onSecondary = SkydiveWhite,
    background = SkydiveWhite,
    onBackground = SkydiveBlack,
    surface = SkydiveWhite,
    onSurface = SkydiveBlack
)

@Composable
fun SkydiveJumpTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SkydiveColorScheme,
        content = content
    )
}
