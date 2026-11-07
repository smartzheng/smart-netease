package com.smart.neteast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors

/** Shared elevated surface keeps account, cache, QR and system-permission dialogs visually related. */
@Composable
fun Modifier.ncrustDialogSurface(): Modifier {
    val shape = RoundedCornerShape(24.dp)
    return clip(shape)
        .background(LocalMetroColors.current.surface)
        .border(1.dp, LocalMetroColors.current.divider.copy(alpha = 0.4f), shape)
}
