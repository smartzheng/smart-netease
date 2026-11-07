package com.smart.neteast.ui.screen

import android.graphics.drawable.BitmapDrawable
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.smart.neteast.account.SessionMonitor
import com.smart.neteast.account.SessionState
import com.smart.neteast.cache.ContentCache
import com.smart.neteast.network.PlaylistApi
import com.smart.neteast.network.SongItem
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.ui.BottomOverlayInsetDp
import com.smart.neteast.ui.ResponsiveContent
import com.smart.neteast.ui.components.SessionExpiredBanner
import com.smart.neteast.library.LibraryManager
import io.github.takahashirinta.kanesumi.anim.sokuou.SokuouTweens
import io.github.takahashirinta.kanesumi.anim.sokuou.rememberMetroFlingBehavior
import io.github.takahashirinta.kanesumi.controls.MetroIconButton
import io.github.takahashirinta.kanesumi.controls.MetroProgressIndicator
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import com.smart.neteast.ui.design.SaltIcon
import com.moriafly.salt.ui.Text as SaltText
import com.smart.neteast.ui.components.PlayAllButton
import com.smart.neteast.ui.components.SongCard
import com.smart.neteast.ui.components.SongCardStyle
import com.smart.neteast.ui.components.SongMenuAction
import com.smart.neteast.ui.components.ScreenTitle
import com.smart.neteast.ui.i18n.LocalStrings
import com.smart.neteast.ui.viewmodel.SearchViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.activity.compose.BackHandler

// 新歌速递容量：一次装够，不做分页。首页只是一瞥的展示位，
// 不是深度浏览入口——用户想探索会去搜索/歌单。
private const val NEW_SONGS_LIMIT = 20

