package io.github.takahashirinta.kanesumi.structure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.takahashirinta.kanesumi.core.insets.MetroBottomStackScope
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors

/**
 * Kanesumi 应用外壳。取代 M3 Scaffold,但只做 Metro 需要的三件事:
 *
 * 1. 建立 MetroBottomStackScope,让内部的 MetroBottomNav / mini player 等
 *    结构元件能自动登记高度;
 * 2. 铺一层背景色(默认 LocalMetroColors.background);
 * 3. 把 bottomBar 作为 overlay 画在 content 之上底部 —— 这是 Metro 与 M3
 *    最大的分歧:M3 Scaffold 把 bottomBar 从 content 空间里"切"出来,Metro
 *    让 content 铺满全屏、bottomBar 悬浮,内容侧靠 bottomOverlayPadding()
 *    自适应留白。
 *
 * bottomBar 是单 slot —— 想放"mini player + bottom nav"这种叠层,自己在
 * 里面 Column 起来即可。每一层内部照常 rememberBottomStackReservation(...),
 * 自动汇总到 bottomOverlayPadding。
 *
 * 不管 topBar:Metro 惯例是"顶栏 = 页面第一个可滚动元素",不是独立 chrome。
 * MetroAppBar 直接作为 LazyColumn 的第一个 item 即可,不需要 shell 分层。
 */
@Composable
fun MetroShell(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
    background: Color = LocalMetroColors.current.background,
    content: @Composable BoxScope.() -> Unit,
) {
    MetroBottomStackScope {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(background),
        ) {
            content()
            if (bottomBar != null) {
                Box(Modifier.align(Alignment.BottomCenter)) {
                    bottomBar()
                }
            }
        }
    }
}
