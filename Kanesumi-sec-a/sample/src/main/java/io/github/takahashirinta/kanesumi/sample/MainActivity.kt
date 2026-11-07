package io.github.takahashirinta.kanesumi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takahashirinta.kanesumi.controls.MetroBottomSheet
import io.github.takahashirinta.kanesumi.controls.MetroButton
import io.github.takahashirinta.kanesumi.controls.MetroDialog
import io.github.takahashirinta.kanesumi.controls.MetroDivider
import io.github.takahashirinta.kanesumi.controls.MetroDropdownMenu
import io.github.takahashirinta.kanesumi.controls.MetroDropdownMenuItem
import io.github.takahashirinta.kanesumi.controls.MetroIconButton
import io.github.takahashirinta.kanesumi.controls.MetroListRow
import io.github.takahashirinta.kanesumi.controls.MetroLyricLine
import io.github.takahashirinta.kanesumi.controls.MetroLyricsPanel
import io.github.takahashirinta.kanesumi.controls.MetroProgressIndicator
import io.github.takahashirinta.kanesumi.controls.MetroResponsiveContent
import io.github.takahashirinta.kanesumi.controls.MetroSurface
import io.github.takahashirinta.kanesumi.controls.MetroSwitch
import io.github.takahashirinta.kanesumi.controls.MetroTabItem
import io.github.takahashirinta.kanesumi.controls.MetroTabRow
import io.github.takahashirinta.kanesumi.core.insets.LocalMetroBottomStack
import io.github.takahashirinta.kanesumi.core.insets.MetroInsets
import io.github.takahashirinta.kanesumi.core.insets.bottomOverlayPadding
import io.github.takahashirinta.kanesumi.core.insets.metroNavigationBarsPadding
import io.github.takahashirinta.kanesumi.core.insets.rememberBottomStackReservation
import io.github.takahashirinta.kanesumi.core.insets.rememberMetroInsets
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon
import io.github.takahashirinta.kanesumi.core.theme.MetroText
import io.github.takahashirinta.kanesumi.core.theme.MetroTheme
import io.github.takahashirinta.kanesumi.structure.MetroAppBar
import io.github.takahashirinta.kanesumi.structure.MetroDetailScaffold
import io.github.takahashirinta.kanesumi.structure.MetroShell
import io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNav
import io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNavItem
import io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNavLabelMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetroTheme {
                SampleRoot()
            }
        }
    }
}

private val DemoMiniBar = Color(0xFF2E67B5)
private val DemoCoverA = Color(0xFF6B4E9E)
private val DemoCoverB = Color(0xFFB57332)
private val DemoCoverC = Color(0xFF3E7C51)

private val NavItems = listOf(
    MetroBottomNavItem(Icons.Filled.Home, "home"),
    MetroBottomNavItem(Icons.Filled.Search, "search"),
    MetroBottomNavItem(Icons.Filled.Star, "star"),
    MetroBottomNavItem(Icons.Filled.Settings, "settings"),
)

private data class DemoDetail(val title: String, val subtitle: String, val cover: Color)

private val DemoDetails = listOf(
    DemoDetail("Album Alpha", "Artist One · 2024 · 12 tracks", DemoCoverA),
    DemoDetail("Album Beta", "Artist Two · 2023 · 9 tracks", DemoCoverB),
    DemoDetail("Album Gamma", "Artist Three · 2025 · 15 tracks", DemoCoverC),
)

