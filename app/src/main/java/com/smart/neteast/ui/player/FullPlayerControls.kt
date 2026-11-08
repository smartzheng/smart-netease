package com.smart.neteast.ui.player

import com.smart.neteast.ui.theme.LocalNcrustColors

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.neteast.formatDuration
import com.smart.neteast.ui.i18n.LocalStrings
import com.smart.neteast.ui.design.SaltIcon
import com.smart.neteast.ui.theme.NcrustSpacing
import com.moriafly.salt.ui.Text as SaltText
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FullPlayerControls(
    isPlaying: Boolean,
    showLyrics: Boolean,
    showQueue: Boolean,
    progressFlow: StateFlow<Float>,
    positionFlow: StateFlow<Long>,
    durationFlow: StateFlow<Long>,
    qualityIndexFlow: StateFlow<Int>,
    qualityOptions: List<String>,
    // 当前歌是否为试听片段（45 秒）。徽标位置改显示「试听」，不显示会误导的档位名。
    trialFlow: StateFlow<Boolean>,
    onPlayPause: () -> Unit,
    onPlayPrevious: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onToggleLyrics: () -> Unit,
    onToggleQueue: () -> Unit,
    onTogglePlayMode: () -> Unit = {},
    onAddToLibrary: () -> Unit = {},
    // 当前歌是否已在收藏库: true 显示对号, 按下走"移出库"分支
    isInLibrary: Boolean = false,
    isBufferingFlow: StateFlow<Boolean>,
    onSeek: (Float) -> Unit = {},
    onNavigateToUser: () -> Unit = {},
    onShowQualityPanel: () -> Unit = {},
    lyricsUnavailable: Boolean = false,
    previousEnabled: Boolean = true,
    // 宽屏左栏用紧凑尺寸：缩小按钮/间距，把纵向空间让给封面区。
    compact: Boolean = false,
    // 时间与进度合行，面板切换交给顶栏。
    tiny: Boolean = false,
    // 宽屏横向布局：进度行 + 操作行扁平铺开，替代手机的竖向大按钮堆叠。
    landscape: Boolean = false,
    artworkStyle: Boolean = false,
) {
    val strings = LocalStrings.current
    val controlColor = if (artworkStyle) Color.White else LocalNcrustColors.current.onBackground
    // 触觉反馈:播放/暂停/切歌/开关面板给一个轻振,补足无 ripple 时代的确认感
    val haptic = LocalHapticFeedback.current
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)

    val hPad = if (compact) 16.dp else 24.dp
    val bottomPad = if (compact) 8.dp else 18.dp
    val gapTight = if (compact) 4.dp else 8.dp
    val gapLoose = if (compact) 8.dp else 12.dp
    val ctrlRowPad = if (compact) 2.dp else 8.dp
    val sideBtn = if (compact) 48.dp else 56.dp
    val sideIcon = if (compact) 27.dp else 32.dp
    val playBtn = if (compact) 52.dp else 64.dp
    val playIcon = if (compact) 34.dp else 42.dp
    val sideGap = if (compact) 16.dp else 24.dp
    val toggleBtn = if (compact) 44.dp else 56.dp
    val toggleIcon = if (compact) 26.dp else 32.dp

    if (landscape) {
        // 宽屏横向控件条：进度行（位置 · 进度 · 时长）+ 操作行（面板开关 · 音质 · 传输），
        // 扁平铺开，不再照搬手机的竖向大按钮堆叠。
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PositionText(positionFlow = positionFlow, modifier = Modifier, color = controlColor)
                Spacer(Modifier.width(8.dp))
                SlimProgressBar(
                    progressFlow = progressFlow,
                    durationFlow = durationFlow,
                    isBufferingFlow = isBufferingFlow,
                    onSeek = onSeek,
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 0.dp,
                    activeColor = if (artworkStyle) Color.White else null,
                    inactiveColor = if (artworkStyle) Color.White.copy(alpha = 0.34f) else null,
                )
                Spacer(Modifier.width(8.dp))
                DurationText(durationFlow = durationFlow, modifier = Modifier, color = controlColor)
            }
            Spacer(Modifier.height(6.dp))
            // 对称三段：左=面板开关，中=传输（主控居中），右=音质。
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(NcrustSpacing.touchTarget)
                            .clickable(enabled = !lyricsUnavailable, role = androidx.compose.ui.semantics.Role.Button) {
                                tick()
                                onToggleLyrics()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SaltIcon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = strings.lyricsButton,
                            tint = if (lyricsUnavailable) LocalNcrustColors.current.onSurfaceVariant.copy(alpha = 0.5f)
                            else if (showLyrics) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                            sizeDp = 24.dp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(NcrustSpacing.touchTarget)
                            .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                                tick()
                                onToggleQueue()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SaltIcon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = strings.queueButton,
                            tint = if (showQueue) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                            sizeDp = 24.dp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(NcrustSpacing.touchTarget)
                            .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                                tick()
                                onAddToLibrary()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SaltIcon(
                            imageVector = if (isInLibrary) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = strings.addToLibraryButton,
                            tint = if (isInLibrary) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                            sizeDp = 24.dp
                        )
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransportControlButton(
                        glyph = TransportGlyph.Previous,
                        contentDescription = strings.prevButton,
                        tint = if (previousEnabled) LocalNcrustColors.current.onBackground else LocalNcrustColors.current.onSurfaceVariant.copy(alpha = 0.5f),
                        onClick = { tick(); onPlayPrevious() },
                        touchSize = 48.dp,
                        iconSize = 28.dp,
                        enabled = previousEnabled,
                    )
                    TransportControlButton(
                        glyph = if (isPlaying) TransportGlyph.Pause else TransportGlyph.Play,
                        contentDescription = if (isPlaying) strings.pauseButton else strings.playButton,
                        tint = LocalNcrustColors.current.primary,
                        onClick = { tick(); onPlayPause() },
                        touchSize = 54.dp,
                        iconSize = 36.dp,
                    )
                    TransportControlButton(
                        glyph = TransportGlyph.Next,
                        contentDescription = strings.nextButton,
                        tint = LocalNcrustColors.current.onBackground,
                        onClick = { tick(); onPlayNext() },
                        touchSize = 48.dp,
                        iconSize = 28.dp,
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onShowQualityPanel() }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    QualityLabel(
                        qualityIndexFlow = qualityIndexFlow,
                        trialFlow = trialFlow,
                        options = qualityOptions
                    )
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = bottomPad)) {
        if (tiny) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = hPad),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PositionText(positionFlow, Modifier.widthIn(max = 48.dp), controlColor)
                Spacer(Modifier.width(8.dp))
                SlimProgressBar(
                    progressFlow = progressFlow,
                    durationFlow = durationFlow,
                    isBufferingFlow = isBufferingFlow,
                    onSeek = onSeek,
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 0.dp,
                    activeColor = if (artworkStyle) Color.White else null,
                    inactiveColor = if (artworkStyle) Color.White.copy(alpha = 0.34f) else null,
                )
                Spacer(Modifier.width(8.dp))
                DurationText(durationFlow, Modifier.widthIn(max = 48.dp), controlColor)
                Spacer(Modifier.width(8.dp))
                // 最长多语言音质名也只占 64dp；省略号截断，不侵占进度条的权重空间。
                Box(
                    Modifier.widthIn(max = 64.dp)
                        .clickable { onShowQualityPanel() }
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    QualityLabel(qualityIndexFlow, trialFlow, qualityOptions)
                }
            }
        } else {
            SlimProgressBar(
                progressFlow = progressFlow,
                durationFlow = durationFlow,
                isBufferingFlow = isBufferingFlow,
                onSeek = onSeek,
                modifier = Modifier.padding(horizontal = hPad),
                horizontalPadding = 0.dp,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = hPad),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PositionText(positionFlow, Modifier.weight(1f), controlColor)
                DurationText(
                    durationFlow,
                    Modifier.weight(1f).wrapContentWidth(Alignment.End),
                    controlColor,
                )
            }
        }
        Spacer(Modifier.height(gapLoose))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = ctrlRowPad),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransportControlButton(
                glyph = TransportGlyph.Previous,
                contentDescription = strings.prevButton,
                tint = if (previousEnabled) controlColor else controlColor.copy(alpha = 0.4f),
                onClick = { tick(); onPlayPrevious() },
                touchSize = sideBtn,
                iconSize = sideIcon,
                enabled = previousEnabled,
            )
            Spacer(Modifier.width(sideGap))
            TransportControlButton(
                glyph = if (isPlaying) TransportGlyph.Pause else TransportGlyph.Play,
                contentDescription = if (isPlaying) strings.pauseButton else strings.playButton,
                tint = controlColor,
                onClick = { tick(); onPlayPause() },
                touchSize = playBtn,
                iconSize = playIcon,
            )
            Spacer(Modifier.width(sideGap))
            TransportControlButton(
                glyph = TransportGlyph.Next,
                contentDescription = strings.nextButton,
                tint = controlColor,
                onClick = { tick(); onPlayNext() },
                touchSize = sideBtn,
                iconSize = sideIcon,
            )
        }
        if (!tiny) {
            PlayerActionRow(
                showLyrics = showLyrics,
                showQueue = showQueue,
                isInLibrary = isInLibrary,
                lyricsUnavailable = lyricsUnavailable,
                onTogglePlayMode = onTogglePlayMode,
                onToggleLyrics = onToggleLyrics,
                onShowQualityPanel = onShowQualityPanel,
                onToggleQueue = onToggleQueue,
                onAddToLibrary = onAddToLibrary,
                artworkStyle = artworkStyle,
            )
        }
    }
}

