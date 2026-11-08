package com.smart.neteast.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Shared spacing and hit-area values for the flat Salt UI layer. */
object NcrustSpacing {
    val compact = 8.dp
    val item = 12.dp
    val pageGutter = 16.dp
    val section = 24.dp
    val largeSection = 32.dp
    val touchTarget = 48.dp
}

/** Semantic shapes keep artwork, controls, cards, and sheets visually related. */
object NcrustShape {
    val control = RoundedCornerShape(12.dp)
    val thumbnail = RoundedCornerShape(10.dp)
    val artwork = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(16.dp)
    val heroArtwork = RoundedCornerShape(18.dp)
    val dialog = RoundedCornerShape(24.dp)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val pill = RoundedCornerShape(50)
}

/** A compact icon scale used by navigation, row actions, and primary controls. */
object NcrustIconSize {
    val small = 18.dp
    val medium = 20.dp
    val regular = 24.dp
    val prominent = 28.dp
    val hero = 32.dp
    val profile = 40.dp
}