@Composable
private fun SampleRoot() {
    var miniBarVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var openedDetail by remember { mutableStateOf<DemoDetail?>(null) }
    var dialogOpen by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }
    var lyricsOpen by remember { mutableStateOf(false) }
    var focusDemoOpen by remember { mutableStateOf(false) }
    var focusLabelMode by remember { mutableStateOf(MetroBottomNavLabelMode.Always) }
    var focusNavHeight by remember { mutableStateOf(56.dp) }

    if (dialogOpen) {
        PlayAllStyleDialog(
            onDismiss = { dialogOpen = false },
        )
    }
    if (sheetOpen) {
        SongMenuSheetStyleDemo(onDismiss = { sheetOpen = false })
    }

    MetroShell(
        bottomBar = {
            Column {
                if (miniBarVisible) OverlayBar(label = "mini player (56dp)", tint = DemoMiniBar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LocalMetroColors.current.surface)
                        .metroNavigationBarsPadding(),
                ) {
                    MetroBottomNav(
                        items = NavItems,
                        selectedIndex = selectedTab,
                        onSelected = { selectedTab = it },
                        labelMode = if (focusDemoOpen) focusLabelMode else MetroBottomNavLabelMode.Always,
                        heightDp = if (focusDemoOpen) focusNavHeight else 56.dp,
                    )
                }
            }
        },
    ) {
        if (lyricsOpen) {
            LyricsDemo(onBack = { lyricsOpen = false })
        } else {
            if (miniBarVisible) {
                rememberBottomStackReservation(key = "sample.miniPlayer", heightDp = 56.dp)
            }

            MetroResponsiveContent {
                val current = openedDetail
                if (focusDemoOpen) {
                    FocusSmallScreenDemo(
                        items = NavItems,
                        labelMode = focusLabelMode,
                        heightDp = focusNavHeight,
                        onLabelModeChange = { focusLabelMode = it },
                        onHeightChange = { focusNavHeight = it },
                        onBack = { focusDemoOpen = false },
                    )
                } else if (current != null) {
                    DetailDemo(detail = current, onBack = { openedDetail = null })
                } else {
                    HomeDemo(
                        miniBarVisible = miniBarVisible,
                        onToggleMiniBar = { miniBarVisible = !miniBarVisible },
                        onOpenDetail = { openedDetail = it },
                        onOpenDialog = { dialogOpen = true },
                        onOpenSheet = { sheetOpen = true },
                        onOpenLyrics = { lyricsOpen = true },
                        onOpenFocusDemo = { focusDemoOpen = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDemo(
    miniBarVisible: Boolean,
    onToggleMiniBar: () -> Unit,
    onOpenDetail: (DemoDetail) -> Unit,
    onOpenDialog: () -> Unit,
    onOpenSheet: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenFocusDemo: () -> Unit,
) {
    val insets = rememberMetroInsets()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = bottomOverlayPadding(),
    ) {
        item {
            MetroAppBar(
                title = "Kanesumi · demo",
                actions = {
                    MetroIcon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "settings",
                        tint = LocalMetroColors.current.onSurfaceVariant,
                        sizeDp = 22.dp,
                    )
                },
            )
        }
        item {
            MetroButton(
                text = "焦点 / 小屏",
                onClick = onOpenFocusDemo,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item { DebugPanel(insets = insets) }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            MetroButton(
                text = if (miniBarVisible) "Hide mini player" else "Show mini player",
                onClick = onToggleMiniBar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                leadingIcon = Icons.Filled.Star,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            MetroButton(
                text = "Lyrics demo (Apple Music × Metro)",
                onClick = onOpenLyrics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetroButton(
                    text = "Open dialog",
                    onClick = onOpenDialog,
                    modifier = Modifier.weight(1f),
                )
                MetroButton(
                    text = "Open sheet",
                    onClick = onOpenSheet,
                    modifier = Modifier.weight(1f),
                    containerColor = LocalMetroColors.current.surfaceVariant,
                    contentColor = LocalMetroColors.current.onSurface,
                )
                Box(
                    modifier = Modifier
                        .background(LocalMetroColors.current.surfaceVariant)
                        .padding(16.dp),
                ) {
                    MetroProgressIndicator()
                }
                OverflowMenuButton()
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item { FormControlsShowcase() }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            MetroText(
                text = "Details",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = LocalMetroTypography.current.title,
            )
        }
        items(DemoDetails) { detail ->
            MetroListRow(
                title = detail.title,
                subtitle = detail.subtitle,
                leading = {
                    Box(
                        Modifier
                            .size(48.dp)
                            .background(detail.cover)
                    )
                },
                onClick = { onOpenDetail(detail) },
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
        items(60) { idx ->
            SimpleRow(index = idx)
        }
    }
}

@Composable
private fun DetailDemo(detail: DemoDetail, onBack: () -> Unit) {
    MetroDetailScaffold(
        onBack = onBack,
        backIcon = Icons.Filled.ArrowBack,
        header = { DetailHeader(detail) },
        content = { detailBody() },
    )
}

@Composable
private fun PlayAllStyleDialog(onDismiss: () -> Unit) {
    val colors = LocalMetroColors.current
    val typography = LocalMetroTypography.current
    MetroDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            MetroText(
                text = "Choose how to play",
                color = colors.onSurfaceVariant,
                style = typography.caption,
            )
        }
        MetroDivider()
        DialogRow(
            icon = Icons.Filled.PlayArrow,
            iconTint = colors.primary,
            title = "Play now",
            subtitle = "Replace queue with these songs",
            onClick = onDismiss,
        )
        MetroDivider()
        DialogRow(
            icon = Icons.Filled.Star,
            iconTint = colors.onSurface,
            title = "Insert next",
            subtitle = "Add right after the current song",
            onClick = onDismiss,
        )
        MetroDivider()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            MetroText(
                text = "Cancel",
                color = colors.onSurfaceVariant,
                style = typography.body,
            )
        }
    }
}

@Composable
private fun FormControlsShowcase() {
    val colors = LocalMetroColors.current
    val typography = LocalMetroTypography.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var autoPlay by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        MetroTabRow(
            items = listOf(
                MetroTabItem("All"),
                MetroTabItem("Albums"),
                MetroTabItem("Songs"),
            ),
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MetroText(text = "Auto-play", color = colors.onSurface, style = typography.body)
                MetroText(
                    text = "Start playing when tapped",
                    color = colors.onSurfaceVariant,
                    style = typography.caption,
                )
            }
            MetroSwitch(checked = autoPlay, onCheckedChange = { autoPlay = it })
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MetroText(text = "Icon buttons", color = colors.onSurface, style = typography.body)
            }
            MetroIconButton(onClick = {}) {
                MetroIcon(Icons.Filled.Star, "star", tint = colors.onSurface, sizeDp = 22.dp)
            }
            MetroIconButton(onClick = {}) {
                MetroIcon(Icons.Filled.Settings, "settings", tint = colors.onSurface, sizeDp = 22.dp)
            }
        }
    }
}

@Composable
private fun SongMenuSheetStyleDemo(onDismiss: () -> Unit) {
    val colors = LocalMetroColors.current
    val typography = LocalMetroTypography.current
    val song = remember {
        DemoDetail("Album Alpha", "Artist One · 2024 · 12 tracks", DemoCoverA)
    }
    MetroBottomSheet(
        onDismiss = onDismiss,
        applyNavigationBarsPadding = true,
        dragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(112.dp)
                        .background(song.cover)
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    MetroText(
                        text = song.title,
                        color = colors.onSurface,
                        style = typography.title,
                    )
                    Spacer(Modifier.height(4.dp))
                    MetroText(
                        text = song.subtitle,
                        color = colors.primary,
                        style = typography.caption,
                    )
                }
            }
        },
    ) {
        MetroDivider()
        SheetAction(Icons.Filled.PlayArrow, "Play next", colors.primary, onDismiss)
        MetroDivider()
        SheetAction(Icons.Filled.Star, "Add to library", colors.onSurface, onDismiss)
        MetroDivider()
        SheetAction(Icons.Filled.Settings, "Share", colors.onSurface, onDismiss)
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    onDismiss: () -> Unit,
) {
    val typography = LocalMetroTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDismiss)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetroIcon(imageVector = icon, contentDescription = null, tint = tint, sizeDp = 24.dp)
        Spacer(Modifier.width(16.dp))
        MetroText(text = label, color = LocalMetroColors.current.onSurface, style = typography.body)
    }
}

