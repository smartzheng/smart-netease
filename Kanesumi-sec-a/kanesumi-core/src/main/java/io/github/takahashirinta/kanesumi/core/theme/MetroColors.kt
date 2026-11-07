package io.github.takahashirinta.kanesumi.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class MetroColors(
    val background: Color = Color(0xFF000000),
    val surface: Color = Color(0xFF0A0F16),
    val surfaceVariant: Color = Color(0xFF14181F),
    val divider: Color = Color(0xFF2A2A2A),
    val primary: Color = Color(0xFF2E67B5),
    val onPrimary: Color = Color(0xFFFFFFFF),
    val onBackground: Color = Color(0xFFF0F0F0),
    val onSurface: Color = Color(0xFFF0F0F0),
    val onSurfaceVariant: Color = Color(0xFF9AA0A6),
    val pressTint: Color = Color(0x22FFFFFF),
)
