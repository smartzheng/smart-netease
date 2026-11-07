package io.github.takahashirinta.kanesumi.core.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Kanesumi 的默认交互反馈 —— 直角矩形闪切,取代 M3 ripple。
 *
 * 按下:tint 从 0 → 1 alpha 淡入 (enterDurationMs, EaseOutCubic)。
 * 松手 / 取消:1 → 0 淡出 (exitDurationMs)。
 * 无圆角、无 ripple 扩散、无 elevation 联动 —— Metro 到底。
 *
 * MetroTheme 通过 LocalIndication 自动挂上本 indication,任何 .clickable {}
 * 默认走此反馈,不需要显式设置 indication 参数。
 *
 * 键盘 / 方向键焦点是向内的 2dp 直角框,无动画,在按压叠层之后绘制。
 * [focusColor] 默认使用节点所在 MetroTheme 的 primary;[focusEnabled] 可关闭焦点框,
 * 不影响原有按压反馈。新参数追加在旧构造参数之后,旧的位置调用仍然有效。
 *
 * clickable 内置 non-touch canFocus,但它不约束 BasicTextField、自定义 focusable
 * 或调用方 focusProperties 覆盖。因此绘制时仍显式检查 InputMode:
 * Touch 下绝不画键盘焦点框,切回 Keyboard 后按实际焦点恢复,不依赖新 Focus 事件。
 * 本 indication 只反馈共享 InteractionSource 的焦点,不会替控件创建焦点目标。
 *
 * EaseOutCubic (1-(1-t)³) 与 Sokuou 里的 MetroCubic 数学上等价,故 core 不
 * 需要依赖 anim 就能保持"感觉一致"。
 *
 * 实现走 Compose 1.7+ 的 IndicationNodeFactory + Modifier.Node,alpha 读取
 * 在 draw phase 内完成,零重组。
 */
class MetroIndication(
    private val tint: Color,
    private val enterDurationMs: Int = 100,
    private val exitDurationMs: Int = 200,
    private val focusEnabled: Boolean = true,
    private val focusColor: Color = Color.Unspecified,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        MetroIndicationNode(tint, interactionSource, enterDurationMs, exitDurationMs, focusEnabled, focusColor)

    override fun equals(other: Any?): Boolean =
        other is MetroIndication &&
        tint == other.tint &&
        enterDurationMs == other.enterDurationMs &&
        exitDurationMs == other.exitDurationMs &&
        focusEnabled == other.focusEnabled &&
        focusColor == other.focusColor

    override fun hashCode(): Int {
        var result = tint.hashCode()
        result = 31 * result + enterDurationMs
        result = 31 * result + exitDurationMs
        result = 31 * result + focusEnabled.hashCode()
        result = 31 * result + focusColor.hashCode()
        return result
    }
}

private class MetroIndicationNode(
    private val tint: Color,
    private val interactionSource: InteractionSource,
    private val enterDurationMs: Int,
    private val exitDurationMs: Int,
    private val focusEnabled: Boolean,
    private val focusColor: Color,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val alpha = Animatable(0f)
    // 事件侧只写 snapshot state,唯一的读取者是 draw;不把焦点提升到组合阶段。
    private val focused = mutableStateOf(false)
    private val activeFocuses = mutableSetOf<FocusInteraction.Focus>()

    override fun onAttach() {
        // 按压保留旧的完整淡入 / 淡出序列;独立收集焦点,不等按压动画结束才移焦。
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(enterDurationMs, easing = EaseOutCubic),
                    )
                    is PressInteraction.Release, is PressInteraction.Cancel -> alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(exitDurationMs, easing = EaseOutCubic),
                    )
                }
            }
        }
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> {
                        activeFocuses.add(interaction)
                        focused.value = true
                    }
                    is FocusInteraction.Unfocus -> {
                        activeFocuses.remove(interaction.focus)
                        focused.value = activeFocuses.isNotEmpty()
                    }
                }
            }
        }
    }

    override fun onDetach() {
        activeFocuses.clear()
        focused.value = false
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        drawRect(color = tint, alpha = alpha.value)
        if (focusEnabled && focused.value &&
            currentValueOf(LocalInputModeManager).inputMode == InputMode.Keyboard
        ) {
            // Stroke 以路径为中心,内缩半笔宽才能保证全部落在组件边界内。
            val width = 2.dp.toPx().coerceAtMost(minOf(size.width, size.height))
            if (width > 0f) {
                drawRect(
                    color = if (focusColor == Color.Unspecified) currentValueOf(LocalMetroColors).primary else focusColor,
                    topLeft = Offset(width / 2f, width / 2f),
                    size = Size(size.width - width, size.height - width),
                    style = Stroke(width),
                )
            }
        }
    }
}