@Composable
private fun OverflowMenuButton() {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalMetroColors.current
    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colors.surfaceVariant)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            MetroIcon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "overflow",
                tint = colors.onSurface,
                sizeDp = 22.dp,
            )
        }
        MetroDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MetroDropdownMenuItem(
                text = "Play next",
                onClick = { expanded = false },
                leading = { MetroIcon(Icons.Filled.PlayArrow, null, tint = colors.primary, sizeDp = 20.dp) },
            )
            MetroDivider()
            MetroDropdownMenuItem(
                text = "Add to library",
                onClick = { expanded = false },
                leading = { MetroIcon(Icons.Filled.Star, null, tint = colors.onSurface, sizeDp = 20.dp) },
            )
            MetroDivider()
            MetroDropdownMenuItem(
                text = "Share",
                enabled = false,
                onClick = { expanded = false },
            )
        }
    }
}

@Composable
private fun DialogRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val typography = LocalMetroTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetroIcon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            sizeDp = 26.dp,
        )
        Spacer(Modifier.width(16.dp))
        Column {
            MetroText(text = title, color = LocalMetroColors.current.onSurface, style = typography.body)
            MetroText(text = subtitle, color = LocalMetroColors.current.onSurfaceVariant, style = typography.caption)
        }
    }
}
@Composable
private fun DetailHeader(detail: DemoDetail) {
    val colors = LocalMetroColors.current
    val typography = LocalMetroTypography.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(detail.cover)
        )
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            MetroText(text = detail.title, color = colors.onBackground, style = typography.pageHeading.copy(fontSize = 26.sp))
            Spacer(Modifier.height(6.dp))
            MetroText(text = detail.subtitle, color = colors.primary, style = typography.caption)
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun LazyListScope.detailBody() {
    items(20) { i ->
        MetroListRow(
            title = "Track ${i + 1}",
            subtitle = "3:${(15 + i * 7) % 60}",
            leading = {
                MetroText(
                    text = "%02d".format(i + 1),
                    modifier = Modifier.padding(start = 16.dp),
                )
            },
            trailing = {
                MetroIcon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    sizeDp = 20.dp,
                )
            },
            onClick = {},
        )
    }
}

