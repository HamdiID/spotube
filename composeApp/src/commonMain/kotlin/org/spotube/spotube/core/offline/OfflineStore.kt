package org.spotube.spotube.core.offline

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import org.spotube.spotube.core.paths.Paths

/**
 * Keeps a copy on disk of what the plugin returned (library, playlists, albums, liked songs),
 * so those pages still open without internet, like Spotify's offline mode.
 */
object OfflineStore {
    private val paths = Paths()
    private val fileSystem = FileSystem.SYSTEM
    @PublishedApi
    internal val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val dir: Path by lazy { (paths.getApplicationDataDirPath() + "/offline_cache").toPath() }

    private fun fileFor(key: String): Path =
        dir / (key.map { if (it.isLetterOrDigit() || it == '-' || it == '_') it else '_' }
            .joinToString("").take(180) + ".json")

    /** Runs [fetch]; on success saves the result, on failure returns the last saved copy if any. */
    suspend inline fun <reified T> cached(key: String, noinline fetch: suspend () -> T): T =
        cached(key, serializer<T>(), fetch)

    suspend fun <T> cached(key: String, serializer: KSerializer<T>, fetch: suspend () -> T): T {
        try {
            val value = fetch()
            runCatching { write(key, serializer, value) }
            return value
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return read(key, serializer) ?: throw e
        }
    }

    private suspend fun <T> write(key: String, serializer: KSerializer<T>, value: T) =
        withContext(Dispatchers.IO) {
            fileSystem.createDirectories(dir)
            val text = json.encodeToString(serializer, value)
            fileSystem.sink(fileFor(key)).buffer().use { it.writeUtf8(text) }
        }

    private suspend fun <T> read(key: String, serializer: KSerializer<T>): T? =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = fileFor(key)
                if (!fileSystem.exists(file)) return@runCatching null
                val text = fileSystem.source(file).buffer().use { it.readUtf8() }
                json.decodeFromString(serializer, text)
            }.getOrNull()
        }
}
