package com.smart.neteast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import com.moriafly.salt.ui.Text as SaltText

/** Shared compact page heading for the three primary tabs. */
@Composable
fun ScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 26.sp,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.width(3.dp).height(16.dp)
                .background(LocalMetroColors.current.primary, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(10.dp))
        SaltText(
            text = text,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.pageHeading.copy(
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
