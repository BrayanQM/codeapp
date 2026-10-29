package com.primerexample.firebaseapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(
    primary = Color(0xFFB3541E),
    secondary = Color(0xFF77574A)
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFFFFB68C),
    secondary = Color(0xFFE5BEB0)
)

@Composable
fun FirebaseApiTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        content = contenido
    )
}