@Composable
private fun PlayerActionRow(
    showLyrics: Boolean,
    showQueue: Boolean,
    isInLibrary: Boolean,
    lyricsUnavailable: Boolean,
    onTogglePlayMode: () -> Unit,
    onToggleLyrics: () -> Unit,
    onShowQualityPanel: () -> Unit,
    onToggleQueue: () -> Unit,
    onAddToLibrary: () -> Unit,
    artworkStyle: Boolean,
) {
    val strings = LocalStrings.current
    val colors = LocalNcrustColors.current
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val actions = listOf(
            Triple(Icons.Rounded.Repeat, strings.playModeButton, onTogglePlayMode),
            Triple(Icons.Rounded.Lyrics, strings.lyricsButton, onToggleLyrics),
            Triple(Icons.Rounded.GraphicEq, strings.qualitySectionTitle, onShowQualityPanel),
            Triple(Icons.AutoMirrored.Filled.PlaylistPlay, strings.queueButton, onToggleQueue),
            Triple(if (isInLibrary) Icons.Rounded.Check else Icons.Rounded.Add, strings.addToLibraryButton, onAddToLibrary),
        )
        actions.forEachIndexed { index, (icon, description, action) ->
            val interaction = remember(icon) { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            val pressFill by androidx.compose.animation.animateColorAsState(
                targetValue = if (pressed) (if (artworkStyle) Color.White else colors.primary).copy(alpha = 0.14f) else Color.Transparent,
                animationSpec = tween(110),
                label = "playerActionPressFill",
            )
            val pressScale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (pressed) 0.94f else 1f,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 520f),
                label = "playerActionPressScale",
            )
            val selected = when (index) {
                1 -> showLyrics
                3 -> showQueue
                4 -> isInLibrary
                else -> false
            }
            Box(
                modifier = Modifier.size(48.dp)
                    .background(pressFill, CircleShape)
                    .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = index != 1 || !lyricsUnavailable,
                    role = androidx.compose.ui.semantics.Role.Button,
                    onClickLabel = description,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        action()
                    },
                ),
                contentAlignment = Alignment.Center,
            ) {
                SaltIcon(
                    imageVector = icon,
                    contentDescription = description,
                    tint = if (artworkStyle) Color.White else if (selected) colors.primary else colors.onBackground,
                    sizeDp = 24.dp,
                    modifier = Modifier.scale(pressScale),
                )
            }
        }
    }
}

