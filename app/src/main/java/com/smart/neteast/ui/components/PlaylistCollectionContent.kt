package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustTypography

import com.smart.neteast.ui.theme.LocalNcrustColors
import com.smart.neteast.ui.theme.NcrustShape



import com.smart.neteast.ui.design.*

import com.smart.neteast.ui.design.SaltIcon

import com.moriafly.salt.ui.Text as SaltText

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.network.PlaylistApi.PlaylistInfo
import com.smart.neteast.playlist.PlaylistCollection
import com.smart.neteast.ui.BottomOverlayInsetDp
import com.smart.neteast.ui.i18n.LocalStrings

/** 窄屏一列，宽屏按内容宽度排多列；名称获得独立于方封面的阅读空间。 */
@Composable
fun PlaylistCollectionContent(
    collection: PlaylistCollection,
    ownerId: Long,
    loading: Boolean,
    error: String?,
    management: Boolean,
    onCreate: () -> Unit,
    onRetry: () -> Unit,
    onOpen: (PlaylistInfo) -> Unit,
    onPlay: (Long) -> Unit
) {
    val s = LocalStrings.current
    val validItems = remember(collection.items) { collection.items.filter { it.id > 0 }.distinctBy { it.id } }
    val owned = remember(validItems, ownerId) { validItems.filter { it.isOwnedBy(ownerId) } }
    val subscribed = remember(validItems, ownerId) { validItems.filterNot { it.isOwnedBy(ownerId) } }
    val waiting = loading && collection.items.isEmpty()
    Crossfade(waiting, modifier = Modifier.fillMaxSize().background(LocalNcrustColors.current.background),
        animationSpec = NcrustMotion.CoverFade, label = "PlaylistCollectionLoad") { showLoader ->
        if (showLoader) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { NcrustProgressIndicator() }
        else Column(Modifier.fillMaxSize()) {
            // 状态放在滚动区域之外，插入提示时不能被 LazyGrid 的锚点保留滚出视口。
            if (error != null || !collection.complete) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SaltText(error ?: s.playlists.incomplete, color = LocalNcrustColors.current.onSurfaceVariant)
                    if (error != null) PlaylistButton(s.retry, onRetry,
                        containerColor = LocalNcrustColors.current.surfaceVariant,
                        contentColor = LocalNcrustColors.current.onBackground)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(340.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    start = 16.dp, top = 8.dp, end = 16.dp, bottom = BottomOverlayInsetDp
                ),
                flingBehavior = rememberNcrustFlingBehavior()
            ) {
                if (owned.isNotEmpty() || management) item(span = { GridItemSpan(maxLineSpan) }, key = "owned") {
                    PlaylistGroupHeading(s.playlists.owned, owned.size, if (management) onCreate else null)
                }
                items(owned, key = { "owned-playlist-${it.id}" }) { item -> PlaylistCollectionRow(item, { onOpen(item) }, { onPlay(item.id) }) }
                if (subscribed.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }, key = "subscribed") {
                    PlaylistGroupHeading(s.playlists.subscribed, subscribed.size)
                }
                items(subscribed, key = { "subscribed-playlist-${it.id}" }) { item -> PlaylistCollectionRow(item, { onOpen(item) }, { onPlay(item.id) }) }
                if (collection.items.isEmpty() && error == null) item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    SaltText(s.noPlaylists, color = LocalNcrustColors.current.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp))
                }
            }
        }
    }
}

@Composable
private fun PlaylistGroupHeading(title: String, count: Int, onCreate: (() -> Unit)? = null) {
    val colors = LocalNcrustColors.current
    Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SaltText(title, modifier = Modifier.weight(1f), style = LocalNcrustTypography.current.title,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            SaltText(count.toString(), style = LocalNcrustTypography.current.caption,
                color = colors.onSurfaceVariant)
        }
        if (onCreate != null) PlaylistButton(LocalStrings.current.playlists.create, onCreate,
            containerColor = colors.surfaceVariant, contentColor = colors.onBackground)
    }
}

@Composable
fun PlaylistCollectionRow(playlist: PlaylistInfo, onOpen: () -> Unit, onPlay: () -> Unit) {
    val s = LocalStrings.current
    val colors = LocalNcrustColors.current
    Row(Modifier.fillMaxWidth().heightIn(min = 84.dp).clickable(role = Role.Button, onClick = onOpen)
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        PlaylistArtwork(playlist.coverImgUrl, Modifier.size(72.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SaltText(playlist.name, style = LocalNcrustTypography.current.bodyLarge,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            val count = if (playlist.trackCount >= 0) s.trackCount(playlist.trackCount) else s.playlists.unknown
            SaltText(listOfNotNull(count, s.playlists.private.takeIf { playlist.privacy == 10 }).joinToString(" · "),
                style = LocalNcrustTypography.current.caption, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        PlayAllButton(size = 44.dp, onClick = onPlay)
    }
}

@Composable
internal fun PlaylistArtwork(url: String?, modifier: Modifier = Modifier, large: Boolean = false) {
    val colors = LocalNcrustColors.current
    Box(modifier.clip(NcrustShape.artwork).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
        if (url.isNullOrBlank()) {
            SaltIcon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, sizeDp = 32.dp,
                tint = colors.onSurfaceVariant)
        } else AsyncImage(if (large) CoverUrls.large(url) else CoverUrls.small(url), contentDescription = LocalStrings.current.playlistCoverDesc,
            placeholder = ColorPainter(colors.surfaceVariant), error = ColorPainter(colors.surfaceVariant),
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
