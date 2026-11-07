package io.github.takahashirinta.kanesumi.core.insets

import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier

fun Modifier.metroStatusBarsPadding(): Modifier = statusBarsPadding()

fun Modifier.metroNavigationBarsPadding(): Modifier = navigationBarsPadding()

fun Modifier.metroSystemBarsPadding(): Modifier = systemBarsPadding()

fun Modifier.metroDisplayCutoutPadding(): Modifier = displayCutoutPadding()