@Composable
fun HomeScreen(
    searchContent: @Composable () -> Unit = {},
    onSongClick: (SongItem) -> Unit,
    onPlaylistClick: (Long) -> Unit = {},
    onPlayPlaylist: (Long) -> Unit = {},
    onPlayDailyAll: ((List<SongItem>) -> Unit)? = null,
    onSongInsertNext: (SongItem) -> Unit = {},
    onSongAppendToQueue: (SongItem) -> Unit = {},
    onShowSongMenu: (SongItem, List<SongMenuAction>) -> Unit = { _, _ -> },
    onPlayFm: (() -> Unit)? = null,
    // 会话失效横幅点击 → 走现有登录流程（窄屏网页登录 / 宽屏扫码）。
    onRelogin: () -> Unit = {}
) {
    val strings = LocalStrings.current
    // 会话状态：Expired 时顶部一条失效横幅；非 Valid 时每日推荐加一行「登录后为你推荐」
    // （登录失效后接口返回的是非个性化的通用列表，不能冒充个性化推荐）。
    val sessionState by SessionMonitor.state.collectAsState()
    val sessionExpired = sessionState is SessionState.Expired
    val sessionValid = sessionState is SessionState.Valid
    // 初始 state 从 ContentCache 读取。有缓存则立即渲染，无需 spinner。
    // 后台仍会刷新——请求返回后写回缓存 + 更新 state；LazyColumn 通过 key diff 平滑替换。
    var dailySongs by remember { mutableStateOf(ContentCache.homeDailySongs ?: emptyList()) }
    var playlists by remember { mutableStateOf(ContentCache.homeRecommendPlaylists ?: emptyList()) }
    val newSongs = remember {
        mutableStateListOf<SongItem>().apply { ContentCache.homeNewSongs?.let { addAll(it) } }
    }
    // 冷启动（三块数据都空）才显示全屏 loader；有任一缓存则跳过。
    var isLoading by remember {
        mutableStateOf(dailySongs.isEmpty() && playlists.isEmpty() && newSongs.isEmpty())
    }
    var error by remember { mutableStateOf<String?>(null) }
    val gridState = rememberLazyGridState()
    // 宽屏：新歌从"整行列表"切到"自适应栅格单元"（Apple Music 式）；手机保持整行列表。
    val isWide = LocalConfiguration.current.screenWidthDp >= 600
    val coroutineScope = rememberCoroutineScope()
    val searchViewModel: SearchViewModel = viewModel()
    val searchQuery by searchViewModel.query.collectAsState()
    val searchFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isSearchActive by remember { mutableStateOf(false) }
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
            keyboard?.show()
        }
    }
    BackHandler(enabled = isSearchActive) {
        keyboard?.hide()
        focusManager.clearFocus()
        isSearchActive = false
    }

    // 私人 FM 电台卡需要登录用户资料: 昵称(卡标题"xx的电台") + 头像(取强调色做封面)。
    // 电台卡**常驻**——资料拿不到也照常显示(标题回退通用文案), 不再因此整卡消失。
    var fmProfile by remember { mutableStateOf(ContentCache.userProfile) }
    // 头像主色调(Palette), 取不到就回退中性底色(不借用 Ncrust 主题色)
    var fmAccent by remember { mutableStateOf<Color?>(null) }
    val fmContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val profile = ContentCache.userProfile
            ?: runCatching { PlaylistApi.getUserProfile() }.getOrNull()
        if (profile != null && profile.userId > 0) {
            fmProfile = profile
            ContentCache.userProfile = profile
            val accent = runCatching { extractAvatarAccent(fmContext, profile.avatarUrl) }.getOrNull()
            if (accent != null) fmAccent = accent
        }
    }

    fun loadDailySongs() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val list = PlaylistApi.getDailyRecommendSongs()
                withContext(Dispatchers.Main) {
                    dailySongs = list
                    ContentCache.homeDailySongs = list
                }
            } catch (e: Exception) {
                android.util.Log.e("DailySongs", "Error", e)
            }
        }
    }

    fun loadPlaylists() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val list = PlaylistApi.getRecommendPlaylists()
                withContext(Dispatchers.Main) {
                    playlists = list
                    ContentCache.homeRecommendPlaylists = list
                }
            } catch (_: Exception) { }
        }
    }

    fun loadNewSongs() {
        coroutineScope.launch(Dispatchers.IO) {
            if (newSongs.isEmpty()) isLoading = true
            error = null
            try {
                val list = PlaylistApi.getTopSongs(limit = NEW_SONGS_LIMIT, offset = 0)
                withContext(Dispatchers.Main) {
                    newSongs.clear()
                    newSongs.addAll(list)
                    ContentCache.homeNewSongs = list.toList()
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error = strings.loadFailed(e.message)
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // AppWarmup 刚预取过同样三个接口时直接复用缓存，避免冷启动重复网络/耗电。
        if (ContentCache.isHomeFresh()) return@LaunchedEffect
        loadDailySongs()
        loadPlaylists()
        loadNewSongs()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    fun songMenu(song: SongItem): List<SongMenuAction> = listOf(
        SongMenuAction(Icons.Rounded.LibraryAdd, strings.actionAddToLibrary) {
            LibraryManager.saveSong(context, song)
            Toast.makeText(context, strings.addedToLibrary, Toast.LENGTH_SHORT).show()
        },
        SongMenuAction(Icons.Rounded.PlaylistPlay, strings.actionInsertNext) { onSongInsertNext(song) },
        SongMenuAction(Icons.Rounded.PlaylistAdd, strings.actionAppendToQueue) { onSongAppendToQueue(song) }
    )

    AnimatedContent(
        targetState = isSearchActive,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
        modifier = Modifier.fillMaxSize(),
        label = "HomeSearchTransition"
    ) { searching ->
        if (searching) {
            ResponsiveContent {
                Column(Modifier.fillMaxSize()) {
                    HomeSearchHeader(
                        query = searchQuery,
                        active = true,
                        focusRequester = searchFocusRequester,
                        horizontalInset = 16.dp,
                        onActivate = { isSearchActive = true },
                        onQueryChange = searchViewModel::onQueryChanged,
                        onClear = searchViewModel::clearQuery,
                        onClose = { isSearchActive = false; focusManager.clearFocus(); keyboard?.hide() },
                        onSearch = { keyboard?.hide() }
                    )
                    Box(Modifier.weight(1f)) { searchContent() }
                }
            }
        } else Crossfade(
            targetState = isLoading,
            animationSpec = SokuouTweens.CoverFade,
            modifier = Modifier.fillMaxSize(),
            label = "HomeContentCrossfade"
        ) { loading ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MetroProgressIndicator(color = LocalMetroColors.current.primary)
            }
        } else {
            ResponsiveContent {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 148.dp),
                    // 背景由 MainScreen 外层 Box 统一填充，子屏不重复画一层（消除 overdraw）
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = BottomOverlayInsetDp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    flingBehavior = rememberMetroFlingBehavior()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeSearchHeader(
                            query = searchQuery,
                            active = false,
                            focusRequester = searchFocusRequester,
                            horizontalInset = 0.dp,
                            onActivate = { isSearchActive = true },
                            onQueryChange = searchViewModel::onQueryChanged,
                            onClear = searchViewModel::clearQuery,
                            onClose = { isSearchActive = false },
                            onSearch = { keyboard?.hide() }
                        )
                    }

                    // 登录失效横幅：放在页头之下、内容之上（列表第一个内容项），点击进登录流程。
                    if (sessionExpired) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SessionExpiredBanner(onRelogin = onRelogin)
                        }
                    }

                    // 每日推荐：横滑大 tile；点击整块进入播放。
                    if (dailySongs.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader(
                                title = strings.dailySongsTitle,
                                onPlayAll = { onPlayDailyAll?.invoke(dailySongs) },
                                actions = {
                                    // 手动刷新每日推荐(#26):重新拉取并 diff 平滑替换
                                    MetroIconButton(onClick = { loadDailySongs() }) {
                                        SaltIcon(
                                            Icons.Rounded.Refresh,
                                            contentDescription = strings.refreshLabel,
                                            tint = LocalMetroColors.current.onSurfaceVariant,
                                            sizeDp = 24.dp
                                        )
                                    }
                                }
                            )
                        }
                        // 非 Valid（含未登录/已失效）时说明这不是个性化推荐。
                        if (!sessionValid) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SaltText(
                                    strings.dailySongsLoginHint,
                                    color = LocalMetroColors.current.onSurfaceVariant,
                                    style = LocalMetroTypography.current.bodySmall,
                                )
                            }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                flingBehavior = rememberMetroFlingBehavior()
                            ) {
                                items(dailySongs.take(12), key = { it.id }) { song ->
                                    DailySongTile(
                                        song = song,
                                        onClick = { onSongClick(song) },
                                        onLongClick = { onShowSongMenu(song, songMenu(song)) }
                                    )
                                }
                            }
                        }
                    }

                    // 推荐歌单：横滑大 tile。私人 FM 电台卡**常驻首位**——
                    // 不再依赖推荐歌单是否拉到、也不依赖用户资料是否拿到。
                    if (onPlayFm != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader(title = strings.recommendPlaylistTitle)
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                flingBehavior = rememberMetroFlingBehavior()
                            ) {
                                item(key = "fm") {
                                    FmRadioTile(
                                        title = fmProfile?.nickname?.takeIf { it.isNotEmpty() }
                                            ?.let { strings.fmRadioTitle(it) }
                                            ?: strings.fmRadioTitleGeneric,
                                        accent = fmAccent ?: LocalMetroColors.current.surfaceVariant,
                                        subtitle = strings.fmRadioSubtitle,
                                        onClick = { onPlayFm() }
                                    )
                                }
                                items(playlists, key = { it.id }) { pl ->
                                    PlaylistTile(
                                        playlist = pl,
                                        onClick = { onPlaylistClick(pl.id) },
                                        onPlayAll = { onPlayPlaylist(pl.id) }
                                    )
                                }
                            }
                        }
                    }

                    // 新歌：宽屏自适应栅格（多列方封面），手机保持整行列表。
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = strings.newSongsTitle)
                    }
                    items(
                        items = newSongs,
                        key = { it.id },
                        span = { if (isWide) GridItemSpan(1) else GridItemSpan(maxLineSpan) }
                    ) { song ->
                        if (isWide) {
                            SongGridTile(
                                song = song,
                                onClick = { onSongClick(song) },
                                onLongClick = { onShowSongMenu(song, songMenu(song)) }
                            )
                        } else {
                            SongCard(
                                song = song,
                                style = SongCardStyle.LIST,
                                contentHorizontalPadding = 0.dp,
                                contentVerticalPadding = 0.dp,
                                onClick = { onSongClick(song) },
                                onShowMenu = { onShowSongMenu(song, songMenu(song)) }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun HomeSearchHeader(
    query: String,
    active: Boolean,
    focusRequester: FocusRequester,
    horizontalInset: androidx.compose.ui.unit.Dp,
    onActivate: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onSearch: () -> Unit
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = horizontalInset, end = horizontalInset, top = 16.dp, bottom = 8.dp)
    ) {
        ScreenTitle(strings.tabHome)
        Spacer(Modifier.height(8.dp))
        AnimatedContent(
            targetState = active,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "HomeSearchFieldTransition"
        ) { expanded ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(LocalMetroColors.current.surfaceVariant)
                    .then(if (expanded) Modifier else Modifier.clickable(onClick = onActivate))
                    .padding(horizontal = if (expanded) 8.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (expanded) {
                    MetroIconButton(onClick = onClose) {
                        SaltIcon(Icons.Rounded.ArrowBack, contentDescription = strings.tabHome,
                            tint = LocalMetroColors.current.onSurfaceVariant, sizeDp = 22.dp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = TextStyle(color = LocalMetroColors.current.onBackground,
                            fontSize = 16.sp, lineHeight = 24.sp),
                        cursorBrush = SolidColor(LocalMetroColors.current.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty()) SaltText(
                                    strings.searchPlaceholder,
                                    color = LocalMetroColors.current.onSurfaceVariant,
                                    style = LocalMetroTypography.current.bodyMedium
                                )
                                innerTextField()
                            }
                        }
                    )
                    if (query.isNotEmpty()) MetroIconButton(onClick = onClear) {
                    SaltIcon(Icons.Rounded.Clear, contentDescription = strings.clearSearchButton,
                            tint = LocalMetroColors.current.onSurfaceVariant, sizeDp = 20.dp)
                    }
                } else {
                    SaltIcon(Icons.Rounded.Search, contentDescription = null,
                        tint = LocalMetroColors.current.onSurfaceVariant, sizeDp = 22.dp)
                    Spacer(Modifier.width(12.dp))
                    SaltText(strings.searchPlaceholder, color = LocalMetroColors.current.onSurfaceVariant,
                        style = LocalMetroTypography.current.bodyMedium)
                }
            }
        }
    }
}

