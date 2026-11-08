package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustTypography

import com.smart.neteast.ui.theme.LocalNcrustColors



import com.smart.neteast.ui.design.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smart.neteast.network.ArtistSearchItem
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.ui.i18n.LocalStrings
import com.moriafly.salt.ui.Text as SaltText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistSearchItem(
    artist: ArtistSearchItem,
    onClick: () -> Unit,
    menuContent: (@Composable ColumnScope.(onDismiss: () -> Unit) -> Unit)? = null
) {
    val strings = LocalStrings.current
    val aliasStr = artist.alias?.joinToString(" / ") ?: ""
    val transStr = artist.trans ?: ""

    var showMenu by remember { mutableStateOf(false) }

    Box {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = onClick,
                onLongClick = { if (menuContent != null) showMenu = true }
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(72.dp)) {
            AsyncImage(
                model = CoverUrls.small(artist.picUrl),
                contentDescription = strings.artistAvatarDesc,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(LocalNcrustColors.current.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SaltText(
                    artist.name,
                    color = LocalNcrustColors.current.onBackground,
                    style = LocalNcrustTypography.current.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transStr.isNotEmpty()) {
                    SaltText(
                        " · $transStr",
                        color = LocalNcrustColors.current.onSurfaceVariant,
                        style = LocalNcrustTypography.current.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (aliasStr.isNotEmpty()) {
                SaltText(
                    aliasStr,
                    color = LocalNcrustColors.current.onSurfaceVariant,
                    style = LocalNcrustTypography.current.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SaltText(
                strings.artistStats(artist.albumSize ?: 0, artist.musicSize ?: 0),
                color = LocalNcrustColors.current.onSurfaceVariant,
                style = LocalNcrustTypography.current.bodySmall
            )
        }
    }
    if (menuContent != null) {
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
}
