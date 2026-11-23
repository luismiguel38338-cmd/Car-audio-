package com.example.online

import com.example.model.OnlineAlbumItem
import com.example.model.OnlineArtistItem
import com.example.model.OnlineTrackItem

sealed class OnlineCatalogResult<out T> {
    data class Success<T>(val data: T) : OnlineCatalogResult<T>()
    data class Error(val message: String, val isConfigurationIssue: Boolean = false) : OnlineCatalogResult<Nothing>()
}

data class OnlineCatalogSearchPayload(
    val tracks: List<OnlineTrackItem>,
    val artists: List<OnlineArtistItem>,
    val albums: List<OnlineAlbumItem>
)

/**
 * Modular provider interface for authorized legal online music discovery and streaming.
 */
interface OnlineMusicProvider {
    val providerName: String
    fun isConfigured(): Boolean
    suspend fun searchCatalog(query: String): OnlineCatalogResult<OnlineCatalogSearchPayload>
    suspend fun fetchFeaturedCatalog(): OnlineCatalogResult<OnlineCatalogSearchPayload>
}