/** 分区标题：中字号 Regular，左对齐 16dp；右侧可选"播放全部"按钮 + 自定义动作。 */
@Composable
private fun SectionHeader(
    title: String,
    onPlayAll: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SaltText(
            title,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.title.copy(
                fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
        )
        actions()
        if (onPlayAll != null) {
            MetroIconButton(onClick = onPlayAll) {
                SaltIcon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = LocalStrings.current.playAllButton,
                    tint = LocalMetroColors.current.primary,
                    sizeDp = 24.dp,
                )
            }
        }
    }
}

/** 每日推荐大 tile：160dp 方封面，下方歌名 + 歌手。 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DailySongTile(song: SongItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .width(160.dp)
            .combinedClickableFallback(onClick, onLongClick)
    ) {
        AsyncImage(
            model = CoverUrls.small(song.album?.picUrl),
            contentDescription = strings.coverDesc,
            placeholder = ColorPainter(LocalMetroColors.current.surfaceVariant),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(4.dp))
        SaltText(
            song.name,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.caption.copy(
                fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SaltText(
            song.artists?.joinToString("/") { it.name } ?: strings.unknownArtist,
            color = LocalMetroColors.current.onSurfaceVariant,
            style = LocalMetroTypography.current.label.copy(fontSize = 12.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/**
 * 自适应栅格里的歌曲 tile：方封面 + 歌名 + 歌手，宽度随单元格（fillMaxWidth），
 * 区别于固定 160dp 的横滑 [DailySongTile]。宽屏新歌用它铺多列。
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SongGridTile(song: SongItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickableFallback(onClick, onLongClick)
    ) {
        AsyncImage(
            model = CoverUrls.small(song.album?.picUrl),
            contentDescription = strings.coverDesc,
            placeholder = ColorPainter(LocalMetroColors.current.surfaceVariant),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(4.dp))
        SaltText(
            song.name,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.caption.copy(
                fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SaltText(
            song.artists?.joinToString("/") { it.name } ?: strings.unknownArtist,
            color = LocalMetroColors.current.onSurfaceVariant,
            style = LocalMetroTypography.current.label.copy(fontSize = 12.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/** 推荐歌单大 tile：160dp 方封面 + 圆播放按钮。 */
