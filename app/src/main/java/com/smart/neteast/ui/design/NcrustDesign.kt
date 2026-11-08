package com.smart.neteast.ui.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import com.smart.neteast.ui.theme.NcrustSpacing
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.ui.Text as SaltText
import com.smart.neteast.ui.theme.LocalNcrustColors
import com.smart.neteast.ui.theme.LocalNcrustTypography

/** Motion tokens used by Ncrust's Salt UI layer. */
object NcrustMotion {
    val StandardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val CoverFade = tween<Float>(durationMillis = 220, easing = StandardEasing)
    val Interaction = spring<Float>(dampingRatio = 0.82f, stiffness = Spring.StiffnessMedium)
    val PlayerTransition = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow,
    )
}

fun ncrustSpring(response: Float, dampingRatio: Float) = spring<Float>(
    dampingRatio = dampingRatio,
    stiffness = ((2f * Math.PI.toFloat() / response.coerceAtLeast(0.05f)).let { it * it })
        .coerceIn(Spring.StiffnessLow, Spring.StiffnessHigh),
)

@Composable
fun rememberNcrustFlingBehavior(): FlingBehavior = ScrollableDefaults.flingBehavior()

@Composable
fun NcrustIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    touchTargetDp: Dp = 48.dp,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .sizeIn(
                minWidth = touchTargetDp.coerceAtLeast(NcrustSpacing.touchTarget),
                minHeight = touchTargetDp.coerceAtLeast(NcrustSpacing.touchTarget),
            )
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
fun NcrustDivider(modifier: Modifier = Modifier, color: Color = LocalNcrustColors.current.divider) {
    Canvas(modifier.fillMaxWidth().height(1.dp)) {
        drawLine(color, androidx.compose.ui.geometry.Offset.Zero, androidx.compose.ui.geometry.Offset(size.width, 0f), 1.dp.toPx())
    }
}

@Composable
fun NcrustProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalNcrustColors.current.primary,
    sizeDp: Dp = 36.dp,
    strokeDp: Dp = 3.dp,
) {
    val transition = rememberInfiniteTransition(label = "ncrustProgress")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )
    Canvas(modifier.size(sizeDp)) {
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 260f,
            useCenter = false,
            style = Stroke(width = strokeDp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
fun NcrustDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalNcrustColors.current.surface,
    maxWidthDp: Dp = 280.dp,
    alignment: Alignment = Alignment.BottomStart,
    cornerRadiusDp: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val shape = RoundedCornerShape(cornerRadiusDp)
    val density = LocalDensity.current
    Popup(
        alignment = alignment,
        offset = IntOffset(0, with(density) { 8.dp.roundToPx() }),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        AnimatedVisibility(visible = true, enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.96f, animationSpec = tween(160))) {
            Column(
                modifier = modifier
                    .widthIn(max = maxWidthDp)
                    .clip(shape)
                    .background(containerColor)
                    .border(1.dp, LocalNcrustColors.current.divider, shape)
                    .padding(vertical = 6.dp),
                content = content,
            )
        }
    }
}

@Composable
fun NcrustDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = LocalNcrustColors.current.onSurface,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = NcrustSpacing.touchTarget).clickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        ).padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(12.dp)) }
        SaltText(
            text,
            modifier = Modifier.weight(1f),
            color = if (enabled) textColor else textColor.copy(alpha = 0.45f),
            style = LocalNcrustTypography.current.bodyMedium,
        )
        if (trailing != null) { Spacer(Modifier.width(12.dp)); trailing() }
    }
}

@Composable
fun NcrustTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String = "",
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle = LocalNcrustTypography.current.bodyMedium,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    containerColor: Color = LocalNcrustColors.current.surfaceVariant,
    cursorColor: Color = LocalNcrustColors.current.primary,
) {
    val colors = LocalNcrustColors.current
    Box(modifier.background(containerColor).padding(contentPadding), contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            SaltText(placeholder, color = colors.onSurfaceVariant, style = textStyle)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.copy(color = if (enabled) colors.onSurface else colors.onSurfaceVariant),
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(cursorColor),
        )
    }
}

@Composable
fun NcrustSelectorFlyout(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeightDp: Dp = 48.dp,
    maxWidthDp: Dp = 320.dp,
    maxHeightDp: Dp = 400.dp,
    containerColor: Color = LocalNcrustColors.current.surface,
    activeColor: Color = LocalNcrustColors.current.primary,
    cornerRadiusDp: Dp = 18.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
) {
    if (!expanded) return
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.roundToPx() }
    val maxHeightPx = with(density) { maxHeightDp.roundToPx() }
    LaunchedEffect(selectedIndex, options.size) {
        if (selectedIndex in options.indices) {
            val centerOffset = ((maxHeightPx - itemHeightPx) / 2).coerceAtLeast(0)
            listState.scrollToItem(selectedIndex, scrollOffset = -centerOffset)
        }
    }
    val shape = RoundedCornerShape(cornerRadiusDp)
    val positionProvider = remember(horizontalAlignment) { NcrustSelectorPositionProvider(horizontalAlignment) }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        AnimatedVisibility(visible = true, enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.96f, animationSpec = tween(160))) {
            LazyColumn(
                state = listState,
                modifier = modifier.widthIn(max = maxWidthDp).heightIn(max = maxHeightDp)
                    .clip(shape).background(containerColor).border(1.dp, LocalNcrustColors.current.divider, shape),
            ) {
                itemsIndexed(options) { index, label ->
                    val selected = index == selectedIndex
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = itemHeightDp)
                            .background(if (selected) activeColor.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onSelect(index); onDismissRequest() }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SaltText(label, modifier = Modifier.weight(1f), color = if (selected) activeColor else LocalNcrustColors.current.onSurface)
                    }
                }
            }
        }
    }
}

