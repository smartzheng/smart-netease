package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustTypography

import com.smart.neteast.ui.theme.LocalNcrustColors



import com.smart.neteast.ui.design.*

import com.moriafly.salt.ui.Text as SaltText
import com.moriafly.salt.ui.TextButton as SaltTextButton

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.neteast.playlist.*
import com.smart.neteast.ui.i18n.LocalStrings
import com.smart.neteast.ui.i18n.PlaylistStrings

@Composable
fun PlaylistManagementOverlay(onDeleted: (Long) -> Unit = {}, onOpen: (Long) -> Unit) {
    val state by PlaylistManager.state.collectAsState()
    if (state.visible) PlaylistManagementPanel(state, PlaylistManager::updateDraft, PlaylistManager::submit,
        PlaylistManager::createFromPicker, PlaylistManager::close, onFinish = {
            val deleted = state.result?.takeIf { state.request?.mode == PlaylistPanelMode.DELETE && it.outcome == PlaylistOutcome.CONFIRMED }?.playlistId
            PlaylistManager.finish()
            deleted?.let(onDeleted)
        },
        PlaylistManager::retryRead, PlaylistManager::backToEditor,
        onReload = { state.request?.let(PlaylistManager::open) }, onOpen = onOpen)
}

/** 无网络的展示层：状态来自应用级协调器，便于用模拟状态验收真实 Compose 布局。 */
@Composable
fun PlaylistManagementPanel(
    state: PlaylistPanelState, onDraft: ((PlaylistDraft) -> PlaylistDraft) -> Unit, onSubmit: (Long) -> Unit,
    onCreate: () -> Unit, onClose: () -> Unit, onFinish: () -> Unit,
    onVerify: () -> Unit, onBackToEdit: () -> Unit, onReload: () -> Unit, onOpen: (Long) -> Unit
) {
    val strings = LocalStrings.current
    val p = strings.playlists
    val colors = LocalNcrustColors.current
    val mode = state.request?.mode ?: return
    BackHandler(onBack = onClose)
    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding().navigationBarsPadding().imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SaltText(when (mode) {
                PlaylistPanelMode.ADD -> p.add
                PlaylistPanelMode.CREATE -> p.create
                PlaylistPanelMode.EDIT -> p.edit
                PlaylistPanelMode.REMOVE -> p.remove
                PlaylistPanelMode.DELETE -> p.delete
            }, style = LocalNcrustTypography.current.title.copy(fontSize = 24.sp), modifier = Modifier.weight(1f))
            PlaylistButton(strings.cancel, onClose, containerColor = colors.background, contentColor = colors.onBackground)
        }
        when {
            state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { NcrustProgressIndicator() }
            state.result != null -> Column(Modifier.weight(1f).widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)) {
                val result = state.result
                SaltText(when (result.outcome) {
                    PlaylistOutcome.CONFIRMED -> p.confirmed
                    PlaylistOutcome.PENDING -> if (result.playlistId == null) p.unknownCreate else p.pending
                    PlaylistOutcome.PARTIAL -> if (state.request.songIds.isNotEmpty()) p.partial(result.confirmedIds.size, result.unresolvedIds.size)
                        else p.partial(result.confirmedFields.count { it != PlaylistAction.CREATE }, 3 - result.confirmedFields.count { it != PlaylistAction.CREATE })
                    PlaylistOutcome.FAILED -> p.failed
                }, style = LocalNcrustTypography.current.bodyLarge)
                result.problem?.let { Spacer(Modifier.height(12.dp)); SaltText(playlistProblemText(it, p)) }
                state.backupFile?.let { Spacer(Modifier.height(12.dp)); SaltText(p.backup(it.absolutePath)) }
                Spacer(Modifier.height(20.dp))
                if (result.outcome in listOf(PlaylistOutcome.PENDING, PlaylistOutcome.PARTIAL) && result.playlistId != null) {
                    PlaylistButton(p.verify, onVerify, enabled = !state.busy, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
                if (result.outcome == PlaylistOutcome.FAILED) {
                    PlaylistButton(p.backToEdit, onBackToEdit, enabled = !state.busy, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
                if (result.playlistId != null && mode != PlaylistPanelMode.DELETE) {
                    PlaylistButton(p.open, { onOpen(result.playlistId) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
                PlaylistButton(if (result.outcome == PlaylistOutcome.CONFIRMED) p.done else p.stopTracking,
                    onFinish, enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.surfaceVariant, contentColor = colors.onBackground)
            }
            state.problem != null -> Column(Modifier.weight(1f).widthIn(max = 640.dp).fillMaxWidth().padding(20.dp)) {
                SaltText(playlistProblemText(state.problem, p))
                Spacer(Modifier.height(16.dp))
                PlaylistButton(strings.retry, onReload)
            }
            mode == PlaylistPanelMode.ADD -> LazyColumn(Modifier.weight(1f).widthIn(max = 640.dp).fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
                item { PlaylistButton(p.create, onCreate, enabled = !state.busy, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(16.dp)) }
                val owned = state.collection.items.filter { it.isOwnedBy(state.ownerId) }
                if (owned.isEmpty()) item { SaltText(strings.noPlaylists) }
                items(owned, key = { it.id }) { playlist ->
                    Column(Modifier.fillMaxWidth().clickable(enabled = !state.busy, role = androidx.compose.ui.semantics.Role.Button) { onSubmit(playlist.id) }.padding(vertical = 16.dp)) {
                        SaltText(playlist.name, style = LocalNcrustTypography.current.bodyLarge)
                        SaltText("${strings.trackCount(playlist.trackCount.coerceAtLeast(0))} · ${if (playlist.privacy == 10) p.private else if (playlist.privacy == 0) p.public else p.unknown}", color = colors.onSurfaceVariant)
                    }
                    NcrustDivider()
                }
                if (!state.collection.complete) item { SaltText(p.incomplete); PlaylistButton(strings.retry, onReload, enabled = !state.busy) }
            }
            else -> PlaylistEditorBody(state, onDraft, onSubmit, Modifier.weight(1f))
        }
        if (state.busy) Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            NcrustProgressIndicator(sizeDp = 20.dp); Spacer(Modifier.width(12.dp)); SaltText(strings.loading)
        }
    }
}

@Composable
private fun PlaylistEditorBody(state: PlaylistPanelState, onDraft: ((PlaylistDraft) -> PlaylistDraft) -> Unit, onSubmit: (Long) -> Unit, modifier: Modifier) {
    val strings = LocalStrings.current
    val p = strings.playlists
    val colors = LocalNcrustColors.current
    val mode = state.request!!.mode
    Column(modifier.widthIn(max = 640.dp).fillMaxWidth()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
            if (mode == PlaylistPanelMode.CREATE || mode == PlaylistPanelMode.EDIT) {
                PlaylistFieldLabel(p.name, "${state.draft.name.length}/40")
                Spacer(Modifier.height(8.dp))
                NcrustTextField(state.draft.name, { value -> if (value.length <= 40) onDraft { it.copy(name = value) } },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = p.name }, enabled = !state.busy, singleLine = true)
                Spacer(Modifier.height(24.dp))
                PlaylistFieldLabel(p.description, "${state.draft.description.length}/1000")
                Spacer(Modifier.height(8.dp))
                NcrustTextField(state.draft.description, { value -> if (value.length <= 1000) onDraft { it.copy(description = value) } },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = p.description }, enabled = !state.busy, minLines = 3, maxLines = 6)
                Spacer(Modifier.height(24.dp))
                PlaylistFieldLabel(p.visibility)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to p.public, 10 to p.private).forEach { (value, label) ->
                        val selected = state.draft.privacy == value
                        PlaylistButton(label, { onDraft { it.copy(privacy = value) } }, enabled = !state.busy,
                            modifier = Modifier.weight(1f), selected = selected,
                            containerColor = if (selected) colors.primary else colors.surfaceVariant,
                            contentColor = if (selected) playlistTextColor(colors.primary) else colors.onBackground)
                    }
                }
            } else {
                SaltText(if (mode == PlaylistPanelMode.DELETE) p.deleteHint(state.snapshot?.info?.name.orEmpty()) else p.removeHint(state.request.songIds.size))
            }
        }
        val editing = mode == PlaylistPanelMode.CREATE || mode == PlaylistPanelMode.EDIT
        val label = when (mode) {
            PlaylistPanelMode.CREATE -> p.create
            PlaylistPanelMode.EDIT -> p.save
            PlaylistPanelMode.DELETE -> p.delete
            else -> p.remove
        }
        PlaylistButton(label, { onSubmit(0) },
            enabled = !state.busy && if (editing) state.draft.valid else state.snapshot?.info?.isOwnedBy(state.ownerId) == true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp))
    }
}

