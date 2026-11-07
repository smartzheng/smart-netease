package com.smart.neteast.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.takahashirinta.kanesumi.anim.sokuou.rememberMetroFlingBehavior
import io.github.takahashirinta.kanesumi.controls.MetroSelectorFlyout
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import com.smart.neteast.ui.design.SaltIcon
import com.moriafly.salt.ui.Text as SaltText
import com.moriafly.salt.ui.Switcher as SaltSwitcher
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.neteast.account.SessionMonitor
import com.smart.neteast.auth.CookieManager
import com.smart.neteast.cache.ContentCache
import android.content.Context
import android.widget.Toast
import com.smart.neteast.network.PlaylistApi
import com.smart.neteast.network.RetrofitClient
import com.smart.neteast.power.BackgroundActivity
import com.smart.neteast.ui.BottomOverlayInsetDp
import com.smart.neteast.ui.components.QrAuthorizeScreen
import com.smart.neteast.ui.components.QrLoginDialog
import com.smart.neteast.ui.components.UpdateAvailableBanner
import com.smart.neteast.ui.components.ScreenTitle
import com.smart.neteast.ui.components.ncrustDialogSurface
import com.smart.neteast.ui.components.rememberManualUpdateCheck
import com.smart.neteast.ui.components.vipStatusText
import com.smart.neteast.ui.i18n.LocalStrings
import com.smart.neteast.ui.i18n.LanguagePreset
import com.smart.neteast.ui.i18n.getSavedLanguageCode
import com.smart.neteast.ui.i18n.languagePresets
import com.smart.neteast.ui.theme.ThemeColorSelector
import com.smart.neteast.ui.theme.ThemeMode
import com.smart.neteast.ui.theme.themeColorPresets
import com.smart.neteast.ui.viewmodel.PlayerViewModel
import com.smart.neteast.update.UpdateService
import com.smart.neteast.playlist.PlaylistManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UserScreen(
    onOpenAbout: () -> Unit = {},
    onOpenUpdate: () -> Unit = {},
    themeIndex: Int = 0,
    onThemeChange: (Int) -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onShowWebLogin: () -> Unit = {},
    refreshTrigger: Int = 0,
    onLanguageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    var showAccountDialog by remember { mutableStateOf(false) }
    var showQrLogin by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf(0L) }
    // 缓存占用要递归遍历 cacheDir，放 IO 线程算，避免组合期主线程卡顿。
    LaunchedEffect(Unit) {
        cacheSize = withContext(Dispatchers.IO) { currentCacheSize(context) }
    }
    var hasCookie by remember { mutableStateOf(CookieManager.hasCookie(context)) }
    // 扫码登录为平板 / 大屏独占：手机端未登录点头像仍直接进 WebView 官方登录页。
    val isWideLayout = LocalConfiguration.current.screenWidthDp >= 600

    var userProfile by remember { mutableStateOf<PlaylistApi.UserProfile?>(null) }
    var isLoadingProfile by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("ncrust_settings", 0) }
    val playerViewModel: PlayerViewModel = viewModel()
    var wifiQuality by remember { mutableIntStateOf(prefs.getInt("wifi_quality", 3)) }
    var mobileQuality by remember { mutableIntStateOf(prefs.getInt("mobile_quality", 1)) }
    // 1.4.0 起默认关闭：预载续播未达可靠标准，降级为用户显式开启的实验特性。
    var gaplessEnabled by remember { mutableStateOf(prefs.getBoolean("gapless_playback", false)) }
    var lyricsTranslation by remember { mutableStateOf(prefs.getBoolean("lyrics_translation", true)) }
    // 设置项「非会员遇到 VIP 歌曲」：true = 播放试听片段（默认），false = 直接跳过。
    var trialPlayback by remember { mutableStateOf(prefs.getBoolean("vip_trial_playback", true)) }

    // 更新：状态在 UpdateService 单例里（检查可能在本页挂载之前就跑完了）。
    val updateState by UpdateService.state.collectAsState()
    var autoCheckUpdate by remember { mutableStateOf(updateState.autoCheckEnabled) }
    val manualUpdateCheck = rememberManualUpdateCheck(onFound = onOpenUpdate)

    // 会话/VIP 状态：会员文案只在这里显示（接口失败显示为未知，绝不显示「未开通」）。
    val sessionState by SessionMonitor.state.collectAsState()
    val vipText = vipStatusText(strings, sessionState)

    var selectedLanguageCode by remember { mutableStateOf(getSavedLanguageCode(context)) }

    fun loadProfile() {
        if (!CookieManager.hasCookie(context)) {
            userProfile = null
            hasCookie = false
            return
        }
        coroutineScope.launch {
            isLoadingProfile = true
            try {
                val profile = PlaylistApi.getUserProfile()
                // 服务端对失效 cookie 会返回空 account/profile → userId=0。
                // 交给 SessionMonitor 统一转 Expired：清请求层 cookie + 保留「曾登录」标记，
                // 全局横幅据此说「登录已失效」而不是「未登录」。
                if (profile.userId == 0L) {
                    SessionMonitor.markExpired(context)
                    hasCookie = CookieManager.hasCookie(context)
                    userProfile = null
                } else {
                    userProfile = profile
                    hasCookie = true
                }
            } catch (_: Exception) {
                userProfile = null
            } finally {
                isLoadingProfile = false
            }
        }
    }

    LaunchedEffect(Unit) { loadProfile() }
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            hasCookie = CookieManager.hasCookie(context)
            loadProfile()
        }
    }

    if (showAccountDialog) AccountDialog(
        userProfile = userProfile,
        onDismiss = { showAccountDialog = false },
        onScanAuthorize = {
            showAccountDialog = false
            showScanner = true
        },
        onLogout = {
            CookieManager.clearCookie(context)
            RetrofitClient.updateCookie(null)
            // 主动退出 ≠ 登录失效：清掉「曾登录」标记，否则会一直显示失效横幅。
            SessionMonitor.onLoggedOut(context)
            hasCookie = false
            userProfile = null
            showAccountDialog = false
        }
    )

    // 手机端扫码授权：扫平板登录二维码 → 解析 unikey → 局域网回传本机 cookie。
    // 连接/成功/失败状态都在扫码页内呈现，不再扫到即关闭。
    if (showScanner) QrAuthorizeScreen(
        onAuthorized = { showScanner = false },
        onClose = { showScanner = false }
    )

    if (showQrLogin) QrLoginDialog(
        onLoginSuccess = { cookie ->
            CookieManager.saveCookie(context, cookie)
            RetrofitClient.updateCookie(cookie)
            // 登录成功 → 会话回 Valid，失效横幅消失。
            SessionMonitor.onLoginSucceeded(context)
            hasCookie = true
            showQrLogin = false
            loadProfile()
        },
        onGenericLogin = {
            showQrLogin = false
            onShowWebLogin()
        },
        onDismiss = { showQrLogin = false }
    )

    if (showClearCacheConfirm) ClearCacheConfirmDialog(
        onConfirm = {
            showClearCacheConfirm = false
            coroutineScope.launch {
                val size = withContext(Dispatchers.IO) {
                    ContentCache.clearAll()
                    runCatching { coil.Coil.imageLoader(context).memoryCache?.clear() }
                    runCatching { coil.Coil.imageLoader(context).diskCache?.clear() }
                    runCatching {
                        context.cacheDir?.let { dir ->
                            dir.listFiles()
                                ?.filter { it.name == "WebView" || it.name == "http" }
                                ?.forEach { it.deleteRecursively() }
                        }
                    }
                    currentCacheSize(context)
                }
                cacheSize = size
                Toast.makeText(context, strings.cacheCleared, Toast.LENGTH_SHORT).show()
            }
        },
        onDismiss = { showClearCacheConfirm = false }
    )

    // 宽屏设置/资料内容居中限宽（上限 720dp），避免设置行横跨平板。
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
        modifier = Modifier.widthIn(max = 720.dp).fillMaxHeight(),
        contentPadding = PaddingValues(bottom = BottomOverlayInsetDp),
        flingBehavior = rememberMetroFlingBehavior()
    ) {
        // Groove 大字页头。
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            ) {
                ScreenTitle(strings.tabUser)
            }
            Spacer(Modifier.height(4.dp))
        }

        // 发现新版本：用户页顶部一行（不弹窗、不发通知的落点）。「稍后」过的版本不再出现，
        // 直到有更高的版本 —— 判定在 UpdateState.shouldNotify。
        if (updateState.shouldNotify) {
            item {
                UpdateAvailableBanner(
                    version = updateState.available?.version?.toString().orEmpty(),
                    onOpen = onOpenUpdate
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // Profile 块。整块可点：已登录 → 账户管理；未登录 → 直接进 WebView 登录。
        item {
            ProfileBlock(
                isLoading = isLoadingProfile,
                profile = userProfile,
                notLoggedInText = strings.notLoggedIn,
                loginHintText = strings.loginHint,
                uidLabel = strings.uidLabel(userProfile?.userId?.toString() ?: ""),
                onClick = {
                    when {
                        hasCookie -> showAccountDialog = true
                        // 平板/大屏：原生扫码（官方 App 扫），下面再给「通用登录」退回 WebView
                        isWideLayout -> showQrLogin = true
                        else -> onShowWebLogin()
                    }
                }
            )
            // 会员状态（有效 / 已过期 / 未开通 / 未知）。会话不是 Valid 时不显示——
            // 那时该说的是「登录已失效」，不是会员状态。
            if (vipText != null) {
                SaltText(
                    vipText,
                    color = LocalMetroColors.current.onSurfaceVariant,
                    style = LocalMetroTypography.current.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // 音质
        item {
            SectionTitle(strings.qualitySectionTitle)
            SettingsGroup {
                MetroDropdownRow(
                    label = strings.wifiQualityLabel,
                    selectedIndex = wifiQuality,
                    options = strings.qualityOptions,
                    onSelect = {
                        wifiQuality = it
                        prefs.edit().putInt("wifi_quality", it).apply()
                    }
                )
                MetroDropdownRow(
                    label = strings.mobileQualityLabel,
                    selectedIndex = mobileQuality,
                    options = strings.qualityOptions,
                    onSelect = {
                        mobileQuality = it
                        prefs.edit().putInt("mobile_quality", it).apply()
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // 播放
        item {
            SectionTitle(strings.playbackSectionTitle)
            SettingsGroup {
                // 歌词翻译开关。切了立即生效，播放器常挂载无需重进。
                SettingSwitchRow(
                    title = strings.lyricsTranslationLabel,
                    checked = lyricsTranslation,
                    onCheckedChange = {
                        lyricsTranslation = it
                        prefs.edit().putBoolean("lyrics_translation", it).apply()
                        playerViewModel.setLyricsTranslation(it)
                    }
                )
                MetroDropdownRow(
                    label = strings.trialSettingTitle,
                    selectedIndex = if (trialPlayback) 0 else 1,
                    options = listOf(strings.trialSettingPlay, strings.trialSettingSkip),
                    onSelect = { index ->
                        trialPlayback = index == 0
                        prefs.edit().putBoolean("vip_trial_playback", trialPlayback).apply()
                    }
                )
                SettingSwitchRow(
                    title = strings.gaplessSectionTitle,
                    description = strings.gaplessDescription,
                    note = strings.gaplessBetaNote,
                    checked = gaplessEnabled,
                    onCheckedChange = {
                        gaplessEnabled = it
                        prefs.edit().putBoolean("gapless_playback", it).apply()
                        playerViewModel.refreshGaplessSetting()
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            val playlistManagement by PlaylistManager.enabled.collectAsState()
            SettingsGroup {
                SettingSwitchRow(
                    title = strings.playlists.experimental,
                    description = strings.playlists.experimentalDescription,
                    checked = playlistManagement,
                    onCheckedChange = { PlaylistManager.setEnabled(context, it) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // 外观：主题模式 + 主题色 + 语言
        item {
            SectionTitle(strings.themeModeSectionTitle)
            ThemeModeSelector(
                selected = themeMode,
                labels = Triple(
                    strings.themeModeSystem,
                    strings.themeModeDark,
                    strings.themeModeLight
                ),
                onSelect = onThemeModeChange
            )
            Spacer(Modifier.height(24.dp))

            SectionTitle(strings.themeSectionTitle)
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ThemeColorSelector(
                    selectedIndex = themeIndex,
                    presets = themeColorPresets,
                    onSelect = onThemeChange
                )
            }
            Spacer(Modifier.height(24.dp))

            SectionTitle(strings.languageSectionTitle)
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MetroLanguageDropdown(
                    selectedCode = selectedLanguageCode,
                    presets = languagePresets,
                    onSelect = { code ->
                        if (code != selectedLanguageCode) {
                            selectedLanguageCode = code
                            onLanguageChange(code)
                        }
                    }
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        // 后台运行：跳转系统"允许后台活动 / 忽略电池优化"设置（与首次启动弹窗同一入口）。
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        runCatching {
                            context.startActivity(BackgroundActivity.requestIntent(context))
                        }.onFailure {
                            runCatching {
                                context.startActivity(BackgroundActivity.appDetailsIntent(context))
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaltText(
                    strings.batteryTitle,
                    color = LocalMetroColors.current.onBackground,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier.weight(1f)
                )
                SaltIcon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = LocalMetroColors.current.onSurfaceVariant,
                    sizeDp = 20.dp,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // 存储与缓存：显示当前占用，点击后弹窗确认再清除。
        item {
            SectionTitle(strings.storageSectionTitle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearCacheConfirm = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaltText(
                    strings.cacheSizeLabel(cacheSize),
                    color = LocalMetroColors.current.onBackground,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier.weight(1f)
                )
                SaltText(
                    strings.clearCache,
                    color = LocalMetroColors.current.primary,
                    style = TextStyle(fontSize = 15.sp)
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        // 更新：自动检查开关（默认开）+ 手动检查入口（手动不受 24 小时限制）。
        item {
            SectionTitle(strings.updateSectionTitle)
            SettingSwitchRow(
                title = strings.updateAutoCheckTitle,
                description = strings.updateAutoCheckDescription,
                checked = autoCheckUpdate,
                onCheckedChange = {
                    autoCheckUpdate = it
                    UpdateService.setAutoCheckEnabled(context, it)
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !updateState.checking, onClick = manualUpdateCheck)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaltText(
                    strings.updateCheckNow,
                    color = LocalMetroColors.current.onBackground,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier.weight(1f)
                )
                SaltText(
                    if (updateState.checking) strings.updateChecking else "",
                    color = LocalMetroColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 13.sp)
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        // 关于：单行条目，右侧带箭头。
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAbout)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaltText(
                    strings.aboutButton,
                    color = LocalMetroColors.current.onBackground,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier.weight(1f)
                )
                SaltIcon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = LocalMetroColors.current.onSurfaceVariant,
                    sizeDp = 20.dp,
                )
            }
        }
    }
    }
}

/** Compact profile card with a circular avatar and clear account affordance. */
@Composable
private fun ProfileBlock(
    isLoading: Boolean,
    profile: PlaylistApi.UserProfile?,
    notLoggedInText: String,
    loginHintText: String,
    uidLabel: String,
    onClick: () -> Unit
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LocalMetroColors.current.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LocalMetroColors.current.surface),
            contentAlignment = Alignment.Center
        ) {
            if (profile?.avatarUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = strings.userAvatarDesc,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                SaltIcon(
                    Icons.Outlined.Person,
                    strings.userIconDesc,
                    tint = LocalMetroColors.current.primary,
                    sizeDp = 38.dp,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        // 文字列 weight(1f)：任何语言的昵称/提示都能换行不撑破。
        Column(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> SaltText(
                    strings.loading,
                    color = LocalMetroColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                )
                profile != null -> {
                    SaltText(
                        profile.nickname,
                        color = LocalMetroColors.current.onBackground,
                        style = LocalMetroTypography.current.title.copy(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    SaltText(
                        uidLabel,
                        color = LocalMetroColors.current.onSurfaceVariant,
                        style = LocalMetroTypography.current.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                else -> {
                    SaltText(
                        notLoggedInText,
                        color = LocalMetroColors.current.onSurfaceVariant,
                        style = LocalMetroTypography.current.title.copy(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    SaltText(
                        loginHintText,
                        color = LocalMetroColors.current.onSurfaceVariant.copy(alpha = 0.6f),
                        style = LocalMetroTypography.current.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        SaltIcon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = LocalMetroColors.current.onSurfaceVariant,
            sizeDp = 22.dp,
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(LocalMetroColors.current.surfaceVariant)
            .padding(vertical = 4.dp),
        content = content,
    )
}

/** Groove 风分区标题：16sp semi-bold、上留白 4dp、左 16dp。 */
@Composable
private fun SectionTitle(text: String) {
    SaltText(
        text,
        color = LocalMetroColors.current.onBackground,
        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
    )
}

/**
 * 开关设置行：标题与 Switch 同一行垂直居中，描述（可选）与提示（可选）另起一行。
 * 描述不参与对齐，避免 Switch 被顶到与描述顶部对齐。
 *
 * [note] 用主题色小字单独一行，给「测试中，可能不稳定」这类实验性提示，与灰色描述区分开。
 */
@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    note: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SaltText(
            title,
            color = LocalMetroColors.current.onBackground,
            style = LocalMetroTypography.current.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        SaltSwitcher(state = checked)
    }
    if (description != null) {
        SaltText(
            description,
            color = LocalMetroColors.current.onSurfaceVariant,
            style = LocalMetroTypography.current.caption,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        )
    }
    if (note != null) {
        SaltText(
            note,
            color = LocalMetroColors.current.primary,
            style = LocalMetroTypography.current.caption,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        )
    }
}

/** 主题模式三选一：跟随系统 / 深色 / 浅色。 */
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    labels: Triple<String, String, String>,
    onSelect: (ThemeMode) -> Unit
) {
    val options = listOf(
        ThemeMode.SYSTEM to labels.first,
        ThemeMode.DARK to labels.second,
        ThemeMode.LIGHT to labels.third
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (mode, label) ->
            val active = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (active) LocalMetroColors.current.primary.copy(alpha = 0.14f)
                        else Color.Transparent
                    )
                    .border(
                        1.dp,
                        if (active) LocalMetroColors.current.primary
                        else LocalMetroColors.current.divider,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                SaltText(
                    label,
                    color = if (active) LocalMetroColors.current.primary
                    else LocalMetroColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 清除缓存确认弹窗。 */
@Composable
private fun ClearCacheConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .ncrustDialogSurface()
                .padding(24.dp)
        ) {
            SaltText(
                strings.clearCache,
                color = LocalMetroColors.current.onBackground,
                style = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(12.dp))
            SaltText(
                strings.clearCacheConfirm,
                color = LocalMetroColors.current.onSurfaceVariant,
                style = LocalMetroTypography.current.bodyMedium,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogButton(text = strings.cancel, accent = false, onClick = onDismiss)
                Spacer(Modifier.width(12.dp))
                DialogButton(text = strings.clearCache, accent = true, onClick = onConfirm)
            }
        }
    }
}

/**
 * 单行下拉：左侧 label + 右侧「选中值 + ▼」，点击整行弹出垂直菜单。
 *
 * 多语言鲁棒：
 *  - label 用 weight(1f)，任何语言都能换行，不会挤到右侧值。
 *  - 选中值用 maxLines=1 + Ellipsis + widthIn(max=160dp)，极端长文会截断但不会撑破布局。
 *  - 下拉展开的菜单里每项独占一行，完整显示，用户始终能看到完整名字。
 */
@Composable
private fun MetroDropdownRow(
    label: String,
    selectedIndex: Int,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaltText(
                label,
                color = LocalMetroColors.current.onBackground,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            SaltText(
                options.getOrElse(selectedIndex) { "" },
                color = LocalMetroColors.current.primary,
                style = TextStyle(fontSize = 15.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 160.dp)
            )
            SaltIcon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = LocalMetroColors.current.onSurfaceVariant,
                sizeDp = 20.dp,
            )
        }
        // UWP ComboBox 移植:选中项落回锚点原位,菜单从锚点双向展开。
        MetroSelectorFlyout(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = options,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            containerColor = LocalMetroColors.current.surface,
            cornerRadiusDp = 20.dp,
        )
    }
}

@Composable
private fun AccountDialog(
    userProfile: PlaylistApi.UserProfile?,
    onDismiss: () -> Unit,
    onScanAuthorize: () -> Unit,
    onLogout: () -> Unit
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .ncrustDialogSurface()
                .padding(24.dp)
        ) {
            SaltText(
                strings.accountDialogTitle,
                color = LocalMetroColors.current.onBackground,
                style = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(16.dp))

            if (userProfile != null) {
                SaltText(
                    strings.nicknameLabel(userProfile.nickname),
                    color = LocalMetroColors.current.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                SaltText(
                    strings.uidLabel(userProfile.userId.toString()),
                    color = LocalMetroColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(20.dp))
            }

            // 扫码授权其他设备：扫平板的登录二维码，经局域网把本机账号授权过去。
            FullWidthDialogButton(
                text = strings.scanEntryTitle,
                accent = false,
                borderColor = LocalMetroColors.current.primary,
                textColor = LocalMetroColors.current.primary,
                onClick = onScanAuthorize
            )
            Spacer(Modifier.height(12.dp))

            // 全宽按钮：容器 fillMaxWidth，文字 Center + 换行——极长翻译最多多占一行，不会撑破对话框。
            FullWidthDialogButton(
                text = strings.logoutButton,
                accent = false,
                borderColor = Color.Red.copy(alpha = 0.5f),
                textColor = Color.Red,
                onClick = onLogout
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogButton(
                    text = strings.close,
                    accent = false,
                    onClick = onDismiss
                )
            }
        }
    }
}

/** 短按钮：内容包裹式（wrap content）。用于对话框右下"取消/关闭/保存"。 */
@Composable
private fun DialogButton(
    text: String,
    accent: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (accent) Modifier.background(LocalMetroColors.current.primary)
                else Modifier.border(1.dp, LocalMetroColors.current.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        SaltText(
            text,
            color = if (accent) Color.Black else LocalMetroColors.current.onSurfaceVariant,
            style = TextStyle(fontSize = 14.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 全宽按钮：填满可用宽度，文字居中，多行安全。用于对话框内的"更新 Cookie/退出登录"。 */
@Composable
private fun FullWidthDialogButton(
    text: String,
    accent: Boolean,
    borderColor: Color = LocalMetroColors.current.onSurfaceVariant.copy(alpha = 0.4f),
    textColor: Color = LocalMetroColors.current.onBackground,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (accent) Modifier.background(LocalMetroColors.current.primary)
                else Modifier.border(1.dp, borderColor, RoundedCornerShape(14.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        SaltText(
            text,
            color = if (accent) Color.Black else textColor,
            style = TextStyle(fontSize = 14.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 语言下拉。闭合态显示当前选中语言名 + 箭头；点击弹出所有语言。
 *
 * 多语言鲁棒：闭合态 Text 设 maxLines=1 + Ellipsis + weight(1f)。极长名如
 * "Советский русский"/"Middle English" 最多截断，绝不换行撑破箭头位置。
 */
@Composable
fun MetroLanguageDropdown(
    selectedCode: String,
    presets: List<LanguagePreset>,
    onSelect: (String) -> Unit
) {
    val selected = presets.find { it.code == selectedCode } ?: presets.first()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, LocalMetroColors.current.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaltText(
                selected.displayName,
                color = LocalMetroColors.current.onBackground,
                style = TextStyle(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            SaltIcon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = LocalMetroColors.current.onSurfaceVariant,
                sizeDp = 20.dp,
            )
        }

        // UWP ComboBox 移植:选中项落回锚点原位,内部滚动到选中语言。
        val selectedIndex = presets.indexOfFirst { it.code == selectedCode }.coerceAtLeast(0)
        MetroSelectorFlyout(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = presets.map { it.displayName },
            selectedIndex = selectedIndex,
            containerColor = LocalMetroColors.current.surface,
            cornerRadiusDp = 20.dp,
            onSelect = { index ->
                onSelect(presets[index].code)
                expanded = false
            },
        )
    }
}


/** 统计应用缓存占用：图片磁盘缓存 + 缓存目录（含 WebView 缓存）。 */
private fun currentCacheSize(context: Context): Long {
    var total = 0L
    runCatching { coil.Coil.imageLoader(context).diskCache?.size?.let { total += it } }
    runCatching { context.cacheDir?.let { total += folderSize(it) } }
    return total
}

private fun folderSize(dir: java.io.File): Long {
    var size = 0L
    dir.listFiles()?.forEach { f ->
        size += if (f.isDirectory) folderSize(f) else f.length()
    }
    return size
}