private class NcrustSelectorPositionProvider(
    private val horizontalAlignment: Alignment.Horizontal,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val centerY = anchorBounds.top + anchorBounds.height / 2
        val y = (centerY - popupContentSize.height / 2)
            .coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        val x = when (horizontalAlignment) {
            Alignment.Start -> anchorBounds.left
            Alignment.End -> anchorBounds.right - popupContentSize.width
            else -> anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        }.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        return IntOffset(x, y)
    }
}

@Composable
fun NcrustTopScrim(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    scrimHeightDp: Dp = 120.dp,
    scrimAlphaTop: Float = 0.55f,
    iconColor: Color = Color.White,
    iconSizeDp: Dp = 24.dp,
) {
    Box(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(scrimHeightDp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = scrimAlphaTop), Color.Transparent))))
        NcrustIconButton(onClick = onClick, modifier = Modifier.align(alignment).windowInsetsPadding(WindowInsets.statusBars)) {
            Image(
                painter = rememberVectorPainter(icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSizeDp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconColor),
            )
        }
    }
}

data class NcrustSidebarItem(val icon: ImageVector, val label: String)

@Composable
fun NcrustSidebar(
    items: List<NcrustSidebarItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 240.dp,
    backgroundColor: Color = LocalNcrustColors.current.surface,
    activeColor: Color = LocalNcrustColors.current.onSurface,
    inactiveColor: Color = LocalNcrustColors.current.onSurfaceVariant,
    indicatorColor: Color = LocalNcrustColors.current.primary,
    rowHeight: Dp = 56.dp,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(modifier.width(width).fillMaxHeight().background(backgroundColor).selectableGroup()) {
        header?.invoke(this)
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = rowHeight)
                    .background(if (selected) indicatorColor.copy(alpha = 0.12f) else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab) { onSelected(index) }
                    .padding(horizontal = NcrustSpacing.pageGutter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) Box(Modifier.width(3.dp).height(24.dp).background(indicatorColor))
                else Spacer(Modifier.width(3.dp))
                Spacer(Modifier.width(14.dp))
                Image(
                    painter = rememberVectorPainter(item.icon),
                    contentDescription = null,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (selected) activeColor else inactiveColor),
                )
                Spacer(Modifier.width(14.dp))
                SaltText(item.label, color = if (selected) activeColor else inactiveColor)
            }
        }
        Spacer(Modifier.weight(1f))
        footer?.invoke(this)
    }
}

data class NcrustLyricLine(val timeMs: Long, val text: String, val translation: String = "")

@Composable
fun NcrustLyricsPanel(
    lines: List<NcrustLyricLine>,
    currentPositionMillis: () -> Long,
    isVisible: Boolean,
    forcedScrollTrigger: Int,
    enabled: Boolean,
    onLineClick: (Long) -> Unit,
    onUserScrolled: () -> Unit,
    pastLineColor: Color,
    futureLineColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    translationFontSize: androidx.compose.ui.unit.TextUnit,
    translationLineHeight: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val position = currentPositionMillis()
    val activeIndex = lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    val state = rememberLazyListState()
    var lastManualScrollAt by remember { mutableLongStateOf(0L) }
    val scrollConnection = remember(onUserScrolled) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource,
            ): androidx.compose.ui.geometry.Offset {
                if (source == NestedScrollSource.UserInput && consumed.y != 0f) {
                    lastManualScrollAt = System.currentTimeMillis()
                    onUserScrolled()
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
    LaunchedEffect(activeIndex, forcedScrollTrigger, isVisible, lastManualScrollAt) {
        if (isVisible && lines.isNotEmpty()) {
            val pauseRemaining = 5_000L - (System.currentTimeMillis() - lastManualScrollAt)
            if (pauseRemaining > 0) kotlinx.coroutines.delay(pauseRemaining)
            val topOffset = -state.layoutInfo.viewportSize.height / 3
            state.animateScrollToItem(activeIndex, scrollOffset = topOffset)
        }
    }
    LazyColumn(
        state = state,
        modifier = modifier.nestedScroll(scrollConnection),
        contentPadding = PaddingValues(vertical = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Lyrics can legitimately share a timestamp (most often 0 ms for intro lines).
        // Include the row position so equal timestamps do not collide in LazyColumn.
        itemsIndexed(lines, key = { index, line -> "lyric-$index-${line.timeMs}" }) { index, line ->
            Column(
                modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onLineClick(line.timeMs) },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SaltText(
                    line.text,
                    color = if (index <= activeIndex) pastLineColor else futureLineColor,
                    style = TextStyle(fontSize = fontSize, lineHeight = lineHeight),
                )
                if (line.translation.isNotBlank()) {
                    SaltText(line.translation, color = futureLineColor, style = TextStyle(fontSize = translationFontSize, lineHeight = translationLineHeight))
                }
            }
        }
    }
}
