package com.example.online

import com.example.BuildConfig
import com.example.model.OnlineAlbumItem
import com.example.model.OnlineArtistItem
import com.example.model.OnlineTrackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Official Jamendo API v3 client using client-scoped BuildConfig.JAMENDO_CLIENT_ID.
 * Returns only real API results — never invents fake tracks or scrapes unauthorized services.
 */
class JamendoMusicProvider(
    private val clientIdProvider: () -> String = { resolveConfiguredClientId() },
    private val httpGet: (String) -> Pair<Int, String> = { url -> executeHttpGet(url) }
) : OnlineMusicProvider {

    override val providerName: String = "Jamendo Music (API Oficial v3)"

    override fun isConfigured(): Boolean = clientIdProvider().isNotBlank()

    override suspend fun searchCatalog(query: String): OnlineCatalogResult<OnlineCatalogSearchPayload> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return fetchFeaturedCatalog()
        return queryJamendo(cleanQuery, featured = false)
    }

    override suspend fun fetchFeaturedCatalog(): OnlineCatalogResult<OnlineCatalogSearchPayload> =
        queryJamendo(query = "", featured = true)

    private suspend fun queryJamendo(
        query: String,
        featured: Boolean
    ): OnlineCatalogResult<OnlineCatalogSearchPayload> = withContext(Dispatchers.IO) {
        val clientId = clientIdProvider()
        if (clientId.isBlank()) {
            return@withContext OnlineCatalogResult.Error(
                message = "Configura tu clave JAMENDO_CLIENT_ID en el panel Secrets de AI Studio para explorar y reproducir música legal libre de derechos.",
                isConfigurationIssue = true
            )
        }

        try {
            val encodedClientId = URLEncoder.encode(clientId, "UTF-8")
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val tracksUrl = if (featured || query.isBlank()) {
                "$BASE_URL/tracks/?client_id=$encodedClientId&format=json&limit=25&order=popularity_week&audioformat=mp32&imagesize=400"
            } else {
                "$BASE_URL/tracks/?client_id=$encodedClientId&format=json&limit=25&namesearch=$encodedQuery&audioformat=mp32&imagesize=400"
            }

            val (tracksCode, tracksBody) = httpGet(tracksUrl)
            if (tracksCode !in 200..299) {
                return@withContext OnlineCatalogResult.Error(
                    message = "Error de conexión con Jamendo (HTTP $tracksCode). Verifica tu conexión o clave JAMENDO_CLIENT_ID."
                )
            }

            val parsedTracks = parseTracksResponse(tracksBody)

            val artistsUrl = if (featured || query.isBlank()) {
                "$BASE_URL/artists/?client_id=$encodedClientId&format=json&limit=12&order=popularity_week"
            } else {
                "$BASE_URL/artists/?client_id=$encodedClientId&format=json&limit=12&namesearch=$encodedQuery"
            }
            val parsedArtists = runCatching {
                val (code, body) = httpGet(artistsUrl)
                if (code in 200..299) parseArtistsResponse(body) else emptyList()
            }.getOrDefault(emptyList())

            val albumsUrl = if (featured || query.isBlank()) {
                "$BASE_URL/albums/?client_id=$encodedClientId&format=json&limit=12&order=popularity_week&imagesize=400"
            } else {
                "$BASE_URL/albums/?client_id=$encodedClientId&format=json&limit=12&namesearch=$encodedQuery&imagesize=400"
            }
            val parsedAlbums = runCatching {
                val (code, body) = httpGet(albumsUrl)
                if (code in 200..299) parseAlbumsResponse(body) else emptyList()
            }.getOrDefault(emptyList())

            OnlineCatalogResult.Success(
                OnlineCatalogSearchPayload(
                    tracks = parsedTracks,
                    artists = parsedArtists,
                    albums = parsedAlbums
                )
            )
        } catch (t: Throwable) {
            OnlineCatalogResult.Error(
                message = "No se pudo consultar Jamendo: ${t.localizedMessage ?: "Error de red"}"
            )
        }
    }

    companion object {
        private const val BASE_URL = "https://api.jamendo.com/v3.0"

        fun resolveConfiguredClientId(): String {
            val raw = runCatching { BuildConfig.JAMENDO_CLIENT_ID }.getOrDefault("").trim()
            if (raw.isBlank() ||
                raw.equals("MY_JAMENDO_CLIENT_ID", ignoreCase = true) ||
                raw.equals("null", ignoreCase = true)
            ) {
                return ""
            }
            return raw
        }

        fun parseTracksResponse(jsonRaw: String): List<OnlineTrackItem> {
            val root = JSONObject(jsonRaw)
            val headers = root.optJSONObject("headers")
            val status = headers?.optString("status", "success")
            if (status == "failed") {
                val errorMsg = headers.optString("error_message", "Error de API Jamendo")
                throw IllegalStateException(errorMsg)
            }
            val results = root.optJSONArray("results") ?: return emptyList()
            val out = ArrayList<OnlineTrackItem>(results.length())
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val streamUrl = item.optString("audio", "").trim()
                if (streamUrl.isBlank()) continue
                val downloadUrl = item.optString("audiodownload", "").trim().takeIf { it.isNotBlank() }
                val licenseUrl = item.optString("license_ccurl", "").trim().takeIf { it.isNotBlank() }
                val allowed = item.optBoolean("audiodownload_allowed", false) &&
                    downloadUrl != null &&
                    licenseUrl != null

                out.add(
                    OnlineTrackItem(
                        id = item.optString("id", "track_$i"),
                        title = item.optString("name", "Pista sin título").ifBlank { "Pista sin título" },
                        artist = item.optString("artist_name", "Artista independiente").ifBlank { "Artista independiente" },
                        album = item.optString("album_name", "Jamendo").ifBlank { "Jamendo" },
                        durationSeconds = item.optInt("duration", 0).coerceAtLeast(0),
                        artworkUrl = item.optString("album_image", "").trim().takeIf { it.isNotBlank() }
                            ?: item.optString("image", "").trim().takeIf { it.isNotBlank() },
                        streamUrl = streamUrl,
                        downloadUrl = downloadUrl,
                        downloadAllowed = allowed,
                        licenseUrl = licenseUrl,
                        sourceName = "Jamendo"
                    )
                )
            }
            return out
        }

        fun parseArtistsResponse(jsonRaw: String): List<OnlineArtistItem> {
            val root = JSONObject(jsonRaw)
            val results = root.optJSONArray("results") ?: return emptyList()
            val out = ArrayList<OnlineArtistItem>(results.length())
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val name = item.optString("name", "").trim()
                if (name.isBlank()) continue
                out.add(
                    OnlineArtistItem(
                        id = item.optString("id", "artist_$i"),
                        name = name,
                        imageUrl = item.optString("image", "").trim().takeIf { it.isNotBlank() },
                        shareUrl = item.optString("shareurl", "").trim().takeIf { it.isNotBlank() }
                    )
                )
            }
            return out
        }

        fun parseAlbumsResponse(jsonRaw: String): List<OnlineAlbumItem> {
            val root = JSONObject(jsonRaw)
            val results = root.optJSONArray("results") ?: return emptyList()
            val out = ArrayList<OnlineAlbumItem>(results.length())
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val title = item.optString("name", "").trim()
                if (title.isBlank()) continue
                out.add(
                    OnlineAlbumItem(
                        id = item.optString("id", "album_$i"),
                        title = title,
                        artist = item.optString("artist_name", "Artista").ifBlank { "Artista" },
                        imageUrl = item.optString("image", "").trim().takeIf { it.isNotBlank() },
                        releaseDate = item.optString("releasedate", "").trim().takeIf { it.isNotBlank() }
                    )
                )
            }
            return out
        }

        private fun executeHttpGet(urlString: String): Pair<Int, String> {
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 12000
                setRequestProperty("Accept", "application/json")
            }
            return try {
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
                code to body
            } finally {
                conn.disconnect()
            }
        }
    }
}