/** Tiny 顶栏与常规底部切换行共用同一组动作及语义。 */
@Composable
fun PlayerPanelToggles(
    showLyrics: Boolean,
    showQueue: Boolean,
    isInLibrary: Boolean,
    lyricsUnavailable: Boolean,
    onToggleLyrics: () -> Unit,
    onToggleQueue: () -> Unit,
    onAddToLibrary: () -> Unit,
    toggleBtn: Dp,
    toggleIcon: Dp,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val haptic = LocalHapticFeedback.current
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    Row(modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(
            modifier = Modifier
                .size(toggleBtn)
                .clickable(enabled = !lyricsUnavailable) {
                    tick()
                    onToggleLyrics()
                },
            contentAlignment = Alignment.Center
        ) {
            SaltIcon(
                imageVector = Icons.Rounded.Lyrics,
                contentDescription = strings.lyricsButton,
                // 无歌词/未加载时置灰, 语义是该歌不可展开歌词
                tint = if (lyricsUnavailable) LocalNcrustColors.current.onSurfaceVariant.copy(alpha = 0.5f)
                else if (showLyrics) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                sizeDp = toggleIcon
            )
        }
        Box(
            modifier = Modifier
                .size(toggleBtn)
                .clickable {
                    tick()
                    onToggleQueue()
                },
            contentAlignment = Alignment.Center
        ) {
            SaltIcon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                contentDescription = strings.queueButton,
                tint = if (showQueue) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                sizeDp = toggleIcon
            )
        }
        Box(
            modifier = Modifier
                .size(toggleBtn)
                .clickable {
                    tick()
                    onAddToLibrary()
                },
            contentAlignment = Alignment.Center
        ) {
            // 已在库 → 对号(按动移出); 不在库 → 加号(按动收藏)
            SaltIcon(
                imageVector = if (isInLibrary) Icons.Rounded.Check else Icons.Rounded.Add,
                contentDescription = strings.addToLibraryButton,
                tint = if (isInLibrary) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                sizeDp = toggleIcon
            )
        }
    }
}