@Composable
private fun LyricsDemo(onBack: () -> Unit) {
    val colors = LocalMetroColors.current
    val typography = LocalMetroTypography.current
    val lyrics = remember {
        listOf(
            MetroLyricLine(0, "Somewhere only we know"),
            MetroLyricLine(3000, "I walked across an empty land"),
            MetroLyricLine(6000, "I knew the pathway like the back of my hand"),
            MetroLyricLine(9500, "I felt the earth beneath my feet"),
            MetroLyricLine(12500, "Sat by the river and it made me complete"),
            MetroLyricLine(15500, "Oh simple thing, where have you gone?"),
            MetroLyricLine(18500, "I'm getting old and I need something to rely on"),
            MetroLyricLine(22500, "So tell me when you're gonna let me in"),
            MetroLyricLine(25500, "I'm getting tired and I need somewhere to begin"),
        )
    }
    var playing by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(playing) {
        while (playing) {
            delay(16)
            positionMs += 16L
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetroIconButton(onClick = onBack) {
                MetroIcon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = colors.onSurface,
                    sizeDp = 22.dp,
                )
            }
            MetroText(
                text = "Lyrics · timestamp-driven",
                color = colors.onSurface,
                style = typography.title,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        MetroLyricsPanel(
            lines = lyrics,
            currentPositionMillis = { positionMs },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            MetroButton(
                text = if (playing) "Pause" else "Play",
                onClick = { playing = !playing },
            )
            MetroButton(
                text = "Restart",
                onClick = {
                    positionMs = 0L
                    playing = false
                },
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurface,
            )
        }
    }
}

@Composable
private fun DebugPanel(insets: MetroInsets) {
    val stack = LocalMetroBottomStack.current
    val colors = LocalMetroColors.current
    val typography = LocalMetroTypography.current
    val mono = typography.caption.copy(fontFamily = FontFamily.Monospace)

    MetroSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = colors.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MetroText(text = "System insets", style = typography.caption)
            MetroText("  statusBar.top    = ${fmt(insets.statusBar.dp)}  (${"%.1f".format(insets.statusBar.px)} px)", style = mono)
            MetroText("  navigationBar    = ${fmt(insets.navigationBar.dp)}  (${"%.1f".format(insets.navigationBar.px)} px)", style = mono)
            MetroText("  displayCutout.T  = ${fmt(insets.displayCutout.top.dp)}", style = mono)
            MetroText("  ime.bottom       = ${fmt(insets.ime.dp)}", style = mono)
            Spacer(Modifier.height(6.dp))
            MetroText(text = "Bottom stack (total ${fmt(stack.totalHeightDp)})", style = typography.caption)
            stack.reservationsByKey.forEach { (k, v) ->
                MetroText("  $k  →  ${fmt(v)}", style = mono, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SimpleRow(index: Int) {
    MetroListRow(
        title = "Row $index",
        subtitle = "scroll to bottom; last row must clear both bars",
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun OverlayBar(label: String, tint: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(tint),
        contentAlignment = Alignment.Center,
    ) {
        MetroText(
            text = label,
            color = LocalMetroColors.current.onSurface,
            style = LocalMetroTypography.current.caption,
        )
    }
}

private fun fmt(dp: Dp): String = "${dp.value.toInt()}dp"
