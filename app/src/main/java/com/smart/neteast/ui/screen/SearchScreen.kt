package com.smart.neteast.ui.screen

import com.smart.neteast.ui.theme.LocalNcrustTypography

import com.smart.neteast.ui.theme.LocalNcrustColors
import com.smart.neteast.ui.theme.NcrustShape



import com.smart.neteast.ui.design.*
import androidx.compose.material.icons.rounded.*

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.neteast.library.LibraryManager
import com.smart.neteast.library.SearchHistoryManager
import com.smart.neteast.network.SongItem
import com.smart.neteast.network.model.AlbumItem
import com.smart.neteast.network.model.ArtistItem
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.ui.BottomOverlayInsetDp
import com.smart.neteast.ui.components.AlbumSearchItem
import com.smart.neteast.ui.components.ArtistSearchItem
import com.smart.neteast.ui.components.SongCard
import com.smart.neteast.ui.components.SongCardStyle
import com.smart.neteast.ui.components.SongMenuAction
import com.smart.neteast.ui.i18n.LocalStrings
import com.smart.neteast.ui.theme.desaturateColor
import com.smart.neteast.ui.theme.themeColorForIndex
import com.smart.neteast.ui.viewmodel.SearchViewModel
import com.smart.neteast.ui.components.SaltTabRow
import com.smart.neteast.ui.design.SaltIcon
import com.moriafly.salt.ui.Text as SaltText
import android.widget.Toast

enum class BatchQueueAction { PLAY_NOW, INSERT_NEXT, APPEND }

