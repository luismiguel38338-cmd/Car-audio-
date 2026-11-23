package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdSlotManager
import com.example.data.LocalPreferencesStore
import com.example.downloads.DownloadManagerRepository
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.EqPresetCatalog
import com.example.model.OnlineTrackItem
import com.example.online.JamendoMusicProvider
import com.example.online.OnlineCatalogResult
import com.example.viewmodel.CarAudioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CarAudioProModulesTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("car_audio_dsp_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        app.getSharedPreferences("car_audio_downloads_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun `preset catalog contains all 10 required latin and studio genre presets`() {
        val names = EqPresetCatalog.presets.map { it.name }.toSet()
        val required = listOf(
            "Dembow dominicano",
            "Reggaetón",
            "Bachata",
            "Merengue",
            "Trap",
            "Hip-Hop",
            "Electrónica",
            "Rock",
            "Música vocal",
            "Personalizado"
        )
        for (presetName in required) {
            assertTrue("Missing preset: $presetName", presetName in names)
        }
        assertTrue(
            EqPresetCatalog.presets.all {
                it.bands10.size == 10 && it.bands31.size == 31
            }
        )
    }

    @Test
    fun `bass mode extreme forces anti clipping limiter enabled and persists`() {
        val vm = CarAudioViewModel(app)
        vm.toggleLimiter(false)
        assertFalse(vm.dspSettings.value.limiterEnabled)

        vm.setBassMode(BassMode.EXTREME)
        assertEquals(BassMode.EXTREME, vm.bassMode.value)
        assertTrue(vm.dspSettings.value.bassBoostEnabled)
        assertTrue(vm.dspSettings.value.subBassEnabled)
        assertTrue(vm.dspSettings.value.limiterEnabled)

        val vm2 = CarAudioViewModel(app)
        assertEquals(BassMode.EXTREME, vm2.bassMode.value)
        assertTrue(vm2.dspSettings.value.limiterEnabled)
    }

    @Test
    fun `jamendo provider rejects unconfigured placeholder and parses official payload without mock tracks`() = runBlocking {
        val unconfigured = JamendoMusicProvider(clientIdProvider = { "" })
        assertFalse(unconfigured.isConfigured())
        val unconfiguredResult = unconfigured.searchCatalog("dembow")
        assertTrue(unconfiguredResult is OnlineCatalogResult.Error)
        assertTrue((unconfiguredResult as OnlineCatalogResult.Error).isConfigurationIssue)

        val sampleJson = """
            {
              "headers": { "status": "success", "code": 0 },
              "results": [
                {
                  "id": "12345",
                  "name": "Noche Urbana CC",
                  "artist_name": "Productor RD",
                  "album_name": "Sesión Abierta",
                  "duration": 195,
                  "album_image": "https://usercontent.jamendo.com/cover.jpg",
                  "audio": "https://mp3d.jamendo.com/stream/12345.mp3",
                  "audiodownload": "https://mp3d.jamendo.com/download/12345.mp3",
                  "audiodownload_allowed": true,
                  "license_ccurl": "https://creativecommons.org/licenses/by-nc/4.0/"
                },
                {
                  "id": "67890",
                  "name": "Solo Streaming",
                  "artist_name": "DJ Invitado",
                  "album_name": "Single",
                  "duration": 180,
                  "audio": "https://mp3d.jamendo.com/stream/67890.mp3",
                  "audiodownload": "",
                  "audiodownload_allowed": false,
                  "license_ccurl": ""
                }
              ]
            }
        """.trimIndent()

        val configured = JamendoMusicProvider(
            clientIdProvider = { "valid_test_id" },
            httpGet = { 200 to sampleJson }
        )
        assertTrue(configured.isConfigured())
        val result = configured.searchCatalog("urbana")
        assertTrue(result is OnlineCatalogResult.Success)
        val payload = (result as OnlineCatalogResult.Success).data
        assertEquals(2, payload.tracks.size)
        assertTrue(payload.tracks[0].downloadAllowed)
        assertEquals("https://creativecommons.org/licenses/by-nc/4.0/", payload.tracks[0].licenseUrl)
        assertFalse(payload.tracks[1].downloadAllowed)
    }

    @Test
    fun `download repository blocks unlicensed tracks and tracks queue pause cancel transitions`() {
        val repo = DownloadManagerRepository(app, CoroutineScope(SupervisorJob() + Dispatchers.IO))

        val streamOnlyTrack = OnlineTrackItem(
            id = "stream_only",
            title = "Stream Only",
            artist = "Artist",
            album = "Single",
            durationSeconds = 180,
            artworkUrl = null,
            streamUrl = "https://example.com/stream.mp3",
            downloadUrl = null,
            downloadAllowed = false,
            licenseUrl = null
        )
        val blockedResult = repo.enqueueAuthorizedDownload(streamOnlyTrack)
        assertTrue(blockedResult.isFailure)
        assertTrue(repo.downloads.value.isEmpty())

        val licensedTrack = OnlineTrackItem(
            id = "cc_track_1",
            title = "Licensed Track",
            artist = "LM Studio",
            album = "Open CC",
            durationSeconds = 200,
            artworkUrl = null,
            streamUrl = "https://127.0.0.1:9/stream.mp3",
            downloadUrl = "https://127.0.0.1:9/dl.mp3",
            downloadAllowed = true,
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/"
        )
        val enqueued = repo.enqueueAuthorizedDownload(licensedTrack)
        assertTrue(enqueued.isSuccess)
        val dlId = enqueued.getOrThrow().id
        assertEquals(1, repo.downloads.value.size)

        repo.pauseDownload(dlId)
        val afterPause = repo.downloads.value.first().status
        assertTrue(afterPause == com.example.model.DownloadStatus.PAUSED || afterPause == com.example.model.DownloadStatus.FAILED)

        repo.cancelDownload(dlId)
        assertEquals(com.example.model.DownloadStatus.CANCELLED, repo.downloads.value.first().status)

        repo.deleteDownload(dlId)
        assertTrue(repo.downloads.value.isEmpty())
    }

    @Test
    fun `local preferences store persists profile name listening seconds and bass mode`() {
        val store = LocalPreferencesStore(app)
        assertEquals(CarAudioThemeType.NEON_DSP_PRO, store.loadTheme())

        store.saveBassMode(BassMode.POWERFUL)
        store.localProfileName = "Cabina Civic Touring"
        store.totalListeningSeconds = 4250L

        val storeReloaded = LocalPreferencesStore(app)
        assertEquals(BassMode.POWERFUL, storeReloaded.loadBassMode())
        assertEquals("Cabina Civic Touring", storeReloaded.localProfileName)
        assertEquals(4250L, storeReloaded.totalListeningSeconds)
    }

    @Test
    fun `ad slot manager stays disabled to comply with play creator program`() {
        assertFalse(AdSlotManager.IS_AD_SDK_BUNDLED)
        assertFalse(AdSlotManager.slotConfigs.any { it.isSdkEnabled })
    }
}
