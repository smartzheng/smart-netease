package com.smart.neteast.ui.player

import com.smart.neteast.ui.theme.LocalNcrustColors
import com.smart.neteast.ui.theme.NcrustShape


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Text as SaltText
import com.smart.neteast.ui.design.SaltIcon
import com.smart.neteast.ui.theme.NcrustIconSize
import com.smart.neteast.ui.i18n.LocalStrings
import com.smart.neteast.ui.theme.NcrustSpacing

@Composable
fun QualitySelectionPanel(
    selectedIndex: Int,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = LocalNcrustColors.current
    val strings = LocalStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.44f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss
                    )
            )

            Column(
                Modifier.fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .clip(NcrustShape.sheet)
                    .background(colors.surface)
                    .selectableGroup()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp)
            ) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp)
                        .size(width = 34.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.onSurfaceVariant.copy(alpha = 0.38f))
                )
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SaltIcon(Icons.Rounded.GraphicEq, null, tint = colors.primary, sizeDp = NcrustIconSize.regular)
                    Spacer(Modifier.width(12.dp))
                    SaltText(
                        strings.qualitySectionTitle,
                        color = colors.onBackground,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier.size(NcrustSpacing.touchTarget).clip(NcrustShape.pill)
                            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        SaltIcon(Icons.Rounded.Close, strings.close, tint = colors.onSurfaceVariant, sizeDp = NcrustIconSize.medium)
                    }
                }
                Spacer(Modifier.height(4.dp))
                options.forEachIndexed { index, label ->
                    val selected = index == selectedIndex
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clip(NcrustShape.control)
                            .background(if (selected) colors.primary.copy(alpha = 0.11f) else Color.Transparent)
                            .selectable(
                                selected = selected,
                                role = androidx.compose.ui.semantics.Role.RadioButton,
                                onClick = { onSelect(index) }
                            )
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SaltText(
                            label,
                            color = if (selected) colors.primary else colors.onBackground,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) SaltIcon(Icons.Rounded.Check, null, tint = colors.primary, sizeDp = 20.dp)
                    }
                }
            }
        }
    }
}
