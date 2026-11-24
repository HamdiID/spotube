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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.spotube.spotube.core.audioplayer.AudioPlayerInterface
import org.spotube.spotube.core.audioplayer.AudioPlayerQueue
import org.spotube.spotube.core.ui.spotify.Sp
import org.spotube.spotube.core.ui.spotify.SpIcon
import org.spotube.spotube.core.ui.spotify.SpIconButton
import org.spotube.spotube.core.ui.spotify.SpIcons
import org.spotube.spotube.core.ui.spotify.darken
import org.spotube.spotube.core.ui.spotify.rememberArtworkColor
import org.spotube.spotube.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import org.spotube.spotube.modules.saved_tracks.SavedTracksViewModel
import spotube.composeapp.generated.resources.Res
import spotube.composeapp.generated.resources.player_nothing_playing
import spotube.composeapp.generated.resources.player_pause_content_description
import spotube.composeapp.generated.resources.player_play_content_description

internal val FloatingPlayerContentHeight = 64.dp
internal val FloatingPlayerBottomBleed = 16.dp

// Spotify-style mini player: a rounded card tinted with the artwork colour that floats above the
// bottom bar. Swipe it sideways to skip, swipe it up (handled by the sheet) to open the player.
@Composable
fun AppFloatingPlayer(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    audioPlayer: AudioPlayerInterface = koinInject(),
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    savedTracksViewModel: SavedTracksViewModel = koinViewModel<SavedTracksViewModel>(
        key = SAVED_TRACKS_COLLECTION_ID,
        parameters = { parametersOf() }
    )
) {
    val playerUiState = rememberPlayerUiState(audioPlayer, audioPlayerQueue)
    val scope = rememberCoroutineScope()
    val albumArtModifier =
        rememberSharedAlbumArtModifier(sharedTransitionScope, animatedVisibilityScope)

    fun onPlayPause() {
        scope.launch {
            if (playerUiState.isPlaying) {
                audioPlayer.pause()
            } else {
                audioPlayer.play()
            }
        }
    }

    FloatingPlayerContent(
        playerUiState = playerUiState,
        modifier = modifier,
        albumArtModifier = albumArtModifier,
        onPlayPause = ::onPlayPause,
        onSkipNext = { scope.launch { runCatching { audioPlayer.skipToNext() } } },
        onSkipPrevious = { scope.launch { runCatching { audioPlayer.skipToPrevious() } } },
        heartButton = {
            PlayerHeartButton(
                audioPlayerQueue = audioPlayerQueue,
                savedTracksViewModel = savedTracksViewModel
            )
        }
    )
}

@Composable
internal fun FloatingPlayerContent(
    playerUiState: PlayerUiState,
    modifier: Modifier = Modifier,
    albumArtModifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    heartButton: @Composable () -> Unit
) {
    val coverModel = playerUiState.coverModel
    val tint by rememberArtworkColor(coverModel, fallback = Sp.Highlight)
    val swipe = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val hasQueue = playerUiState.queue.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(FloatingPlayerContentHeight + FloatingPlayerBottomBleed)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FloatingPlayerContentHeight)
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.darken(0.28f))
                .pointerInput(hasQueue) {
                    if (!hasQueue) return@pointerInput
                    val threshold = 72.dp.toPx()
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val value = swipe.value
                            scope.launch {
                                when {
                                    value <= -threshold -> onSkipNext()
                                    value >= threshold -> onSkipPrevious()
                                }
                                swipe.animateTo(0f, spring(dampingRatio = 0.6f))
                            }
                        },
                        onDragCancel = { scope.launch { swipe.animateTo(0f) } },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            scope.launch { swipe.snapTo(swipe.value + amount * 0.6f) }
                        }
                    )
                }
                .drawBehind {
                    // Progress line along the bottom edge of the card, like Spotify.
                    val h = 2.dp.toPx()
                    val inset = 8.dp.toPx()
                    val y = size.height - h
                    val w = size.width - inset * 2
                    drawRect(Color.White.copy(alpha = 0.22f), Offset(inset, y), Size(w, h))
                    drawRect(Color.White, Offset(inset, y), Size(w * playerUiState.progress, h))
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp, end = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            translationX = swipe.value
                            alpha = 1f - (kotlin.math.abs(swipe.value) / 300f).coerceIn(0f, 0.7f)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .then(albumArtModifier)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverModel != null) AsyncImage(
                            model = coverModel,
                            contentDescription = playerUiState.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        else SpIcon(SpIcons.Music, null, tint = Sp.SubText, size = 22.dp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!hasQueue) stringResource(Res.string.player_nothing_playing) else playerUiState.title,
                            style = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold),
                            color = Sp.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasQueue) Text(
                            text = playerUiState.artists,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasQueue) heartButton()
                    SpIconButton(
                        imageVector = if (playerUiState.isPlaying) SpIcons.Pause else SpIcons.Play,
                        contentDescription = stringResource(
                            if (playerUiState.isPlaying) Res.string.player_pause_content_description
                            else Res.string.player_play_content_description
                        ),
                        onClick = onPlayPause,
                        enabled = hasQueue,
                        iconSize = 28.dp
                    )
                }
            }
        }
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
