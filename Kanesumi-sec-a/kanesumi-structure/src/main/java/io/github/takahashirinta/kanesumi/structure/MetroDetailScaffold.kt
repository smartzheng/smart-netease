package io.github.takahashirinta.kanesumi.structure

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.anim.sokuou.MetroDefault
import io.github.takahashirinta.kanesumi.anim.sokuou.SokuouTweens
import io.github.takahashirinta.kanesumi.anim.sokuou.rememberMetroFlingBehavior
import io.github.takahashirinta.kanesumi.controls.MetroButton
import io.github.takahashirinta.kanesumi.core.insets.bottomOverlayPadding
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * 详情页 3 态骨架:Loading / Error / Content 之间 Crossfade,Content 分支带
 * 短促向上入场(220ms MetroDefault),LazyColumn 承接 header + 内容 item。
 *
 * hasCachedContent = true 时跳过全屏 loader —— 由缓存驱动的即时渲染(见
 * Ncrust ContentCache 模式)不需要占位 spinner,直接进 Content 分支。
 *
 * 顶部悬浮 back 图标由内建 MetroTopScrim 承接,scrim 兼顾"任何底图上图标可
 * 读"的通用需求。想要额外顶栏动作(设置/更多),可通过 topScrimExtra slot
 * 追加,与 back 图标位置分离。
 *
 * LazyColumn 使用 MetroFlingBehavior + bottomOverlayPadding(),要求上级
 * 已建立 MetroBottomStackScope(通常 MetroShell 已经做了)。
 */
@Composable
fun MetroDetailScaffold(
    onBack: () -> Unit,
    backIcon: ImageVector,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    hasCachedContent: Boolean = false,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = "Retry",
    backContentDescription: String = "Back",
    topScrimExtra: @Composable (() -> Unit)? = null,
    header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val colors = LocalMetroColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val state = when {
            error != null -> DetailState.Error
            isLoading && !hasCachedContent -> DetailState.Loading
            else -> DetailState.Content
        }

        Crossfade(
            targetState = state,
            animationSpec = SokuouTweens.CoverFade,
            modifier = Modifier.fillMaxSize(),
            label = "MetroDetailScaffold",
        ) { s ->
            when (s) {
                DetailState.Loading -> LoadingSlot()
                DetailState.Error -> ErrorSlot(error ?: "", onRetry, retryLabel)
                DetailState.Content -> ContentSlot(header = header, content = content)
            }
        }

        MetroTopScrim(
            icon = backIcon,
            contentDescription = backContentDescription,
            onClick = onBack,
        )
        if (topScrimExtra != null) {
            topScrimExtra()
        }
    }
}

@Composable
private fun LoadingSlot() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MetroText(
            text = "Loading…",
            color = LocalMetroColors.current.onSurfaceVariant,
            style = LocalMetroTypography.current.body,
        )
    }
}

@Composable
private fun ErrorSlot(error: String, onRetry: (() -> Unit)?, retryLabel: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            MetroText(
                text = error,
                color = LocalMetroColors.current.onSurface,
                style = LocalMetroTypography.current.body,
            )
            if (onRetry != null) {
                Spacer(Modifier.height(16.dp))
                MetroButton(text = retryLabel, onClick = onRetry)
            }
        }
    }
}

@Composable
private fun ContentSlot(
    header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val slideOffsetPx = with(density) { 12.dp.roundToPx() }
    val cascadeState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    AnimatedVisibility(
        visibleState = cascadeState,
        enter = fadeIn(animationSpec = tween(220, easing = MetroDefault)) +
            slideInVertically(
                animationSpec = tween(220, easing = MetroDefault),
                initialOffsetY = { slideOffsetPx },
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = bottomOverlayPadding(),
            flingBehavior = rememberMetroFlingBehavior(),
        ) {
            item { header() }
            content()
        }
    }
}

private enum class DetailState { Loading, Error, Content }
