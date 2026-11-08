package com.smart.neteast.ui.theme


import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Ncrust 自有配色板。替代 MD3 的 ColorScheme。
 *
 * 设计：深色为 OLED 纯黑背景；浅色为米色偏白底。两套共享同一组强调色。
 * 所有颜色硬编码，不依赖 MD3 的 tonalElevation 调色逻辑。
 */
@Immutable
data class NcrustColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val divider: Color = Color(0xFF2A2A2A),
    val error: Color = Color(0xFFFF6B6B),
)

/**
 * 默认配色：云杉绿主题 + OLED 纯黑。
 * onSurfaceVariant 用中性灰 #B3B3B3（Spotify 风），与 MD3 紫调 onSurfaceVariant 区分。
 */
val DefaultNcrustColors = NcrustColors(
    primary = Color(0xFF1DB954),
    onPrimary = Color.Black,
    background = Color(0xFF000000),
    surface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFF1A1A1A),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFB3B3B3),
    divider = Color(0xFF2A2A2A),
)

/**
 * 浅色配色：米色偏白底（暖调），近黑暖灰文字。
 * surface 比 background 更亮一档，作为弹窗/侧栏/卡背的抬升面。
 */
val LightNcrustColors = NcrustColors(
    primary = Color(0xFF1DB954),
    onPrimary = Color.Black,
    background = Color(0xFFF6F2E9),
    surface = Color(0xFFFFFDF8),
    surfaceVariant = Color(0xFFEDE6D8),
    onBackground = Color(0xFF1C1A16),
    onSurface = Color(0xFF1C1A16),
    onSurfaceVariant = Color(0xFF6E6658),
    divider = Color(0xFFE2DACB),
    error = Color(0xFFB42318),
)

/** Pick black or white foreground by WCAG contrast for arbitrary user-selected accent colors. */
fun contrastingColor(background: Color): Color {
    val luminance = background.luminance()
    val blackContrast = (luminance + 0.05f) / 0.05f
    val whiteContrast = 1.05f / (luminance + 0.05f)
    return if (blackContrast >= whiteContrast) Color.Black else Color.White
}

val LocalNcrustColors = compositionLocalOf { DefaultNcrustColors }
