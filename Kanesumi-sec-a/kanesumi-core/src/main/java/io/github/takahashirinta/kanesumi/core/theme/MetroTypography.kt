package io.github.takahashirinta.kanesumi.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 两套命名共存:
 *
 * - **语义轴**(pageHeading / title / body / caption / label) —— Metro/UWP 风格,按
 *   "这段字在页面里承担什么角色"命名。新写代码优先用这套,更能表达意图。
 * - **尺度轴**(headlineMedium / titleLarge / titleMedium / bodyLarge / bodyMedium /
 *   bodySmall) —— 对齐 Material 3 命名惯例,方便从 M3/Ncrust 代码迁进来时逐个替换,
 *   不用每个 Text 手挑样式。
 *
 * 值本身由 Kanesumi 决定,不承诺跟 M3 数字一致 —— 命名只是"命中率"的桥。
 * 所有 style 都定 lineHeight,避免默认 lineHeight 让中文段落行距过松。
 */
@Immutable
data class MetroTypography(
    // 语义轴
    val pageHeading: TextStyle = TextStyle(fontSize = 34.sp, lineHeight = 42.sp, fontWeight = FontWeight.Normal),
    val title: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Normal),
    val body: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    val caption: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    val label: TextStyle = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Normal),

    // 尺度轴(M3 命名,Kanesumi 定值)
    val headlineMedium: TextStyle = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Normal),
    val titleLarge: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Normal),
    val titleMedium: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    val bodyLarge: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    val bodyMedium: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    val bodySmall: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
)
