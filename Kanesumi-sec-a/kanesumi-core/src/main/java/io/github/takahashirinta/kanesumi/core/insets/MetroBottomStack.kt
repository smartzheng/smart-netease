package io.github.takahashirinta.kanesumi.core.insets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class MetroBottomStack internal constructor() {
    private val reservations = mutableStateMapOf<Any, Dp>()

    internal fun register(key: Any, height: Dp) {
        reservations[key] = height
    }

    internal fun unregister(key: Any) {
        reservations.remove(key)
    }

    val totalHeightDp: Dp
        get() = reservations.values.fold(0.dp) { acc, height -> acc + height }

    val reservationsByKey: Map<Any, Dp>
        get() = reservations.toMap()
}

val LocalMetroBottomStack = staticCompositionLocalOf<MetroBottomStack> {
    error(
        "MetroBottomStack not provided. Wrap your app root with " +
        "MetroBottomStackScope { … } before calling rememberBottomStackReservation " +
        "or bottomOverlayPadding()."
    )
}

@Composable
fun MetroBottomStackScope(content: @Composable () -> Unit) {
    val stack = remember { MetroBottomStack() }
    CompositionLocalProvider(LocalMetroBottomStack provides stack) { content() }
}

@Composable
fun rememberBottomStackReservation(key: Any, heightDp: Dp) {
    val stack = LocalMetroBottomStack.current
    DisposableEffect(stack, key, heightDp) {
        stack.register(key, heightDp)
        onDispose { stack.unregister(key) }
    }
}

@Composable
fun bottomOverlayPadding(): PaddingValues =
    PaddingValues(bottom = LocalMetroBottomStack.current.totalHeightDp)

@Composable
fun Modifier.metroBottomOverlayPadding(): Modifier =
    this.padding(bottomOverlayPadding())
