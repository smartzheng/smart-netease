package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * Metro 风文本输入框。直角无圆角、无边框、无 M3 outline/filled 语义。
 * 底色 surfaceVariant,placeholder 走 onSurfaceVariant,光标 primary。
 *
 * 取代 M3 TextField / OutlinedTextField:它们带圆角边框 + floating label +
 * 聚集态变色,Kanesumi 不需要。这里是"一个底色的矩形里打字",触摸下只用光标,
 * 键盘焦点通过共享 InteractionSource 在外容器画 MetroIndication 框。
 * placeholder 用 MetroText 叠在底下,不参与输入布局,多行时不会跟文字
 * 打架。
 *
 * 图标 / 前后缀 / 错误态都不内置 -- 没有真实用例驱动,避免预抽象。需要时用
 * Box 包一层自己拼。
 */
@Composable
fun MetroTextField(
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
    textStyle: TextStyle = LocalMetroTypography.current.body,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    containerColor: Color = LocalMetroColors.current.surfaceVariant,
    cursorColor: Color = LocalMetroColors.current.primary,
) {
    val colors = LocalMetroColors.current
    val mergedStyle = textStyle.copy(color = if (enabled) colors.onSurface else colors.onSurfaceVariant)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .background(containerColor)
            .indication(interactionSource, LocalIndication.current)
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            MetroText(
                text = placeholder,
                color = colors.onSurfaceVariant,
                style = mergedStyle,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.heightIn(min = 20.dp),
            enabled = enabled,
            readOnly = readOnly,
            interactionSource = interactionSource,
            textStyle = mergedStyle,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(cursorColor),
        )
    }
}
