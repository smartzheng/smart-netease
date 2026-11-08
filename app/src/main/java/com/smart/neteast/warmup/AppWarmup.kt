package com.smart.neteast.warmup

import android.content.Context
import coil.Coil
import coil.request.ImageRequest
import com.smart.neteast.auth.CookieManager
import com.smart.neteast.cache.ContentCache
import com.smart.neteast.library.LibraryManager
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.network.PlaylistApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 冷启动一次性预热：首页数据进入启动关键路径，封面缓存留在后台低优先级处理。
 *
 * 目的：先以本地收藏数据和首页请求尽快进入界面；图片预取与云端同步不得延长 splash。
 *
 * ready 由 splash 订阅：为 true 时才允许淡出（配合 splash 侧的最短驻留时间）。
 * 总兜底 [TIMEOUT_MS]：无论网络多慢，超时后强制置 ready，防止启动被卡死。
 */
object AppWarmup {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var started = false

    private const val TIMEOUT_MS = 3_000L
    // Home 里 tile 显示尺寸约 160dp，取 320px 覆盖 xxhdpi 单张封面
    private const val COVER_PX = 320
    // 首页每类内容预取多少张封面——后台覆盖首屏可见部分即可
    private const val PREFETCH_PER_SECTION = 6
    private const val PREFETCH_LIBRARY_SONGS = 16
    private const val PREFETCH_LIBRARY_ALBUMS = 8

    // 全部 SharedPreferences 文件名——IO 线程一次性触碰，让主线程 getSharedPreferences 命中缓存。
    // 与各 Manager 里的 PREFS_NAME 常量保持同步。
    private val PREFS_FILES = arrayOf(
        "ncrust_prefs",           // CookieManager
        "ncrust_settings",        // ThemeManager / LanguageManager / PlayerViewModel
        "ncrust_library",         // LibraryManager
        "ncrust_playback_state",  // PlaybackStateManager
        "ncrust_lyrics_cache",    // LyricsCache
        "search_history"          // SearchHistoryManager
    )

    fun start(context: Context) {
        if (started) return
        started = true
        val app = context.applicationContext

        // 阶段零：立即触碰所有 SharedPreferences 文件，把 XML 解析从主线程拉走。
        // SharedPreferences 按 name 单例，首次 getSharedPreferences 会同步读磁盘+解析 XML
        // （eMMC 上单文件 30~100ms）。这里 IO 线程先跑一遍，后续主线程 get* 就都是内存命中。
        // 独立 launch 不进 withTimeout：这些操作本身很快，即使个别慢也不该阻塞 ready 判定。
        scope.launch {
            for (name in PREFS_FILES) {
                runCatching { app.getSharedPreferences(name, Context.MODE_PRIVATE) }
            }
        }

        // 预解析收藏库（IO）：避免首次在组合期主线程 getSavedSongs/isSongSaved 卡顿。
        LibraryManager.preload(app)

        scope.launch {
            val homeReady = withTimeoutOrNull(TIMEOUT_MS) {
                // 阶段一：三条 Home 请求并发写入 ContentCache
                coroutineScope {
                    val dailyDeferred = async {
                        runCatching { PlaylistApi.getDailyRecommendSongs() }.getOrNull()
                    }
                    val plsDeferred = async {
                        runCatching { PlaylistApi.getRecommendPlaylists() }.getOrNull()
                    }
                    val topDeferred = async {
                        // limit 与 HomeScreen NEW_SONGS_LIMIT 对齐——warmup 一次装够，
                        // 不留"缓存 10 条 → 刷新变 20 条"的可见 diff
                        runCatching { PlaylistApi.getTopSongs(limit = 20, offset = 0) }.getOrNull()
                    }
                    dailyDeferred.await()?.let { ContentCache.homeDailySongs = it }
                    plsDeferred.await()?.let { ContentCache.homeRecommendPlaylists = it }
                    topDeferred.await()?.let { ContentCache.homeNewSongs = it }
                }
                // 标记首页数据已预热：HomeScreen 进屏时据此跳过重复请求。
                ContentCache.markHomeWarmed()
                true
            } ?: false

            // 先让主页面绘制，再在后台预取首页与本地收藏封面；图片网络/解码不挡启动。
            _ready.value = true
            scope.launch {
                delay(350L)
                if (homeReady) prefetchHomeCovers(app)
                prefetchLibraryCovers(app)
            }

            // 阶段二：收藏库刷新(已登录时)。只喂收藏页缓存, 不进 ready 关键路径——
            // 它要 3 次额外网络往返, 挂在关键路径上会拖长启动。
            if (CookieManager.hasCookie(app)) {
                scope.launch { runCatching { LibraryManager.refreshFromCloud(app) } }
            }
        }
    }

    private suspend fun prefetchHomeCovers(app: Context) {
        val sourceUrls = buildList {
            ContentCache.homeDailySongs?.take(PREFETCH_PER_SECTION)?.forEach {
                addIfPresent(it.album?.picUrl)
            }
            ContentCache.homeRecommendPlaylists?.take(PREFETCH_PER_SECTION)?.forEach {
                addIfPresent(it.coverUrl)
            }
            ContentCache.homeNewSongs?.take(PREFETCH_PER_SECTION)?.forEach {
                addIfPresent(it.album?.picUrl)
            }
        }
        prefetchCovers(app, sourceUrls, COVER_PX)
    }

    private suspend fun prefetchLibraryCovers(app: Context) {
        val sourceUrls = buildList {
            LibraryManager.getSavedSongs(app).take(PREFETCH_LIBRARY_SONGS).forEach {
                addIfPresent(it.album?.picUrl)
            }
            LibraryManager.getSavedAlbums(app).take(PREFETCH_LIBRARY_ALBUMS).forEach {
                addIfPresent(it.picUrl)
            }
        }
        prefetchCovers(app, sourceUrls, COVER_PX)
    }

    private suspend fun prefetchCovers(app: Context, sourceUrls: List<String>, sizePx: Int) {
        val urls = sourceUrls.distinct().mapNotNull { CoverUrls.small(it) }
        if (urls.isEmpty()) return
        val loader = Coil.imageLoader(app)
        coroutineScope {
            urls.chunked(4).forEach { chunk ->
                chunk.map { url ->
                    async {
                        runCatching {
                            loader.execute(
                                ImageRequest.Builder(app)
                                    .data(url)
                                    .size(sizePx, sizePx)
                                    .build()
                            )
                        }
                    }
                }.awaitAll()
            }
        }
    }

    /** Gson may supply null for legacy/non-null model fields; never let a bad cover crash startup warmup. */
    private fun MutableList<String>.addIfPresent(value: String?) {
        if (!value.isNullOrBlank()) add(value)
    }
}
