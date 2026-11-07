package com.smart.neteast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme

/** Shared elevated surface keeps account, cache, QR and system-permission dialogs visually related. */
@Composable
fun Modifier.ncrustDialogSurface(): Modifier {
    val shape = SaltTheme.shapes.large
    return clip(shape)
        .background(SaltTheme.colors.popup)
        .border(1.dp, SaltTheme.colors.stroke, shape)
}