@Composable
private fun PlaylistFieldLabel(label: String, value: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SaltText(label, modifier = Modifier.weight(1f), color = LocalNcrustColors.current.onSurfaceVariant,
            style = LocalNcrustTypography.current.caption)
        if (value != null) SaltText(value, color = LocalNcrustColors.current.onSurfaceVariant,
            style = LocalNcrustTypography.current.caption)
    }
}

fun playlistProblemText(problem: PlaylistProblem, p: PlaylistStrings): String = when (problem) {
    PlaylistProblem.LOGIN -> p.login
    PlaylistProblem.PERMISSION -> p.permission
    PlaylistProblem.RATE_LIMIT -> p.rateLimit
    PlaylistProblem.NETWORK -> p.network
    PlaylistProblem.ACCOUNT_CHANGED -> p.accountChanged
    PlaylistProblem.INVALID -> p.invalid
    PlaylistProblem.INCOMPLETE -> p.incomplete
    PlaylistProblem.REJECTED -> p.failed
}

@Composable
fun PlaylistSelectionActions(count: Int, owned: Boolean, onAll: () -> Unit, onCancel: () -> Unit,
    onAdd: () -> Unit, onRemove: () -> Unit) {
    val s = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SaltText(s.playlists.selectionCount(count))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlaylistButton(s.playlists.selectAll, onAll, Modifier.weight(1f),
                containerColor = LocalNcrustColors.current.surfaceVariant, contentColor = LocalNcrustColors.current.onBackground)
            PlaylistButton(s.cancel, onCancel, Modifier.weight(1f),
                containerColor = LocalNcrustColors.current.surfaceVariant, contentColor = LocalNcrustColors.current.onBackground)
        }
        PlaylistButton(s.playlists.add, onAdd, Modifier.fillMaxWidth(), enabled = count > 0)
        if (owned) PlaylistButton(s.playlists.remove, onRemove, Modifier.fillMaxWidth(), enabled = count > 0)
    }
}

/** 强调色可由用户任意选择，白字在云杉绿或素白上不可读。只为本次新增按钮选择前景。 */
internal fun playlistTextColor(background: Color): Color {
    return com.smart.neteast.ui.theme.contrastingColor(background)
}

@Composable
fun PlaylistButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    containerColor: Color = LocalNcrustColors.current.primary, contentColor: Color? = null, selected: Boolean? = null) {
    val latestClick by rememberUpdatedState(onClick)
    SaltTextButton(onClick = onClick, text = text, modifier = modifier.clearAndSetSemantics {
        this.text = AnnotatedString(text)
        role = Role.Button
        selected?.let { this.selected = it }
        if (!enabled) disabled() else onClick { latestClick(); true }
    }, enabled = enabled,
        backgroundColor = containerColor,
        textColor = contentColor ?: playlistTextColor(containerColor))
}
