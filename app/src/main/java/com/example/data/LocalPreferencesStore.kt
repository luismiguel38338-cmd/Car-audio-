package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.AbSlot
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.CustomSoundProfile
import com.example.model.DspAbSnapshot
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.MusicPlaylist
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.model.YouTubeMusicItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * Device-local JSON/SharedPreferences store for DSP settings, A/B snapshots,
 * cached tracks, playlists, YouTube Music shortcuts, and UI flags.
 */
class LocalPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("car_audio_dsp_prefs", Context.MODE_PRIVATE)

    var introEnabled: Boolean
        get() = prefs.getBoolean("intro_video_enabled", false)
        set(value) = prefs.edit().putBoolean("intro_video_enabled", value).apply()

    var dontShowIntroAgain: Boolean
        get() = prefs.getBoolean("dont_show_again", true)
        set(value) = prefs.edit().putBoolean("dont_show_again", value).apply()

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", true)
        set(value) = prefs.edit().putBoolean("onboarding_completed", value).apply()

    var vipPromoDismissed: Boolean
        get() = prefs.getBoolean("vip_intro_dismissed", true)
        set(value) = prefs.edit().putBoolean("vip_intro_dismissed", value).apply()

    var isVipUnlocked: Boolean
        get() = prefs.getBoolean("vip_unlocked", true)
        set(value) = prefs.edit().putBoolean("vip_unlocked", value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean("keep_screen_on", true)
        set(value) = prefs.edit().putBoolean("keep_screen_on", value).apply()

    fun getLiteVisualsEnabled(defaultLowRam: Boolean): Boolean =
        prefs.getBoolean("lite_visuals_enabled", defaultLowRam)

    fun setLiteVisualsEnabled(value: Boolean) =
        prefs.edit().putBoolean("lite_visuals_enabled", value).apply()

    var shuffleEnabled: Boolean
        get() = prefs.getBoolean("shuffle_enabled", false)
        set(value) = prefs.edit().putBoolean("shuffle_enabled", value).apply()

    var lastTrackKey: String?
        get() = prefs.getString("last_track_key", null)
        set(value) = prefs.edit().putString("last_track_key", value).apply()

    var lastPositionSeconds: Int
        get() = prefs.getInt("last_position_sec", 0)
        set(value) = prefs.edit().putInt("last_position_sec", value.coerceAtLeast(0)).apply()

    var playbackSpeed: Float
        get() = prefs.getFloat("playback_speed", 1.0f).coerceIn(0.5f, 2.0f)
        set(value) = prefs.edit().putFloat("playback_speed", value.coerceIn(0.5f, 2.0f)).apply()

    var playbackPitch: Float
        get() = prefs.getFloat("playback_pitch", 1.0f).coerceIn(0.5f, 2.0f)
        set(value) = prefs.edit().putFloat("playback_pitch", value.coerceIn(0.5f, 2.0f)).apply()

    fun loadSortOrder(): com.example.model.LibrarySortOrder {
        val raw = prefs.getString("library_sort_order", com.example.model.LibrarySortOrder.RECENT.name)
        return try {
            com.example.model.LibrarySortOrder.valueOf(raw ?: com.example.model.LibrarySortOrder.RECENT.name)
        } catch (_: Throwable) {
            com.example.model.LibrarySortOrder.RECENT
        }
    }

    fun saveSortOrder(order: com.example.model.LibrarySortOrder) {
        prefs.edit().putString("library_sort_order", order.name).apply()
    }

    fun loadRecentTrackKeys(): List<String> {
        val raw = prefs.getString("recent_track_keys_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { idx -> arr.optString(idx, "") }.filter { it.isNotBlank() }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveRecentTrackKeys(keys: List<String>) {
        try {
            prefs.edit().putString("recent_track_keys_json", JSONArray(keys.take(25)).toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadPlayCounts(): Map<String, Int> {
        val raw = prefs.getString("track_play_counts_json", null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val out = LinkedHashMap<String, Int>()
            val iter = obj.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                val count = obj.optInt(key, 0)
                if (key.isNotBlank() && count > 0) {
                    out[key] = count
                }
            }
            out
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    fun savePlayCounts(counts: Map<String, Int>) {
        try {
            val obj = JSONObject()
            counts.entries
                .sortedByDescending { it.value }
                .take(250)
                .forEach { (k, v) -> if (v > 0) obj.put(k, v) }
            prefs.edit().putString("track_play_counts_json", obj.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadTheme(vipUnlocked: Boolean = true): CarAudioThemeType {
        val savedId = prefs.getString("selected_theme_id", CarAudioThemeType.GRAPHITE_STUDIO.id)
        return CarAudioThemeType.fromId(savedId)
    }

    fun saveThemeId(themeId: String) {
        prefs.edit().putString("selected_theme_id", themeId).apply()
    }

    fun loadRepeatMode(): RepeatMode {
        val raw = prefs.getString("repeat_mode", RepeatMode.ALL.name)
        return try {
            RepeatMode.valueOf(raw ?: RepeatMode.ALL.name)
        } catch (_: Throwable) {
            RepeatMode.ALL
        }
    }

    fun saveRepeatMode(mode: RepeatMode) {
        prefs.edit().putString("repeat_mode", mode.name).apply()
    }

    fun loadFavoriteIds(): Set<String> =
        prefs.getStringSet("favorite_track_ids", emptySet()) ?: emptySet()

    fun saveFavoriteIds(ids: Set<String>) {
        prefs.edit().putStringSet("favorite_track_ids", ids).apply()
    }

    fun loadQueueKeys(): List<String> {
        val raw = prefs.getString("saved_queue_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { idx -> arr.optString(idx, "") }.filter { it.isNotBlank() }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveQueueKeys(keys: List<String>) {
        try {
            prefs.edit().putString("saved_queue_json", JSONArray(keys).toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadSavedCustomTracks(favs: Set<String>): List<AudioTrackItem> {
        val raw = prefs.getString("saved_custom_tracks", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = ArrayList<AudioTrackItem>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val uriStr = obj.optString("uri", "")
                if (uriStr.isBlank()) continue
                val uri = Uri.parse(uriStr)
                val trackId = obj.optString("id", "saved_$i")
                list.add(
                    AudioTrackItem(
                        id = trackId,
                        title = obj.optString("title", "Audio Local"),
                        artist = obj.optString("artist", "Archivo Cargado"),
                        album = obj.optString("album", "Dispositivo"),
                        durationSeconds = obj.optInt("duration", 180),
                        category = TrackCategory.USER_CUSTOM,
                        frequencyDescription = "Archivo Local • DSP en Vivo",
                        customUri = uri,
                        isFavorite = favs.contains(trackId) || favs.contains(uriStr)
                    )
                )
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun persistCustomTrack(track: AudioTrackItem) {
        val uri = track.customUri ?: return
        try {
            val currentRaw = prefs.getString("saved_custom_tracks", "[]")
            val arr = JSONArray(currentRaw)
            val newArr = JSONArray()
            val entry = JSONObject().apply {
                put("id", track.id)
                put("uri", uri.toString())
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("duration", track.durationSeconds)
            }
            newArr.put(entry)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("uri") != uri.toString()) {
                    newArr.put(obj)
                }
            }
            prefs.edit().putString("saved_custom_tracks", newArr.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun removePersistedCustomTrack(uri: Uri) {
        try {
            val currentRaw = prefs.getString("saved_custom_tracks", "[]")
            val arr = JSONArray(currentRaw)
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("uri") != uri.toString()) {
                    newArr.put(obj)
                }
            }
            prefs.edit().putString("saved_custom_tracks", newArr.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadPlaylists(): List<MusicPlaylist> {
        val raw = prefs.getString("saved_playlists_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val idsArr = obj.optJSONArray("trackIds")
                val trackIds = if (idsArr != null) {
                    List(idsArr.length()) { j -> idsArr.optString(j, "") }.filter { it.isNotEmpty() }
                } else emptyList()
                MusicPlaylist(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    trackIds = trackIds
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun savePlaylists(list: List<MusicPlaylist>) {
        try {
            val arr = JSONArray()
            list.forEach { pl ->
                arr.put(
                    JSONObject().apply {
                        put("id", pl.id)
                        put("name", pl.name)
                        put("trackIds", JSONArray(pl.trackIds))
                    }
                )
            }
            prefs.edit().putString("saved_playlists_json", arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadDspSettings(vipUnlocked: Boolean = true): DspSettings {
        val raw = prefs.getString("saved_dsp_json", null) ?: return DspSettings()
        return try {
            dspFromJson(JSONObject(raw))
        } catch (_: Throwable) {
            DspSettings()
        }
    }

    fun saveDspSettings(dsp: DspSettings) {
        try {
            prefs.edit().putString("saved_dsp_json", dspToJson(dsp).toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadEqualizerSettings(): EqualizerSettings {
        val raw = prefs.getString("saved_eq_json", null) ?: return EqualizerSettings()
        return try {
            val o = JSONObject(raw)
            val arr31 = o.optJSONArray("b31")
            val b31 = if (arr31 != null && arr31.length() == 31) {
                List(31) { arr31.optDouble(it, 0.0).toFloat() }
            } else EqualizerSettings().bands31
            val arr15 = o.optJSONArray("b15")
            val b15 = if (arr15 != null && arr15.length() == 15) {
                List(15) { arr15.optDouble(it, 0.0).toFloat() }
            } else EqualizerSettings().bands15
            val arr10 = o.optJSONArray("b10")
            val b10 = if (arr10 != null && arr10.length() == 10) {
                List(10) { arr10.optDouble(it, 0.0).toFloat() }
            } else EqualizerSettings.bands10From31(b31)
            EqualizerSettings(
                isEnabled = o.optBoolean("en", true),
                masterGainDb = o.optDouble("mg", 0.0).toFloat(),
                bands10 = b10,
                bands31 = b31,
                bands15 = b15,
                activePresetName = o.optString("preset", "Estudio Equilibrado")
            )
        } catch (_: Throwable) {
            EqualizerSettings()
        }
    }

    fun saveEqualizerSettings(eq: EqualizerSettings) {
        try {
            val o = JSONObject().apply {
                put("en", eq.isEnabled)
                put("mg", eq.masterGainDb.toDouble())
                put("preset", eq.activePresetName)
                put("b10", JSONArray(eq.bands10.map { it.toDouble() }))
                put("b15", JSONArray(eq.bands15.map { it.toDouble() }))
                put("b31", JSONArray(eq.bands31.map { it.toDouble() }))
            }
            prefs.edit().putString("saved_eq_json", o.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadCustomSoundProfiles(vipUnlocked: Boolean = true): List<CustomSoundProfile> {
        val raw = prefs.getString("saved_sound_profiles_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val b10Arr = obj.optJSONArray("b10")
                val b15Arr = obj.optJSONArray("b15")
                val b31Arr = obj.optJSONArray("b31")
                val dspObj = obj.optJSONObject("dsp") ?: JSONObject()
                val b31 = if (b31Arr != null && b31Arr.length() == 31) {
                    List(31) { b31Arr.optDouble(it, 0.0).toFloat() }
                } else List(31) { 0f }
                val b10 = if (b10Arr != null && b10Arr.length() == 10) {
                    List(10) { b10Arr.optDouble(it, 0.0).toFloat() }
                } else EqualizerSettings.bands10From31(b31)
                CustomSoundProfile(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.optString("desc", ""),
                    createdAtMs = obj.optLong("createdAt", System.currentTimeMillis()),
                    dspSettings = dspFromJson(dspObj),
                    eqBands10 = b10,
                    eqBands15 = if (b15Arr != null && b15Arr.length() == 15) List(15) { b15Arr.optDouble(it, 0.0).toFloat() } else List(15) { 0f },
                    eqBands31 = b31
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveCustomSoundProfiles(list: List<CustomSoundProfile>) {
        try {
            val arr = JSONArray()
            list.forEach { p ->
                arr.put(
                    JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("desc", p.description)
                        put("createdAt", p.createdAtMs)
                        put("dsp", dspToJson(p.dspSettings))
                        put("b10", JSONArray(p.eqBands10.map { it.toDouble() }))
                        put("b15", JSONArray(p.eqBands15.map { it.toDouble() }))
                        put("b31", JSONArray(p.eqBands31.map { it.toDouble() }))
                    }
                )
            }
            prefs.edit().putString("saved_sound_profiles_json", arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadPresetsCatalog(): List<EqPresetItem> {
        val raw = prefs.getString("saved_custom_presets", null) ?: return EqPresetCatalog.presets
        return try {
            val arr = JSONArray(raw)
            val customList = List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val bArr = obj.getJSONArray("bands")
                val b31Arr = obj.optJSONArray("bands31")
                val b10Arr = obj.optJSONArray("bands10")
                val b31 = if (b31Arr != null && b31Arr.length() == 31) List(31) { b31Arr.optDouble(it, 0.0).toFloat() } else emptyList()
                val b10 = if (b10Arr != null && b10Arr.length() == 10) {
                    List(10) { b10Arr.optDouble(it, 0.0).toFloat() }
                } else if (b31.size == 31) {
                    EqualizerSettings.bands10From31(b31)
                } else List(10) { 0f }
                EqPresetItem(
                    name = obj.getString("name"),
                    description = obj.optString("desc", "Preset personalizado"),
                    bands10 = b10,
                    bands = List(bArr.length()) { bArr.getDouble(it).toFloat() },
                    bands31 = b31,
                    isCustom = true
                )
            }
            EqPresetCatalog.presets + customList
        } catch (_: Throwable) {
            EqPresetCatalog.presets
        }
    }

    fun saveCustomPresets(allPresets: List<EqPresetItem>) {
        try {
            val customOnly = allPresets.filter { it.isCustom && it.name != "Custom" }
            val arr = JSONArray()
            customOnly.forEach { p ->
                arr.put(
                    JSONObject().apply {
                        put("name", p.name)
                        put("desc", p.description)
                        put("bands10", JSONArray(p.bands10.map { it.toDouble() }))
                        put("bands", JSONArray(p.bands.map { it.toDouble() }))
                        put("bands31", JSONArray(p.bands31.map { it.toDouble() }))
                    }
                )
            }
            prefs.edit().putString("saved_custom_presets", arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    // =====================================================================
    // A/B Comparison Snapshots Persistence
    // =====================================================================
    fun loadAbSnapshot(slot: AbSlot): DspAbSnapshot? {
        val raw = prefs.getString("ab_snapshot_${slot.name}", null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val b10Arr = obj.optJSONArray("b10")
            val b31Arr = obj.optJSONArray("b31")
            val b31 = if (b31Arr != null && b31Arr.length() == 31) {
                List(31) { b31Arr.optDouble(it, 0.0).toFloat() }
            } else List(31) { 0f }
            val b10 = if (b10Arr != null && b10Arr.length() == 10) {
                List(10) { b10Arr.optDouble(it, 0.0).toFloat() }
            } else EqualizerSettings.bands10From31(b31)
            DspAbSnapshot(
                slot = slot,
                name = obj.optString("name", slot.label),
                dspSettings = dspFromJson(obj.optJSONObject("dsp") ?: JSONObject()),
                bands10 = b10,
                bands31 = b31,
                savedAtMs = obj.optLong("savedAt", System.currentTimeMillis())
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun saveAbSnapshot(slot: AbSlot, snapshot: DspAbSnapshot?) {
        try {
            val key = "ab_snapshot_${slot.name}"
            if (snapshot == null) {
                prefs.edit().remove(key).apply()
                return
            }
            val obj = JSONObject().apply {
                put("name", snapshot.name)
                put("savedAt", snapshot.savedAtMs)
                put("dsp", dspToJson(snapshot.dspSettings))
                put("b10", JSONArray(snapshot.bands10.map { it.toDouble() }))
                put("b31", JSONArray(snapshot.bands31.map { it.toDouble() }))
            }
            prefs.edit().putString(key, obj.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadActiveAbSlot(): AbSlot? {
        val raw = prefs.getString("active_ab_slot", null) ?: return null
        return try {
            AbSlot.valueOf(raw)
        } catch (_: Throwable) {
            null
        }
    }

    fun saveActiveAbSlot(slot: AbSlot?) {
        if (slot == null) prefs.edit().remove("active_ab_slot").apply()
        else prefs.edit().putString("active_ab_slot", slot.name).apply()
    }

    // =====================================================================
    // YouTube Music Saved Shortcuts & Search History Persistence
    // =====================================================================
    fun loadYouTubeMusicItems(): List<YouTubeMusicItem> {
        val raw = prefs.getString("yt_music_items_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                YouTubeMusicItem(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    artist = obj.optString("artist", ""),
                    category = obj.optString("category", "Búsqueda directa"),
                    customUrl = obj.optString("url", "").takeIf { it.isNotBlank() },
                    isFavorite = obj.optBoolean("fav", false),
                    addedAtMs = obj.optLong("addedAt", System.currentTimeMillis())
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveYouTubeMusicItems(items: List<YouTubeMusicItem>) {
        try {
            val arr = JSONArray()
            items.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("artist", item.artist)
                        put("category", item.category)
                        put("url", item.customUrl ?: "")
                        put("fav", item.isFavorite)
                        put("addedAt", item.addedAtMs)
                    }
                )
            }
            prefs.edit().putString("yt_music_items_json", arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadYouTubeSearchHistory(): List<String> {
        val raw = prefs.getString("yt_search_history_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i -> arr.optString(i, "") }.filter { it.isNotBlank() }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveYouTubeSearchHistory(history: List<String>) {
        try {
            prefs.edit().putString("yt_search_history_json", JSONArray(history).toString()).apply()
        } catch (_: Throwable) {}
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun dspFromJson(o: JSONObject): DspSettings {
        return DspSettings(
            dspMasterEnabled = o.optBoolean("master", true),
            preampDb = o.optDouble("preamp", 0.0).toFloat(),
            bassGainDb = o.optDouble("bass", 1.5).toFloat(),
            midGainDb = o.optDouble("mid", 0.0).toFloat(),
            trebleGainDb = o.optDouble("treble", 1.0).toFloat(),
            masterGainDb = o.optDouble("masterGain", 0.0).toFloat(),
            bassBoostEnabled = o.optBoolean("bbEn", true),
            bassBoostDb = o.optDouble("bbDb", 3.0).toFloat(),
            bassBoostFreqHz = o.optDouble("bbHz", 48.0).toFloat(),
            subBassEnabled = o.optBoolean("subEn", false),
            subBassBoostDb = o.optDouble("subDb", 3.0).toFloat(),
            subBassFreqHz = o.optDouble("subHz", 35.0).toFloat(),
            subsonicFilterActive = o.optBoolean("subsonicAct", false),
            subsonicFrequencyHz = o.optDouble("subsonicHz", 26.0).toFloat(),
            bassPunchLevel = o.optDouble("punch", 0.0).toFloat(),
            loudnessEnabled = o.optBoolean("loudEn", false),
            loudnessGainDb = o.optDouble("loudDb", 3.5).toFloat(),
            stereoWidthPercent = o.optDouble("stereo", 100.0).toFloat(),
            balancePan = o.optDouble("pan", 0.0).toFloat(),
            compressorEnabled = o.optBoolean("comp", false),
            limiterEnabled = o.optBoolean("lim", true),
            limiterCeilingDb = o.optDouble("limCeil", -0.5).toFloat(),
            hpfEnabled = o.optBoolean("hpfEn", true),
            hpfFrequencyHz = o.optDouble("hpfHz", 24.0).toFloat(),
            hpfSlopeDb = o.optInt("hpfSlope", 24),
            lpfEnabled = o.optBoolean("lpfEn", false),
            lpfFrequencyHz = o.optDouble("lpfHz", 19000.0).toFloat(),
            lpfSlopeDb = o.optInt("lpfSlope", 24),
            hiResQualityEnabled = o.optBoolean("hiRes", false),
            tubeWarmthLevel = o.optDouble("tube", 0.0).toFloat(),
            presenceEnabled = o.optBoolean("presEn", false),
            presenceLevel = o.optDouble("presLvl", 2.0).toFloat(),
            clarityEnabled = o.optBoolean("clarEn", false),
            clarityLevel = o.optDouble("clarLvl", 2.5).toFloat(),
            exciterEnabled = o.optBoolean("excEn", false),
            exciterLevel = o.optDouble("excLvl", 3.0).toFloat()
        )
    }

    private fun dspToJson(dsp: DspSettings): JSONObject {
        return JSONObject().apply {
            put("master", dsp.dspMasterEnabled)
            put("preamp", dsp.preampDb.toDouble())
            put("bass", dsp.bassGainDb.toDouble())
            put("mid", dsp.midGainDb.toDouble())
            put("treble", dsp.trebleGainDb.toDouble())
            put("masterGain", dsp.masterGainDb.toDouble())
            put("bbEn", dsp.bassBoostEnabled)
            put("bbDb", dsp.bassBoostDb.toDouble())
            put("bbHz", dsp.bassBoostFreqHz.toDouble())
            put("subEn", dsp.subBassEnabled)
            put("subDb", dsp.subBassBoostDb.toDouble())
            put("subHz", dsp.subBassFreqHz.toDouble())
            put("subsonicAct", dsp.subsonicFilterActive)
            put("subsonicHz", dsp.subsonicFrequencyHz.toDouble())
            put("punch", dsp.bassPunchLevel.toDouble())
            put("loudEn", dsp.loudnessEnabled)
            put("loudDb", dsp.loudnessGainDb.toDouble())
            put("stereo", dsp.stereoWidthPercent.toDouble())
            put("pan", dsp.balancePan.toDouble())
            put("comp", dsp.compressorEnabled)
            put("lim", dsp.limiterEnabled)
            put("limCeil", dsp.limiterCeilingDb.toDouble())
            put("hpfEn", dsp.hpfEnabled)
            put("hpfHz", dsp.hpfFrequencyHz.toDouble())
            put("hpfSlope", dsp.hpfSlopeDb)
            put("lpfEn", dsp.lpfEnabled)
            put("lpfHz", dsp.lpfFrequencyHz.toDouble())
            put("lpfSlope", dsp.lpfSlopeDb)
            put("hiRes", dsp.hiResQualityEnabled)
            put("tube", dsp.tubeWarmthLevel.toDouble())
            put("presEn", dsp.presenceEnabled)
            put("presLvl", dsp.presenceLevel.toDouble())
            put("clarEn", dsp.clarityEnabled)
            put("clarLvl", dsp.clarityLevel.toDouble())
            put("excEn", dsp.exciterEnabled)
            put("excLvl", dsp.exciterLevel.toDouble())
        }
    }
}
