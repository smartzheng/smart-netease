package io.github.takahashirinta.kanesumi.sample

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.controls.MetroButton
import io.github.takahashirinta.kanesumi.controls.MetroListRow
import io.github.takahashirinta.kanesumi.controls.MetroTextField
import io.github.takahashirinta.kanesumi.core.insets.bottomOverlayPadding
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText
import io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNav
import io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNavItem
import io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNavLabelMode

/** 与首页共用真实 shell 底栏;预览不登记 overlay,防止三份示例虚增底部留白。 */
@Composable
internal fun FocusSmallScreenDemo(
    items: List<MetroBottomNavItem>,
    labelMode: MetroBottomNavLabelMode,
    heightDp: Dp,
    onLabelModeChange: (MetroBottomNavLabelMode) -> Unit,
    onHeightChange: (Dp) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val colors = LocalMetroColors.current
    var activations by remember { mutableIntStateOf(0) }
    var previewSelected by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    val modes = MetroBottomNavLabelMode.values().toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = bottomOverlayPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetroText("焦点 / 小屏", style = LocalMetroTypography.current.pageHeading)
                MetroButton("返回首页", onClick = onBack)
                MetroText("上下键 / Tab 遍历按钮和列表,左右键遍历 tab;确认键激活。触摸时隐藏键盘焦点框。")
                MetroText("已激活 $activations 次;底栏与下方三种预览使用相同高度。")
            }
        }
        item {
            MetroButton(
                text = "底栏标签: $labelMode (切换)",
                onClick = { onLabelModeChange(modes[(modes.indexOf(labelMode) + 1) % modes.size]) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurface,
            )
        }
        item {
            MetroButton(
                text = "导航高度: ${heightDp.value.toInt()}dp (56 / 48)",
                onClick = { onHeightChange(if (heightDp == 56.dp) 48.dp else 56.dp) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurface,
            )
        }
        item {
            MetroButton(
                text = "按住确认键: 按压 + 焦点同时可见",
                onClick = { activations++ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurface,
            )
        }
        item {
            MetroTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "触摸可编辑;键盘模式才显示焦点框",
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }
        item {
            MetroButton(
                text = "禁用按钮 (应跳过)",
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }
        items(modes, key = { it.name }) { mode ->
            Column {
                MetroText(
                    text = "$mode · ${heightDp.value.toInt()}dp",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = LocalMetroTypography.current.label,
                )
                MetroBottomNav(
                    items = items,
                    selectedIndex = previewSelected,
                    onSelected = { previewSelected = it },
                    heightDp = heightDp,
                    labelMode = mode,
                    autoReserveBottomStack = false,
                )
            }
        }
        item {
            MetroListRow(
                title = "只读列表行 (应跳过)",
                subtitle = "未提供 onClick,不创建焦点目标",
                contentPadding = PaddingValues(16.dp),
            )
        }
        items(12) { index ->
            MetroListRow(
                title = "可操作列表行 ${index + 1}",
                subtitle = "确认键激活;上下移焦并滚动到末行",
                onClick = { activations++ },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