@Composable
fun SearchScreen(
    onSongClick: (SongItem) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onInsertNext: (SongItem) -> Unit = {},
    onAppendToQueue: (SongItem) -> Unit = {},
    onShowSongMenu: (SongItem, List<SongMenuAction>) -> Unit = { _, _ -> },
    onAlbumBatch: (albumId: Long, action: BatchQueueAction) -> Unit = { _, _ -> },
    onArtistBatch: (artistName: String, action: BatchQueueAction) -> Unit = { _, _ -> },
    themeIndex: Int = 0,
    showSearchInput: Boolean = true
) {
    val viewModel: SearchViewModel = viewModel()
    val query by viewModel.query.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentType by viewModel.currentType.collectAsState()
    val context = LocalContext.current
    val strings = LocalStrings.current
    val categories = listOf(strings.searchCategoryTracks, strings.searchCategoryAlbums, strings.searchCategoryArtists)

    val currentThemeColor = themeColorForIndex(themeIndex)
    val desaturatedFill = desaturateColor(
        currentThemeColor,
        darkTheme = LocalNcrustColors.current.background.luminance() < 0.5f
    )

    // 键盘治理: 点结果进详情/切 tab 后键盘不该还浮着
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun dismissKeyboard() {
        keyboard?.hide()
        focusManager.clearFocus()
    }
    // 切 tab(离开组合)强制收键盘
    DisposableEffect(Unit) {
        onDispose { keyboard?.hide() }
    }

    // Search history state — loaded from SharedPreferences, refreshed whenever query clears
    var songHistory by remember { mutableStateOf(SearchHistoryManager.getSongs(context)) }
    var albumHistory by remember { mutableStateOf(SearchHistoryManager.getAlbums(context)) }
    var artistHistory by remember { mutableStateOf(SearchHistoryManager.getArtists(context)) }

    fun refreshHistory() {
        songHistory = SearchHistoryManager.getSongs(context)
        albumHistory = SearchHistoryManager.getAlbums(context)
        artistHistory = SearchHistoryManager.getArtists(context)
    }

    LaunchedEffect(query) {
        if (query.isEmpty()) refreshHistory()
    }

    val showHistory = query.isEmpty() &&
        (songHistory.isNotEmpty() || albumHistory.isNotEmpty() || artistHistory.isNotEmpty())

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景由 MainScreen 外层 Box 统一填充，此处不重复画一层
        Column(modifier = Modifier.fillMaxSize()) {
            // Search input
            // BasicTextField 本身没有 M3 TextField 的隐式 56dp min-height 与内 padding，
            // 得手动在 decorationBox 里补齐——heightIn(min=56.dp) 保住触控区高度，内容
            // 纵向居中、左右 16dp 内边距对齐 M3 视觉。否则搜索框会塌成一条 20dp 高的
            // 细条，看着像被压扁的 Chip。
            if (showSearchInput) Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(NcrustShape.pill)
                    .background(desaturatedFill)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = { dismissKeyboard() }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    textStyle = TextStyle(color = LocalNcrustColors.current.onBackground, fontSize = 18.sp),
                    cursorBrush = SolidColor(LocalNcrustColors.current.onBackground),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) {
                                SaltText(
                                    text = strings.searchPlaceholder,
                                    color = LocalNcrustColors.current.onBackground.copy(alpha = 0.5f),
                                    style = TextStyle(fontSize = 18.sp),
                                )
                            }
                            innerTextField()
                            Row(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLoading) NcrustProgressIndicator(
                                    sizeDp = 24.dp,
                                    color = currentThemeColor,
                                )
                                if (query.isNotEmpty()) NcrustIconButton(onClick = { viewModel.clearQuery() }) {
                                    SaltIcon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = strings.clearSearchButton,
                                        tint = LocalNcrustColors.current.onBackground.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // 三态过渡：History（有历史时空 query）/ Results（输入非空）/ Empty（空 query 无历史）。
                // 用 Crossfade + NcrustMotion.CoverFade 消除清空搜索框时"结果列表 → 历史"的硬切。
                val searchContentState = when {
                    showHistory -> SearchContentState.History
                    query.isNotEmpty() -> SearchContentState.Results
                    else -> SearchContentState.Empty
                }
                // 宽屏搜索内容居中限宽(上限 760dp)，避免整行列表/历史横跨平板。
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                Crossfade(
                    targetState = searchContentState,
                    animationSpec = NcrustMotion.CoverFade,
                    modifier = Modifier.widthIn(max = 760.dp).fillMaxHeight(),
                    label = "SearchContentCrossfade"
                ) { state -> when (state) {
                    SearchContentState.History -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = BottomOverlayInsetDp),
                            flingBehavior = rememberNcrustFlingBehavior()
                        ) {
                    if (songHistory.isNotEmpty()) {
                        item {
                            SearchHistorySectionHeader(
                                title = strings.searchCategoryTracks,
                                onClear = {
                                    SearchHistoryManager.clearSection(context, SearchHistoryManager.TYPE_SONG)
                                    refreshHistory()
                                },
                                clearLabel = strings.searchHistoryClear
                            )
                        }
                        items(songHistory, key = { "s_${it.id}" }) { item ->
                            val song = item.toSongItem()
                            SearchHistoryItemCard(
                                item = item,
                                onClick = {
                                    onSongClick(song)
                                },
                                menuContent = { onDismiss ->
                                    NcrustDropdownMenuItem(
                                        text = strings.playButton,
                                        textColor = LocalNcrustColors.current.onBackground,
                                        onClick = { onDismiss(); onSongClick(song) },
                                    )
                                    NcrustDropdownMenuItem(
                                        text = strings.actionInsertNext,
                                        textColor = LocalNcrustColors.current.onBackground,
                                        onClick = { onDismiss(); onInsertNext(song) },
                                    )
                                    NcrustDropdownMenuItem(
                                        text = strings.actionAddToLibrary,
                                        textColor = LocalNcrustColors.current.onBackground,
                                        onClick = {
                                            onDismiss()
                                            LibraryManager.saveSong(context, song)
                                            Toast.makeText(context, strings.addedToLibrary, Toast.LENGTH_SHORT).show()
                                        },
                                    )
                                    NcrustDropdownMenuItem(
                                        text = strings.searchHistoryDelete,
                                        textColor = LocalNcrustColors.current.error.copy(alpha = 0.85f),
                                        onClick = {
                                            onDismiss()
                                            SearchHistoryManager.remove(context, SearchHistoryManager.TYPE_SONG, item.id)
                                            refreshHistory()
                                        },
                                    )
                                }
                            )
                        }
                    }

                    if (albumHistory.isNotEmpty()) {
                        item {
                            SearchHistorySectionHeader(
                                title = strings.searchCategoryAlbums,
                                onClear = {
                                    SearchHistoryManager.clearSection(context, SearchHistoryManager.TYPE_ALBUM)
                                    refreshHistory()
                                },
                                clearLabel = strings.searchHistoryClear
                            )
                        }
                        items(albumHistory, key = { "a_${it.id}" }) { item ->
                            SearchHistoryItemCard(
                                item = item,
                                onClick = { onAlbumClick(item.id) },
                                menuContent = { onDismiss ->
                                    NcrustDropdownMenuItem(
                                        text = strings.albumDetailTitle,
                                        textColor = LocalNcrustColors.current.onBackground,
                                        onClick = { onDismiss(); onAlbumClick(item.id) },
                                    )
                                    NcrustDropdownMenuItem(
                                        text = strings.searchHistoryDelete,
                                        textColor = LocalNcrustColors.current.error.copy(alpha = 0.85f),
                                        onClick = {
                                            onDismiss()
                                            SearchHistoryManager.remove(context, SearchHistoryManager.TYPE_ALBUM, item.id)
                                            refreshHistory()
                                        },
                                    )
                                }
                            )
                        }
                    }

                    if (artistHistory.isNotEmpty()) {
                        item {
                            SearchHistorySectionHeader(
                                title = strings.searchCategoryArtists,
                                onClear = {
                                    SearchHistoryManager.clearSection(context, SearchHistoryManager.TYPE_ARTIST)
                                    refreshHistory()
                                },
                                clearLabel = strings.searchHistoryClear
                            )
                        }
                        items(artistHistory, key = { "r_${it.id}" }) { item ->
                            SearchHistoryItemCard(
                                item = item,
                                onClick = { dismissKeyboard(); onArtistClick(item.id) },
                                menuContent = { onDismiss ->
                                    NcrustDropdownMenuItem(
                                        text = strings.artistDetailTitle,
                                        textColor = LocalNcrustColors.current.onBackground,
                                        onClick = { dismissKeyboard(); onDismiss(); onArtistClick(item.id) },
                                    )
                                    NcrustDropdownMenuItem(
                                        text = strings.searchHistoryDelete,
                                        textColor = LocalNcrustColors.current.error.copy(alpha = 0.85f),
                                        onClick = {
                                            onDismiss()
                                            SearchHistoryManager.remove(context, SearchHistoryManager.TYPE_ARTIST, item.id)
                                            refreshHistory()
                                        },
                                    )
                                }
                            )
                        }
                    }

                        }
                    }
                    SearchContentState.Results -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SaltTabRow(
                                items = categories,
                                selectedTabIndex = when (currentType) {
                                    1 -> 0
                                    10 -> 1
                                    100 -> 2
                                    else -> 0
                                },
                                onTabSelected = { index ->
                                    viewModel.onTypeChanged(
                                        when (index) {
                                            0 -> 1
                                            1 -> 10
                                            2 -> 100
                                            else -> 1
                                        }
                                    )
                                },
                            )

                error?.let {
                    SaltText(
                        text = strings.loadFailed(it),
                        color = LocalNcrustColors.current.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                when (currentType) {
                    1 -> {
                        if (songs.isEmpty() && !isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                SaltText(
                                    text = strings.searchSongsEmpty,
                                    color = LocalNcrustColors.current.onSurfaceVariant,
                                    style = TextStyle(fontSize = 16.sp),
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = BottomOverlayInsetDp),
                                flingBehavior = rememberNcrustFlingBehavior()
                            ) {
                                items(songs, key = { it.id }) { item ->
                                    SongCard(
                                        song = item,
                                        style = SongCardStyle.LIST,
                                        coverSize = 72.dp,
                                        onClick = {
                                            SearchHistoryManager.addSong(context, item)
                                            dismissKeyboard(); onSongClick(item)
                                        },
                                        onShowMenu = {
                                            onShowSongMenu(item, listOf(
                                                SongMenuAction(Icons.Rounded.LibraryAdd, strings.actionAddToLibrary) {
                                                    SearchHistoryManager.addSong(context, item)
                                                    LibraryManager.saveSong(context, item)
                                                    Toast.makeText(context, strings.addedToLibrary, Toast.LENGTH_SHORT).show()
                                                },
                                                SongMenuAction(Icons.Rounded.PlaylistPlay, strings.actionInsertNext) {
                                                    SearchHistoryManager.addSong(context, item)
                                                    onInsertNext(item)
                                                },
                                                SongMenuAction(Icons.Rounded.PlaylistAdd, strings.actionAppendToQueue) {
                                                    SearchHistoryManager.addSong(context, item)
                                                    onAppendToQueue(item)
                                                }
                                            ))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    10 -> {
                        if (albums.isEmpty() && !isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                SaltText(
                                    text = strings.searchAlbumsEmpty,
                                    color = LocalNcrustColors.current.onSurfaceVariant,
                                    style = TextStyle(fontSize = 16.sp),
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = BottomOverlayInsetDp),
                                flingBehavior = rememberNcrustFlingBehavior()
                            ) {
                                items(albums, key = { it.id }) { album ->
                                    AlbumSearchItem(
                                        album = album,
                                        onClick = {
                                            SearchHistoryManager.addAlbum(context, album)
                                            dismissKeyboard(); onAlbumClick(album.id)
                                        },
                                        menuContent = { onDismiss ->
                                            NcrustDropdownMenuItem(
                                                text = strings.playAllButton,
                                                onClick = {
                                                    SearchHistoryManager.addAlbum(context, album)
                                                    onAlbumBatch(album.id, BatchQueueAction.PLAY_NOW)
                                                    onDismiss()
                                                }
                                            )
                                            NcrustDropdownMenuItem(
                                                text = strings.actionInsertNext,
                                                onClick = {
                                                    SearchHistoryManager.addAlbum(context, album)
                                                    onAlbumBatch(album.id, BatchQueueAction.INSERT_NEXT)
                                                    onDismiss()
                                                }
                                            )
                                            NcrustDropdownMenuItem(
                                                text = strings.actionAppendToQueue,
                                                onClick = {
                                                    SearchHistoryManager.addAlbum(context, album)
                                                    onAlbumBatch(album.id, BatchQueueAction.APPEND)
                                                    onDismiss()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    100 -> {
                        if (artists.isEmpty() && !isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                SaltText(
                                    text = strings.searchArtistsEmpty,
                                    color = LocalNcrustColors.current.onSurfaceVariant,
                                    style = TextStyle(fontSize = 16.sp),
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = BottomOverlayInsetDp),
                                flingBehavior = rememberNcrustFlingBehavior()
                            ) {
                                items(artists, key = { it.id }) { artist ->
                                    ArtistSearchItem(
                                        artist = artist,
                                        onClick = {
                                            SearchHistoryManager.addArtist(context, artist)
                                            dismissKeyboard(); onArtistClick(artist.id)
                                        },
                                        menuContent = { onDismiss ->
                                            NcrustDropdownMenuItem(
                                                text = strings.playAllButton,
                                                onClick = {
                                                    SearchHistoryManager.addArtist(context, artist)
                                                    onArtistBatch(artist.name, BatchQueueAction.PLAY_NOW)
                                                    onDismiss()
                                                }
                                            )
                                            NcrustDropdownMenuItem(
                                                text = strings.actionInsertNext,
                                                onClick = {
                                                    SearchHistoryManager.addArtist(context, artist)
                                                    onArtistBatch(artist.name, BatchQueueAction.INSERT_NEXT)
                                                    onDismiss()
                                                }
                                            )
                                            NcrustDropdownMenuItem(
                                                text = strings.actionAppendToQueue,
                                                onClick = {
                                                    SearchHistoryManager.addArtist(context, artist)
                                                    onArtistBatch(artist.name, BatchQueueAction.APPEND)
                                                    onDismiss()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                        }
                    }
                    SearchContentState.Empty -> {
                        // Query 空且无历史：只留搜索框在上面，下方空白。
                        Spacer(Modifier.fillMaxSize())
                    }
                } }
                }
        }
    }
}

private enum class SearchContentState { History, Results, Empty }

@Composable
private fun SearchHistorySectionHeader(title: String, clearLabel: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SaltText(
            text = title,
            color = LocalNcrustColors.current.onBackground,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
        )
        // TextButton 替换:直角矩形 tap target,无背景 -- 与无边框交互风格
        // 原则契合。padding 补齐视觉高度。
        Box(
            modifier = Modifier
                .clickable(onClick = onClear)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            SaltText(
                text = clearLabel,
                color = LocalNcrustColors.current.onSurfaceVariant,
                style = TextStyle(fontSize = 12.sp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchHistoryItemCard(
    item: SearchHistoryManager.HistoryItem,
    onClick: () -> Unit,
    menuContent: @Composable ColumnScope.(onDismiss: () -> Unit) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    role = androidx.compose.ui.semantics.Role.Button,
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = CoverUrls.small(item.coverUrl),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(NcrustShape.thumbnail),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                SaltText(
                    text = item.title,
                    color = LocalNcrustColors.current.onBackground,
                    style = LocalNcrustTypography.current.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!item.subtitle.isNullOrEmpty()) {
                    SaltText(
                        text = item.subtitle,
                        color = LocalNcrustColors.current.onSurfaceVariant,
                        style = LocalNcrustTypography.current.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        NcrustDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = LocalNcrustColors.current.surface,
            cornerRadiusDp = 18.dp,
        ) {
            menuContent { showMenu = false }
        }
    }
}

private fun SearchHistoryManager.HistoryItem.toSongItem(): SongItem = SongItem(
    id = id,
    name = title,
    artists = subtitle?.let { listOf(ArtistItem(name = it)) },
    album = AlbumItem(id = null, name = null, picUrl = coverUrl),
    duration = null
)

@Composable
fun SongSearchItem(
    song: SongItem,
    onPlay: () -> Unit,
    onAddToLibrary: () -> Unit,
    onInsertNext: () -> Unit = {},
    onAppendToQueue: () -> Unit = {}
) {
    val strings = LocalStrings.current
    SongCard(
        song = song,
        style = SongCardStyle.LIST,
        coverSize = 56.dp,
        onClick = onPlay,
        actions = {
            NcrustIconButton(onClick = onAddToLibrary) {
                SaltIcon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = strings.actionAddToLibrary,
                    tint = LocalNcrustColors.current.onBackground,
                )
            }
            NcrustIconButton(onClick = onInsertNext) {
                SaltIcon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = strings.actionInsertNext,
                    tint = LocalNcrustColors.current.onBackground,
                )
            }
            NcrustIconButton(onClick = onAppendToQueue) {
                SaltIcon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = strings.actionAddToPlaylist,
                    tint = LocalNcrustColors.current.onBackground,
                )
            }
        }
    )
}
