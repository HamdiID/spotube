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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.spotube.spotube.core.audioplayer.AudioPlayerQueue
import org.spotube.spotube.core.audioplayer.QueueEntry
import org.spotube.spotube.core.ui.spotify.Sp
import org.spotube.spotube.core.ui.spotify.SpIconButton
import org.spotube.spotube.core.ui.spotify.SpIcons
import org.spotube.spotube.modules.saved_tracks.SavedState
import org.spotube.spotube.modules.saved_tracks.SavedTracksViewModel
import org.spotube.spotube.modules.saved_tracks.rememberIsSavedTracks
import spotube.composeapp.generated.resources.Res
import spotube.composeapp.generated.resources.player_like_content_description
import spotube.composeapp.generated.resources.player_unlike_content_description

/** Spotify's "add to Liked Songs" button: an outlined plus that pops into a green check. */
@Composable
fun PlayerHeartButton(
    audioPlayerQueue: AudioPlayerQueue,
    savedTracksViewModel: SavedTracksViewModel,
    iconSize: Dp = 24.dp
) {
    val currentQueueEntry by audioPlayerQueue.currentQueueEntryFlow.collectAsStateWithLifecycle()
    val currentTrackId = (currentQueueEntry as? QueueEntry.StreamingTrack)?.track?.id
    val isSavedTrackState =
        rememberIsSavedTracks(trackIds = currentTrackId?.let { listOf(it) } ?: emptyList())
    val savedTrackIds by savedTracksViewModel.savedTrackIdsFlow.collectAsStateWithLifecycle()
    val pendingSavedTrackIds by savedTracksViewModel.pendingSavedTrackIds.collectAsStateWithLifecycle()

    val isInSavedIds = if (currentTrackId != null) currentTrackId in savedTrackIds else false
    val isLiked = if (isSavedTrackState is SavedState.Success) {
        isSavedTrackState.data.firstOrNull() == true || isInSavedIds
    } else {
        isInSavedIds
    }
    val isPending = currentTrackId != null && currentTrackId in pendingSavedTrackIds

    val pop = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(isLiked) {
        if (first) {
            first = false
            return@LaunchedEffect
        }
        if (isLiked) {
            pop.snapTo(0.6f)
            pop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        }
    }

    SpIconButton(
        imageVector = if (isLiked) SpIcons.CheckCircle else SpIcons.AddCircle,
        contentDescription = stringResource(
            if (isLiked) Res.string.player_unlike_content_description
            else Res.string.player_like_content_description
        ),
        onClick = { currentTrackId?.let { savedTracksViewModel.toggleSavedTrack(it, isLiked) } },
        enabled = isSavedTrackState is SavedState.Success && !isPending,
        tint = if (isLiked) Sp.Green else Sp.Text,
        iconSize = iconSize,
        modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }
    )
}
