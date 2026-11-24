package org.spotube.spotube.core.ui.spotify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.common.Thumbnail
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import org.spotube.spotube.core.audioplayer.AudioPlayerQueue
import org.spotube.spotube.core.audioplayer.QueueEntry
import org.spotube.spotube.core.navigation.NavigationCommands
import org.spotube.spotube.core.navigation.Routes
import org.spotube.spotube.modules.welcome.WelcomeRepository

/** Drives the app into a screen for automated screenshots; only used when SPOTUBE_DEMO is set. */
@Composable
fun UiDemoDriver(mode: String) {
    val welcome: WelcomeRepository = koinInject()
    val queue: AudioPlayerQueue = koinInject()
    val navigation: NavigationCommands = koinInject()
    LaunchedEffect(mode) {
        welcome.completeOnboarding()
        delay(1500)
        if ("track" in mode) {
            val track = MetadataTrack(
                id = "demo-track",
                title = "Midnight City Lights",
                durationMs = 215_000,
                trackNumber = 1,
                discNumber = 1,
                artists = listOf(MetadataArtist.Basic("demo-artist", "Nova Lane", emptyList(), null)),
                album = null,
                thumbnails = listOf(Thumbnail("https://picsum.photos/id/1015/600/600", 600, 600)),
                explicit = false,
                popularity = null,
                isrcCode = null,
                externalUri = null
            )
            queue.load(
                listOf(
                    QueueEntry.StreamingTrack(
                        track = track,
                        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                    )
                ),
                autoPlay = true
            )
        }
        when {
            "search" in mode -> navigation.navigateTo(Routes.Search)
            "library" in mode -> navigation.navigateTo(Routes.Library)
            "settings" in mode -> navigation.navigateTo(Routes.Settings)
        }
    }
}
