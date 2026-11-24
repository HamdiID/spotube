/*
 * Copyright (C) 2026 Kingkor Roy Tirtho and Spotube Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.spotube.spotube.modules.shell


import org.spotube.millennium.composables.MillenniumIcon
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.spotube.spotube.core.ui.spotify.Sp
import org.spotube.spotube.core.ui.spotify.SpIcon
import org.spotube.spotube.core.ui.spotify.SpIconButton
import org.spotube.spotube.core.ui.spotify.SpIcons
import org.spotube.spotube.core.ui.spotify.SpPlayButton
import org.spotube.spotube.core.ui.spotify.SpSeekBar
import org.spotube.spotube.core.ui.spotify.bouncyClick
import org.spotube.spotube.core.ui.spotify.darken
import org.spotube.spotube.core.ui.spotify.rememberArtworkColor
import spotube.composeapp.generated.resources.devices_title
import spotube.composeapp.generated.resources.player_playing_from
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import org.spotube.spotube.core.ui.theming.AppTheme
import org.spotube.millennium.composables.MillenniumSnackbarHost
import org.spotube.millennium.composables.MillenniumSnackbarHostState
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumModalBottomSheet
import org.spotube.millennium.composables.MillenniumCard
import org.spotube.millennium.composables.MillenniumSlider
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import com.composeunstyled.SheetDetent
import com.composeunstyled.rememberModalBottomSheetState

import com.composeunstyled.Text
import com.composeunstyled.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.spotube.spotube.core.audioplayer.AudioPlayerInterface
import org.spotube.spotube.core.audioplayer.AudioPlayerQueue
import org.spotube.spotube.core.audioplayer.LoopState
import org.spotube.spotube.core.audioplayer.QueueEntry
import org.spotube.spotube.core.jam.JamRole
import org.spotube.spotube.core.jam.JamRoomService
import org.spotube.spotube.core.navigation.NavigationCommands
import org.spotube.spotube.core.navigation.Routes
import org.spotube.spotube.modules.downloads.DownloadProgressIcon
import org.spotube.spotube.modules.downloads.DownloadStatus
import org.spotube.spotube.modules.downloads.DownloadsViewModel
import org.spotube.spotube.modules.lyrics.LyricsViewModel
import org.spotube.spotube.modules.lyrics.SyncedLyricsList
import org.spotube.spotube.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import org.spotube.spotube.modules.saved_tracks.SavedTracksViewModel
import org.spotube.spotube.resources.iconsax.Iconsax
import org.spotube.spotube.resources.iconsax.Iconsax3DotsMore
import org.spotube.spotube.resources.iconsax.IconsaxArrowDown4
import org.spotube.spotube.resources.iconsax.IconsaxArrowSquareUp
import org.spotube.spotube.resources.iconsax.IconsaxCd
import org.spotube.spotube.resources.iconsax.IconsaxCheckCircle
import org.spotube.spotube.resources.iconsax.IconsaxCloseSquare
import org.spotube.spotube.resources.iconsax.IconsaxDirectboxReceive
import org.spotube.spotube.resources.iconsax.IconsaxMusicFilter
import org.spotube.spotube.resources.iconsax.IconsaxNext
import org.spotube.spotube.resources.iconsax.IconsaxPause
import org.spotube.spotube.resources.iconsax.IconsaxPlay
import org.spotube.spotube.resources.iconsax.IconsaxPrevious
import org.spotube.spotube.resources.iconsax.IconsaxRefreshRight
import org.spotube.spotube.resources.iconsax.IconsaxRepeatMusic
import org.spotube.spotube.resources.iconsax.IconsaxRepeateMusic
import org.spotube.spotube.resources.iconsax.IconsaxRepeateOne
import org.spotube.spotube.resources.iconsax.IconsaxShuffle
import org.spotube.spotube.resources.iconsax.InconsaxClock
import org.spotube.spotube.resources.iconsax.SwapHorizontal2
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds
import org.spotube.spotube.core.i18n.getString
import org.jetbrains.compose.resources.stringResource
import spotube.composeapp.generated.resources.Res
import spotube.composeapp.generated.resources.downloads_status_cancelled
import spotube.composeapp.generated.resources.downloads_status_failed
import spotube.composeapp.generated.resources.downloads_status_queued
import spotube.composeapp.generated.resources.player_alternative_source
import spotube.composeapp.generated.resources.player_collapse_content_description
import spotube.composeapp.generated.resources.player_disable_shuffle_content_description
import spotube.composeapp.generated.resources.player_download
import spotube.composeapp.generated.resources.player_download_already_downloaded
import spotube.composeapp.generated.resources.player_download_retrying
import spotube.composeapp.generated.resources.player_download_started
import spotube.composeapp.generated.resources.player_download_status_downloaded
import spotube.composeapp.generated.resources.player_download_status_downloading
import spotube.composeapp.generated.resources.player_enable_shuffle_content_description
import spotube.composeapp.generated.resources.player_expand_lyrics_content_description
import spotube.composeapp.generated.resources.player_go_to_album
import spotube.composeapp.generated.resources.player_loop_mode_all
import spotube.composeapp.generated.resources.player_loop_mode_none
import spotube.composeapp.generated.resources.player_loop_mode_one
import spotube.composeapp.generated.resources.player_lyrics
import spotube.composeapp.generated.resources.player_next_content_description
import spotube.composeapp.generated.resources.player_now_playing
import spotube.composeapp.generated.resources.player_options_content_description
import spotube.composeapp.generated.resources.player_pause_content_description
import spotube.composeapp.generated.resources.player_play_content_description
import spotube.composeapp.generated.resources.player_previous_content_description
import spotube.composeapp.generated.resources.player_seek_label
import spotube.composeapp.generated.resources.player_sleep_timer
import spotube.composeapp.generated.resources.player_track_details
import spotube.composeapp.generated.resources.queue_title

// The expanded player on small screens
// on the top left it has a angle/chevron down that can be used to collapse the player back to the
// floating player
// on the top right it has a three dots for options
// The top bar can be used to swipe down and close the player back to the floating player. It should
// follow the user's finger while swiping down and should have a nice animation when collapsing back
// to the floating player
// On the middle the album art stays rounded, centered and takes up most of the space
// Below the album art, on the left the song title and artists are shown and on the right the like
// button is shown
// below that we have the playback controls and the progress bar, which should be similar to the
// large player but with a different layout to fit the smaller screen
// And at the very end we will have the volume slider
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppExpandedPlayer(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onCollapse: () -> Unit = {},
    onQueue: () -> Unit = {},
    onAlternativeSource: () -> Unit = {},
    onExpandLyrics: () -> Unit = {},
    onDownloadTrack: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
    onSleepTimer: () -> Unit = {},
    audioPlayer: AudioPlayerInterface = koinInject(),
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    playerOptionsViewModel: PlayerOptionsViewModel = koinViewModel(),
    savedTracksViewModel: SavedTracksViewModel = koinViewModel<SavedTracksViewModel>(
        key = SAVED_TRACKS_COLLECTION_ID,
        parameters = { parametersOf() }
    )
) {
    val playerUiState = rememberPlayerUiState(audioPlayer, audioPlayerQueue)
    val jamRoomService: JamRoomService = koinInject()
    val isJamGuest by jamRoomService.role
        .map { it == JamRole.Guest }
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val downloadsViewModel: DownloadsViewModel = koinViewModel()
    val navigationCommands: NavigationCommands = koinInject()
    val currentEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()
    val playerOptionsUiState by playerOptionsViewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack = remember(currentEntry) {
        (currentEntry as? QueueEntry.StreamingTrack)?.track
    }
    val downloads by downloadsViewModel.downloads.collectAsStateWithLifecycle()
    val currentDownload = remember(currentTrack, downloads) {
        currentTrack?.let { t ->
            downloads.lastOrNull { it.track?.id == t.id }
        }
    }
    val snackbarHostState = remember { MillenniumSnackbarHostState() }
    val albumArtModifier =
        rememberSharedAlbumArtModifier(sharedTransitionScope, animatedVisibilityScope)
    val coverModel = playerUiState.coverModel
    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(playerUiState.progress) }
    val moreOptionsSheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.FullyExpanded
    )

    PlayerOptionDialogs(viewModel = playerOptionsViewModel)

    LaunchedEffect(playerUiState.progress, isSeeking) {
        if (!isSeeking) {
            seekProgress = playerUiState.progress
        }
    }

    fun onPlayPause() {
        scope.launch {
            if (playerUiState.isPlaying) {
                audioPlayer.pause()
            } else {
                audioPlayer.play()
            }
        }
    }

    fun onSkipPrevious() {
        if (isJamGuest) return
        scope.launch { audioPlayer.skipToPrevious() }
    }

    fun onSkipNext() {
        if (isJamGuest) return
        scope.launch { audioPlayer.skipToNext() }
    }

    fun onShuffleToggle() {
        if (isJamGuest) return
        scope.launch { audioPlayer.shuffle(!playerUiState.isShuffling) }
    }

    fun onLoopToggle() {
        if (isJamGuest) return
        scope.launch { audioPlayer.loop(playerUiState.loopState.next()) }
    }

    fun onSeekFinished() {
        val durationMillis = playerUiState.seekDuration.inWholeMilliseconds
        if (durationMillis <= 0L) return
        scope.launch {
            audioPlayer.seekTo(
                (durationMillis * seekProgress.coerceIn(
                    0f,
                    1f
                )).toLong().milliseconds
            )
        }
    }

    LaunchedEffect(moreOptionsSheetState.bottomSheetState.currentDetent) {
        if (moreOptionsSheetState.bottomSheetState.currentDetent == SheetDetent.Hidden) {
            playerOptionsViewModel.dismissMoreOptionsSheet()
        }
    }

    LaunchedEffect(playerOptionsUiState.isMoreOptionsSheetOpen) {
        if (playerOptionsUiState.isMoreOptionsSheetOpen) {
            moreOptionsSheetState.animateTo(SheetDetent.FullyExpanded)
        }
    }

    val artworkTint by rememberArtworkColor(coverModel)
    val artScale by animateFloatAsState(
        targetValue = if (playerUiState.isPlaying) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "artScale"
    )
    val artSwipe = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Sp.Base)
            .background(
                Brush.verticalGradient(
                    0f to artworkTint,
                    0.5f to artworkTint.darken(0.55f),
                    1f to Sp.Base
                )
            )
    ) {
        if (playerOptionsUiState.isMoreOptionsSheetOpen) {
            MillenniumModalBottomSheet(
                state = moreOptionsSheetState,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                contentSpacing = 4.dp
            ) {
                // Grid of Options
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OptionTile(
                            icon = Iconsax.SwapHorizontal2,
                            label = stringResource(Res.string.player_alternative_source),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                onAlternativeSource()
                            })
                    }
                    item {
                        val download = currentDownload
                        val status = download?.status
                        val statusLabel = when (status) {
                            is DownloadStatus.Completed -> stringResource(Res.string.player_download_status_downloaded)
                            is DownloadStatus.Failed -> stringResource(Res.string.downloads_status_failed)
                            is DownloadStatus.Cancelled -> stringResource(Res.string.downloads_status_cancelled)
                            is DownloadStatus.Queued -> stringResource(Res.string.downloads_status_queued)
                            is DownloadStatus.Downloading -> stringResource(Res.string.player_download_status_downloading)
                            null -> stringResource(Res.string.player_download)
                        }
                        val statusColor = when (status) {
                            is DownloadStatus.Completed -> AppTheme.colors.primary
                            is DownloadStatus.Failed -> AppTheme.colors.error
                            else -> AppTheme.colors.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .millenniumSurface(
                                    background = millenniumPanelBrush(MillenniumTheme.colors),
                                    shape = RoundedCornerShape(14.dp),
                                    borderColor = MillenniumTheme.colors.border,
                                    borderWidth = 1.dp,
                                    shadowElevation = 2.dp
                                )
                                .clickable {
                                    playerOptionsViewModel.dismissMoreOptionsSheet()
                                    when (status) {
                                        is DownloadStatus.Completed -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(getString(Res.string.player_download_already_downloaded))
                                            }
                                        }

                                        is DownloadStatus.Failed, is DownloadStatus.Cancelled -> {
                                            download?.let { downloadsViewModel.retry(it.id) }
                                            scope.launch {
                                                snackbarHostState.showSnackbar(getString(Res.string.player_download_retrying))
                                            }
                                        }

                                        is DownloadStatus.Downloading, is DownloadStatus.Queued -> {
                                            // already in progress
                                        }

                                        null -> {
                                            val track = currentTrack
                                            if (track != null) {
                                                downloadsViewModel.downloadTrack(track)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = getString(Res.string.player_download_started, track.title)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    onDownloadTrack()
                                }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (status is DownloadStatus.Downloading) {
                                    DownloadProgressIcon(
                                        track = currentTrack,
                                        icon = Iconsax.IconsaxDirectboxReceive,
                                        contentDescription = stringResource(Res.string.player_download),
                                        modifier = Modifier.size(28.dp),
                                        tint = AppTheme.colors.onSurface
                                    )
                                } else {
                                    val icon = when (status) {
                                        is DownloadStatus.Completed -> Iconsax.IconsaxCheckCircle
                                        is DownloadStatus.Failed -> Iconsax.IconsaxCloseSquare
                                        is DownloadStatus.Cancelled -> Iconsax.IconsaxRefreshRight
                                        is DownloadStatus.Queued -> Iconsax.InconsaxClock
                                        null -> Iconsax.IconsaxDirectboxReceive
                                    }
                                    MillenniumIcon(
                                        imageVector = icon,
                                        contentDescription = statusLabel,
                                        modifier = Modifier.size(28.dp),
                                        tint = when (status) {
                                            is DownloadStatus.Completed -> AppTheme.colors.primary
                                            is DownloadStatus.Failed -> AppTheme.colors.error
                                            else -> AppTheme.colors.onSurface
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = statusLabel,
                                    style = AppTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = statusColor
                                )
                            }
                        }
                    }
                    item {
                        OptionTile(
                            icon = Iconsax.InconsaxClock,
                            label = stringResource(Res.string.player_sleep_timer),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                playerOptionsViewModel.showSleepTimerDialog()
                                onSleepTimer()
                            })
                    }
                    item {
                        OptionTile(
                            icon = Iconsax.IconsaxCd,
                            label = stringResource(Res.string.player_go_to_album),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                val albumId = currentTrack?.album?.id
                                if (albumId != null) {
                                    navigationCommands.navigateTo(Routes.Album(albumId))
                                    onCollapse()
                                }
                                onGoToAlbum()
                            })
                    }
                    item {
                        OptionTile(
                            icon = Iconsax.Iconsax3DotsMore,
                            label = stringResource(Res.string.player_track_details),
                            onClick = {
                                playerOptionsViewModel.dismissMoreOptionsSheet()
                                playerOptionsViewModel.showTrackDetails()
                            })
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpIconButton(
                    imageVector = SpIcons.ChevronDown,
                    contentDescription = stringResource(Res.string.player_collapse_content_description),
                    onClick = onCollapse,
                    iconSize = 30.dp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.player_playing_from).uppercase(),
                        style = TextStyle(fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                    Text(
                        text = playerUiState.album.ifBlank { stringResource(Res.string.player_now_playing) },
                        style = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold),
                        color = Sp.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                SpIconButton(
                    imageVector = SpIcons.More,
                    contentDescription = stringResource(Res.string.player_options_content_description),
                    onClick = playerOptionsViewModel::showMoreOptionsSheet
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = artScale
                        scaleY = artScale
                        translationX = artSwipe.value
                        rotationZ = artSwipe.value / 60f
                    }
                    .pointerInput(isJamGuest) {
                        if (isJamGuest) return@pointerInput
                        val threshold = 90.dp.toPx()
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val v = artSwipe.value
                                scope.launch {
                                    if (v <= -threshold) onSkipNext()
                                    else if (v >= threshold) onSkipPrevious()
                                    artSwipe.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
                                }
                            },
                            onDragCancel = { scope.launch { artSwipe.animateTo(0f) } },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                scope.launch { artSwipe.snapTo(artSwipe.value + amount * 0.7f) }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(albumArtModifier)
                        .shadow(28.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black, spotColor = Color.Black)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Sp.Highlight),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverModel != null) {
                        AsyncImage(
                            model = coverModel,
                            contentDescription = playerUiState.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        SpIcon(SpIcons.Music, null, tint = Sp.SubText, size = 96.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerUiState.title,
                        style = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
                        color = Sp.Text,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 2000)
                    )
                    PlayerArtistsText(
                        artists = playerUiState.artistList,
                        onArtistClick = { artistId ->
                            navigationCommands.navigateTo(Routes.Artist(artistId))
                            // The page opens beneath the player, so get out of the way
                            onCollapse()
                        },
                        style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.width(12.dp))
                PlayerHeartButton(
                    audioPlayerQueue = audioPlayerQueue,
                    savedTracksViewModel = savedTracksViewModel,
                    iconSize = 28.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            SpSeekBar(
                value = seekProgress,
                onValueChange = {
                    seekProgress = it
                    isSeeking = true
                },
                onValueChangeFinished = {
                    isSeeking = false
                    onSeekFinished()
                },
                enabled = playerUiState.seekDuration.inWholeMilliseconds > 0L,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val shownPosition = if (isSeeking) {
                    (playerUiState.seekDuration.inWholeMilliseconds * seekProgress).toLong().milliseconds
                } else playerUiState.position
                Text(
                    text = formatPlayerTime(shownPosition),
                    style = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = formatPlayerTime(playerUiState.displayDuration),
                    style = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToggleIcon(
                    imageVector = SpIcons.Shuffle,
                    active = playerUiState.isShuffling,
                    enabled = !isJamGuest,
                    contentDescription = stringResource(if (playerUiState.isShuffling) Res.string.player_disable_shuffle_content_description else Res.string.player_enable_shuffle_content_description),
                    onClick = ::onShuffleToggle
                )
                SpIconButton(
                    imageVector = SpIcons.Previous,
                    contentDescription = stringResource(Res.string.player_previous_content_description),
                    onClick = ::onSkipPrevious,
                    enabled = !isJamGuest,
                    iconSize = 40.dp,
                    touchSize = 56.dp
                )
                SpPlayButton(
                    isPlaying = playerUiState.isPlaying,
                    onClick = ::onPlayPause,
                    size = 68.dp,
                    background = Sp.Text,
                    glyph = Sp.Black,
                    contentDescription = stringResource(if (playerUiState.isPlaying) Res.string.player_pause_content_description else Res.string.player_play_content_description)
                )
                SpIconButton(
                    imageVector = SpIcons.Next,
                    contentDescription = stringResource(Res.string.player_next_content_description),
                    onClick = ::onSkipNext,
                    enabled = !isJamGuest,
                    iconSize = 40.dp,
                    touchSize = 56.dp
                )
                ToggleIcon(
                    imageVector = if (playerUiState.loopState == LoopState.ONE) SpIcons.RepeatOne else SpIcons.Repeat,
                    active = playerUiState.loopState != LoopState.NONE,
                    enabled = !isJamGuest,
                    contentDescription = stringResource(when (playerUiState.loopState) { LoopState.NONE -> Res.string.player_loop_mode_none; LoopState.ONE -> Res.string.player_loop_mode_one; LoopState.ALL -> Res.string.player_loop_mode_all }),
                    onClick = ::onLoopToggle
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpIconButton(
                    imageVector = SpIcons.Devices,
                    contentDescription = stringResource(Res.string.devices_title),
                    onClick = {
                        navigationCommands.navigateTo(Routes.Devices)
                        onCollapse()
                    },
                    tint = Color.White.copy(alpha = 0.85f),
                    iconSize = 20.dp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpIconButton(
                        imageVector = SpIcons.Timer,
                        contentDescription = stringResource(Res.string.player_sleep_timer),
                        onClick = {
                            playerOptionsViewModel.showSleepTimerDialog()
                            onSleepTimer()
                        },
                        tint = Color.White.copy(alpha = 0.85f),
                        iconSize = 20.dp
                    )
                    SpIconButton(
                        imageVector = SpIcons.Queue,
                        contentDescription = stringResource(Res.string.queue_title),
                        onClick = onQueue,
                        tint = Color.White.copy(alpha = 0.85f),
                        iconSize = 22.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LyricsPreviewCard(
                tint = artworkTint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                onExpand = onExpandLyrics
            )
        }

        MillenniumSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun rememberSharedAlbumArtModifier(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return Modifier

    with(sharedTransitionScope) {
        return Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = "player_album_art"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun LyricsPreviewCard(
    tint: Color,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit,
    viewModel: LyricsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncedLyrics = uiState.syncedLyrics ?: emptyList()
    if (syncedLyrics.isEmpty() || uiState.isLoading) return
    val current = uiState.currentIndex.coerceIn(0, syncedLyrics.lastIndex)
    val first = (current - 1).coerceAtLeast(0)
    val last = (first + 4).coerceAtMost(syncedLyrics.lastIndex)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tint.darken(0.12f))
            .bouncyClick(pressedScale = 0.98f, onClick = onExpand)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.player_lyrics),
                style = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
                color = Sp.Text
            )
            SpIcon(
                imageVector = SpIcons.Lyrics,
                contentDescription = stringResource(Res.string.player_expand_lyrics_content_description),
                tint = Sp.Text,
                size = 20.dp
            )
        }
        Spacer(Modifier.height(14.dp))
        for (i in first..last) key(i) {
            val isCurrent = i == uiState.currentIndex
            val isPast = i < uiState.currentIndex
            val lineColor by animateColorAsState(
                when {
                    isCurrent -> Sp.Text
                    isPast -> Color.White.copy(alpha = 0.55f)
                    else -> Color.Black.copy(alpha = 0.55f)
                },
                tween(320),
                label = "lyric"
            )
            Text(
                text = syncedLyrics[i].text.ifBlank { "♪" },
                style = TextStyle(fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
                color = lineColor,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ToggleIcon(
    imageVector: ImageVector,
    active: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SpIconButton(
            imageVector = imageVector,
            contentDescription = contentDescription,
            onClick = onClick,
            enabled = enabled,
            tint = if (active) Sp.Green else Sp.Text,
            iconSize = 26.dp
        )
        val dot by animateFloatAsState(if (active) 1f else 0f, spring(dampingRatio = 0.5f), label = "dot")
        Box(
            modifier = Modifier
                .offset(y = (-6).dp)
                .size(4.dp)
                .graphicsLayer { scaleX = dot; scaleY = dot; alpha = dot }
                .clip(CircleShape)
                .background(Sp.Green)
        )
    }
}

@Composable
private fun OptionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MillenniumCard(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(12.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MillenniumIcon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = AppTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = AppTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}
