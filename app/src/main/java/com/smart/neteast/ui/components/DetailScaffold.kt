package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustColors


import com.smart.neteast.ui.design.*
import com.smart.neteast.ui.theme.NcrustShape

import com.smart.neteast.network.CoverUrls
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smart.neteast.ui.BottomOverlayInsetDp
import com.moriafly.salt.ui.Text as SaltText
import com.smart.neteast.ui.i18n.LocalStrings

/**
 * 无边框详情页骨架。
 *
 * 与旧版差异：
 *  - 没有 M3 TopAppBar 的实体 Surface。顶部使用向下渐隐的深色 scrim 提供图标可读性，
 *    避免以往"半透明黑色方块"堆在角落的突兀感——scrim 与内容边缘自然融合，仍无任何 border。
 *  - 返回箭头是裸图标，无背板，Ripple 以图标中心为原点，触控区扩大到 44dp。
 *  - 内容 LazyColumn 从屏幕顶部开始（延伸到 status bar 下），第一项 header() 会填满整个可视区宽度。
 *  - title 参数已弃用（Groove 风：页面标题由 header 本身承担），保留以兼容签名。
 */
@Composable
fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    // 缓存兜底：调用方已有可立即渲染的内容时传 true，跳过全屏 loader。
    hasCachedContent: Boolean = false,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val strings = LocalStrings.current
    Box(
        // 必须 opaque：nav popExit 动画期间详情页会 slide + 部分透明，
        // 若无 bg 会透视到下方主 tab 屏（返回时 mount/绘制未完成 → 用户看到"低一层残影"）。
        // 主 tab 屏一直在下层挂载，DetailScaffold 显示期间就是它遮住 tab 屏。
        modifier = Modifier
            .fillMaxSize()
            .background(LocalNcrustColors.current.background)
    ) {
        val stateKey = when {
            error != null -> DetailScaffoldState.Error
            isLoading && !hasCachedContent -> DetailScaffoldState.Loading
            else -> DetailScaffoldState.Content
        }
        Crossfade(
            targetState = stateKey,
            animationSpec = NcrustMotion.CoverFade,
            modifier = Modifier.fillMaxSize(),
            label = "DetailScaffoldCrossfade"
        ) { state ->
            when (state) {
                DetailScaffoldState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        NcrustProgressIndicator(color = LocalNcrustColors.current.primary)
                    }
                }
                DetailScaffoldState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SaltText(error ?: "", color = LocalNcrustColors.current.error, style = TextStyle(fontSize = 16.sp))
                            if (onRetry != null) {
                                Spacer(Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .background(LocalNcrustColors.current.primary)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            role = androidx.compose.ui.semantics.Role.Button,
                                            onClick = onRetry
                                        )
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SaltText(strings.retry, color = LocalNcrustColors.current.onPrimary)
                                }
                            }
                        }
                    }
                }
                DetailScaffoldState.Content -> {
                    // 入场级联：内容分支从下方 12dp 微滑上 + 淡入。方向与 NavGraph 横向推入正交。
                    val density = LocalDensity.current
                    val slideOffsetPx = with(density) { 12.dp.roundToPx() }
                    val cascadeState = remember {
                        MutableTransitionState(false).apply { targetState = true }
                    }
                    AnimatedVisibility(
                        visibleState = cascadeState,
                        enter = fadeIn(animationSpec = tween(220, easing = NcrustMotion.StandardEasing)) +
                            slideInVertically(
                                animationSpec = tween(220, easing = NcrustMotion.StandardEasing),
                                initialOffsetY = { slideOffsetPx }
                            )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = BottomOverlayInsetDp),
                            flingBehavior = rememberNcrustFlingBehavior()
                        ) {
                            item { header() }
                            content()
                        }
                    }
                }
            }
        }

        TopScrimIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = strings.back,
            onClick = onBack
        )
    }
}

