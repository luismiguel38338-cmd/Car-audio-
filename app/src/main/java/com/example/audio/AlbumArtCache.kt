package com.example.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bounded LRU thumbnail cache for embedded audio cover art.
 * Runs safely off the main thread and guarantees MediaMetadataRetriever cleanup.
 */
object AlbumArtCache {
    private const val MAX_ENTRIES = 24
    private const val TARGET_SIZE_PX = 180

    private val cache = object : LruCache<String, Bitmap>(MAX_ENTRIES) {}
    private val failedUris = HashSet<String>()

    fun getCached(uri: Uri?): Bitmap? {
        val key = uri?.toString() ?: return null
        return synchronized(cache) { cache.get(key) }
    }

    suspend fun loadOrGet(context: Context, uri: Uri?): Bitmap? {
        val key = uri?.toString() ?: return null
        synchronized(cache) {
            cache.get(key)?.let { return it }
            if (failedUris.contains(key)) return null
        }

        val decoded = withContext(Dispatchers.IO) {
            var retriever: MediaMetadataRetriever? = null
            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val bytes = retriever.embeddedPicture ?: return@withContext null
                decodeSampledBitmap(bytes, TARGET_SIZE_PX, TARGET_SIZE_PX)
            } catch (_: Throwable) {
                null
            } finally {
                try {
                    retriever?.release()
                } catch (_: Throwable) {}
            }
        }

        synchronized(cache) {
            if (decoded != null) {
                cache.put(key, decoded)
            } else {
                if (failedUris.size > 200) failedUris.clear()
                failedUris.add(key)
            }
        }
        return decoded
    }

    private fun decodeSampledBitmap(bytes: ByteArray, reqW: Int, reqH: Int): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        var inSampleSize = 1
        if (opts.outHeight > reqH || opts.outWidth > reqW) {
            val halfH = opts.outHeight / 2
            val halfW = opts.outWidth / 2
            while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) {
                inSampleSize *= 2
            }
        }
        val finalOpts = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            this.inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, finalOpts)
    }

    fun clear() {
        synchronized(cache) {
            cache.evictAll()
            failedUris.clear()
        }
    }
}
