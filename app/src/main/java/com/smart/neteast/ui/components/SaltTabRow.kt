package com.smart.neteast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.BasicButton
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

/** Shared text-first tabs using SaltUI theme values and its interaction feedback. */
@Composable
fun SaltTabRow(
    items: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val selected = selectedTabIndex == index
            BasicButton(
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                backgroundColor = if (selected) {
                    SaltTheme.colors.subBackground
                } else {
                    Color.Transparent
                },
            ) {
                Text(
                    text = item,
                    color = if (selected) SaltTheme.colors.highlight else SaltTheme.colors.subText,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}