@Composable
private fun PositionText(positionFlow: StateFlow<Long>, modifier: Modifier, color: Color = LocalNcrustColors.current.onSurfaceVariant) {
    // 按生命周期收集：熄屏 / 后台时 2Hz 的位置更新不再推动重组。
    val position by positionFlow.collectAsStateWithLifecycle()
    SaltText(
        formatDuration(position),
        color = color,
        style = TextStyle(fontSize = 12.sp),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun DurationText(durationFlow: StateFlow<Long>, modifier: Modifier, color: Color = LocalNcrustColors.current.onSurfaceVariant) {
    val duration by durationFlow.collectAsStateWithLifecycle()
    SaltText(
        formatDuration(duration),
        color = color,
        style = TextStyle(fontSize = 12.sp),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun QualityLabel(
    qualityIndexFlow: StateFlow<Int>,
    trialFlow: StateFlow<Boolean>,
    options: List<String>
) {
    val qualityIndex by qualityIndexFlow.collectAsState()
    val isTrial by trialFlow.collectAsState()
    // 试听片段没有"实际档位"可言：显示档位名会让用户以为音质设置生效了。
    val label = if (isTrial) LocalStrings.current.trialBadge
    else options.getOrElse(qualityIndex) { options.getOrElse(3) { "" } }
    SaltText(
        label,
        color = LocalNcrustColors.current.primary,
        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * 迷你条上的音质徽标位：只在试听片段时显示「试听」。
 *
 * 单独做成叶子 Composable 让 [trialFlow] 的订阅只重组这一个 Text——
 * 迷你条挂在 PlayerCard 的常驻子树上，在那里 collect 会让整条随切歌重组。
 */
@Composable
fun TrialBadge(trialFlow: StateFlow<Boolean>, modifier: Modifier = Modifier) {
    val isTrial by trialFlow.collectAsState()
    if (!isTrial) return
    SaltText(
        LocalStrings.current.trialBadge,
        color = LocalNcrustColors.current.primary,
        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}
