package io.github.takahashirinta.kanesumi.core.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class EdgeInset(val dp: Dp, val px: Float) {
    companion object {
        val Zero = EdgeInset(0.dp, 0f)
    }
}

@Immutable
data class DisplayCutoutInsets(
    val top: EdgeInset,
    val bottom: EdgeInset,
    val left: EdgeInset,
    val right: EdgeInset,
) {
    companion object {
        val Zero = DisplayCutoutInsets(EdgeInset.Zero, EdgeInset.Zero, EdgeInset.Zero, EdgeInset.Zero)
    }
}

@Immutable
data class MetroInsets(
    val statusBar: EdgeInset,
    val navigationBar: EdgeInset,
    val displayCutout: DisplayCutoutInsets,
    val ime: EdgeInset,
) {
    companion object {
        val Zero = MetroInsets(
            statusBar = EdgeInset.Zero,
            navigationBar = EdgeInset.Zero,
            displayCutout = DisplayCutoutInsets.Zero,
            ime = EdgeInset.Zero,
        )
    }
}

@Composable
fun rememberMetroInsets(): MetroInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val statusBars = WindowInsets.statusBars.asPaddingValues()
    val navigationBars = WindowInsets.navigationBars.asPaddingValues()
    val cutout = WindowInsets.displayCutout.asPaddingValues()
    val ime = WindowInsets.ime.asPaddingValues()
    return MetroInsets(
        statusBar = statusBars.calculateTopPadding().toEdge(density),
        navigationBar = navigationBars.calculateBottomPadding().toEdge(density),
        displayCutout = DisplayCutoutInsets(
            top = cutout.calculateTopPadding().toEdge(density),
            bottom = cutout.calculateBottomPadding().toEdge(density),
            left = cutout.calculateLeftPadding(layoutDirection).toEdge(density),
            right = cutout.calculateRightPadding(layoutDirection).toEdge(density),
        ),
        ime = ime.calculateBottomPadding().toEdge(density),
    )
}

private fun Dp.toEdge(density: Density): EdgeInset =
    EdgeInset(this, with(density) { this@toEdge.toPx() })
