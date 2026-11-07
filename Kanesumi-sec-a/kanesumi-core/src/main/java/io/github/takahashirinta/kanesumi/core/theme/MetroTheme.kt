package io.github.takahashirinta.kanesumi.core.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMetroColors = staticCompositionLocalOf { MetroColors() }

val LocalMetroTypography = staticCompositionLocalOf { MetroTypography() }

@Composable
fun MetroTheme(
    colors: MetroColors = MetroColors(),
    typography: MetroTypography = MetroTypography(),
    content: @Composable () -> Unit,
) {
    // MetroIndication uses the theme's pressTint. Rebuild the indication if pressTint
    // changes so every clickable inside the theme picks up the new tint immediately.
    val indication = remember(colors.pressTint) { MetroIndication(tint = colors.pressTint) }
    CompositionLocalProvider(
        LocalMetroColors provides colors,
        LocalMetroTypography provides typography,
        LocalIndication provides indication,
    ) {
        content()
    }
}