@Composable
private fun PlaylistTile(playlist: PlaylistApi.PlaylistCard, onClick: () -> Unit, onPlayAll: () -> Unit) {    val strings = LocalStrings.current
    Column(modifier = Modifier.width(160.dp).clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp))) {
            AsyncImage(
                model = CoverUrls.small(playlist.coverUrl),
                contentDescription = null,
                placeholder = ColorPainter(LocalMetroColors.current.surfaceVariant),
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            PlayAllButton(
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                size = 34.dp,
                onClick = onPlayAll
            )
        }
        Spacer(Modifier.height(4.dp))
        SaltText(
            playlist.name,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.caption.copy(
                fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SaltText(
            strings.trackCountSongs(playlist.trackCount),
            color = LocalMetroColors.current.onSurfaceVariant,
            style = LocalMetroTypography.current.label.copy(fontSize = 12.sp),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/**
 * 私人 FM 电台大 tile：形制同 PlaylistTile，封面为 Metro 风格图形——
 * 整块用**用户头像提取的强调色**铺底，中央是一枚**对称音频波形**记号
 * (以中线为轴上下等幅的方端竖条，包络中间高两侧收)，无文字、无圆角；
 * 记号颜色按底色明度取黑/白以保证对比。
 */
@Composable
private fun FmRadioTile(
    title: String,
    accent: Color,
    subtitle: String,
    onClick: () -> Unit
) {
    // 波形记号颜色只看与**底色 accent** 的对比，不能跟随主题明暗：
    // 浅色主题下 onBackground 是近黑，压在深色头像强调色上会看不见。
    // 深色主题用白、浅色主题用米色(主题背景色)。
    val isLightTheme = LocalMetroColors.current.background.luminance() > 0.5f
    val onAccent = when {
        accent.luminance() > 0.5f -> Color.Black
        isLightTheme -> LocalMetroColors.current.background
        else -> Color.White
    }
    Column(modifier = Modifier.width(160.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(accent)
        ) {
            // 对称音频波形：5 根方端竖条，包络 0.5→1→0.5，中线上下等幅。
            // 方端(非圆角)贴合 Kanesumi；包络收口让记号有"声音起伏"而不呆板。
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.45f
                val envelope = floatArrayOf(0.5f, 0.82f, 1f, 0.82f, 0.5f)
                val barW = w * 0.085f
                val gap = w * 0.055f
                val maxHalf = h * 0.22f
                val totalW = envelope.size * barW + (envelope.size - 1) * gap
                var x = cx - totalW / 2f
                envelope.forEach { f ->
                    val half = maxHalf * f
                    drawRect(
                        color = onAccent,
                        topLeft = Offset(x, cy - half),
                        size = Size(barW, half * 2f)
                    )
                    x += barW + gap
                }
            }
            PlayAllButton(
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                size = 34.dp,
                onClick = onClick
            )
        }
        Spacer(Modifier.height(4.dp))
        SaltText(
            title,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.caption.copy(
                fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SaltText(
            subtitle,
            color = LocalMetroColors.current.onSurfaceVariant,
            style = LocalMetroTypography.current.label.copy(fontSize = 12.sp),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/**
 * 从头像位图提取强调色。Coil 在 API 26+ 默认返回硬件位图，Palette 无法读取，
 * 故显式 allowHardware(false)；取色优先 vibrant，再 dominant，最后 muted。
 */
private suspend fun extractAvatarAccent(context: Context, avatarUrl: String): Color? {
    if (avatarUrl.isBlank()) return null
    val loader = Coil.imageLoader(context)
    val result = loader.execute(
        ImageRequest.Builder(context)
            .data(CoverUrls.large(avatarUrl))
            .size(128, 128)
            .allowHardware(false)
            .build()
    )
    val bitmap = (result as? SuccessResult)?.drawable
        ?.let { (it as BitmapDrawable).bitmap }
        ?: return null
    val palette = Palette.from(bitmap).generate()
    val argb = palette.getVibrantColor(0).takeIf { it != 0 }
        ?: palette.getDominantColor(0).takeIf { it != 0 }
        ?: palette.getMutedColor(0).takeIf { it != 0 }
        ?: return null
    return Color(argb)
}

/** 点击 + 长按合并到一个 modifier，避免每个 tile 内部重复样板。 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableFallback(onClick: () -> Unit, onLongClick: () -> Unit): Modifier =
    this.combinedClickable(onClick = onClick, onLongClick = onLongClick)
