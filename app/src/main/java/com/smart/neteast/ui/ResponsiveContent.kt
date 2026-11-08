package com.smart.neteast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 内容容器。手机与大屏走两套策略：
 *
 * - < 600dp（手机）→ 使用窗口实际宽度，由页面自身的 16dp 内容边距控制留白。
 * - >= 600dp（平板 / 折叠展开 / 车机）→ 默认**不再居中限宽**，内容铺满内容栏，
 *   仅留左右 24dp 内边距（配合自适应栅格，专辑/歌单随宽度多列铺开）。
 *   若传入 [maxWidth]（文本为主的页面，如单曲详情），则宽屏也居中限宽，
 *   避免整行文字横跨平板、行长不可读。
 *
 * 宽屏下 MainScreen 已把内容栏右移给左侧 sidebar 让位，这里只负责栏内布局。
 */
@Composable
fun ResponsiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    content: @Composable () -> Unit
) {
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    if (windowWidthDp >= 600) {
        if (maxWidth != null) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                        .fillMaxHeight()
                ) {
                    content()
                }
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                content()
            }
        }
    } else {
        // 不要在手机上额外套 360dp 的居中画布：Home/Library 自己已留 16dp，
        // 二次限宽会形成约 32dp 的视觉边距，并浪费大屏手机/模拟器的可用宽度。
        Box(modifier = modifier.fillMaxSize()) { content() }
    }
}