/**
 * 顶部渐隐 scrim + 裸图标。scrim 承担"让白色图标在任意封面上可读"的职责，
 * 图标本身无背板。详情页与 WebView 登录共用此组件以保证一致性。
 *
 * 实现委托给库的 NcrustTopScrim（同语义：渐隐 scrim + 48dp 触控区 + 直角闪切），
 * 保留本签名以便现有调用方不改。
 */
@Composable
fun TopScrimIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    alignment: Alignment = Alignment.TopStart
) {
    NcrustTopScrim(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        alignment = alignment
    )
}

private enum class DetailScaffoldState { Loading, Error, Content }

/**
 * 无边框详情页头部。
 *
 * 窄屏：封面全宽通铺（fillMaxWidth + aspectRatio 1:1），下方是标题 / 副标题 / info
 * / 右侧大圆播放按钮；封面贴屏幕边缘、无 padding。
 *
 * 宽屏（>=600dp）：改为 Apple Music 式两栏——左侧固定 220dp 方封面，右侧信息列
 * 底部对齐。避免封面在平板上铺成一整屏的正方形。
 */
@Composable
fun DetailHeader(
    coverUrl: String?,
    title: String,
    subtitle: String? = null,
    infoLines: List<String> = emptyList(),
    onPlayAll: (() -> Unit)? = null,
    // 副标题可点击(如专辑页的作曲者 → 跳歌手页), 且无按动反馈
    onSubtitleClick: (() -> Unit)? = null,
    headerActions: @Composable ColumnScope.() -> Unit = {}
) {
    val strings = LocalStrings.current
    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    if (isWide) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 顶部留出返回按钮触控区的高度（系统栏 inset 已由外层内容容器处理）。
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            AsyncImage(
                model = CoverUrls.large(coverUrl),
                contentDescription = strings.coverDesc,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(LocalNcrustColors.current.surfaceVariant),
                modifier = Modifier.size(220.dp).clip(NcrustShape.heroArtwork),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                DetailHeaderInfoRow(
                    title = title,
                    subtitle = subtitle,
                    infoLines = infoLines,
                    onPlayAll = onPlayAll,
                    onSubtitleClick = onSubtitleClick,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = headerActions
                )
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = CoverUrls.large(coverUrl),
            contentDescription = strings.coverDesc,
            // 纯色占位:详情首开封面解码完成前不闪空块(封面直接落图,不做 crossfade)
            placeholder = androidx.compose.ui.graphics.painter.ColorPainter(LocalNcrustColors.current.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(NcrustShape.heroArtwork),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(20.dp))
        DetailHeaderInfoRow(
            title = title,
            subtitle = subtitle,
            infoLines = infoLines,
            onPlayAll = onPlayAll,
            onSubtitleClick = onSubtitleClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // 右侧附加操作（如"收藏专辑"按钮），左对齐、无 divider。
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            content = headerActions
        )
        Spacer(Modifier.height(16.dp))
    }
}

/** 标题 / 副标题 / info 行 + 右侧大圆播放按钮。宽窄屏共用。 */
@Composable
private fun DetailHeaderInfoRow(
    title: String,
    subtitle: String?,
    infoLines: List<String>,
    onPlayAll: (() -> Unit)?,
    onSubtitleClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SaltText(
                title,
                color = LocalNcrustColors.current.onBackground,
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Normal),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                SaltText(
                    subtitle,
                    color = LocalNcrustColors.current.primary,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = if (onSubtitleClick != null)
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = androidx.compose.ui.semantics.Role.Button,
                            onClick = onSubtitleClick
                        )
                    else Modifier
                )
            }
            infoLines.forEach { line ->
                Spacer(Modifier.height(3.dp))
                SaltText(line, color = LocalNcrustColors.current.onSurfaceVariant, style = TextStyle(fontSize = 13.sp))
            }
        }
        if (onPlayAll != null) {
            Spacer(Modifier.width(12.dp))
            PlayAllButton(size = 48.dp, onClick = onPlayAll)
        }
    }
}
