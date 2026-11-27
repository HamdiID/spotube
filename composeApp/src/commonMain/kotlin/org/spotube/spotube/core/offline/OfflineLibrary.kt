package org.spotube.spotube.core.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import org.spotube.plugin_interfaces.plugin_apis.metadata.playlist.MetadataPlaylist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import org.spotube.spotube.core.paths.Paths

/** A playlist as it was last seen online: enough to open it and play its downloads offline. */
@Serializable
data class OfflinePlaylist(
    val playlist: MetadataPlaylist,
    val tracks: List<MetadataTrack>
)

/**
 * Remembers every playlist opened with internet, so it can be opened again without it (Spotify-style
 * offline mode). Stored in one small JSON file in the app's data folder.
 */
object OfflineLibrary {
    private val fileSystem = FileSystem.SYSTEM
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()
    private val file by lazy { (Paths().getApplicationDataDirPath() + "/offline_cache/playlists.json").toPath() }

    private val state = MutableStateFlow<Map<String, OfflinePlaylist>>(emptyMap())
    private var loaded = false

    val playlists: StateFlow<Map<String, OfflinePlaylist>> = state

    init {
        scope.launch { ensureLoaded() }
    }

    suspend fun ensureLoaded() = mutex.withLock {
        if (loaded) return@withLock
        state.value = runCatching {
            if (!fileSystem.exists(file)) emptyMap()
            else json.decodeFromString<Map<String, OfflinePlaylist>>(
                fileSystem.source(file).buffer().use { it.readUtf8() }
            )
        }.getOrDefault(emptyMap())
        loaded = true
    }

    suspend fun get(id: String): OfflinePlaylist? {
        ensureLoaded()
        return state.value[id]
    }

    /** Saves what was just loaded; never shrinks a longer list already saved (paged loading). */
    fun remember(playlist: MetadataPlaylist, tracks: List<MetadataTrack>) {
        scope.launch {
            ensureLoaded()
            mutex.withLock {
                val existing = state.value[playlist.id]
                val keepTracks = if (existing != null && existing.tracks.size > tracks.size &&
                    existing.tracks.take(tracks.size).map { it.id } == tracks.map { it.id }
                ) existing.tracks else tracks
                val updated = state.value + (playlist.id to OfflinePlaylist(playlist, keepTracks))
                state.value = updated
                runCatching {
                    file.parent?.let { fileSystem.createDirectories(it) }
                    fileSystem.sink(file).buffer().use { it.writeUtf8(json.encodeToString(updated)) }
                }
            }
        }
    }
}
