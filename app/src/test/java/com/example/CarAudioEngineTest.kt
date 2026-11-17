package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.CarAudioEngine
import com.example.model.AbSlot
import com.example.model.AppSection
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.FaultSeverity
import com.example.model.LibrarySortOrder
import com.example.model.ParametricBand
import com.example.model.ParametricFilterType
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.model.TutorialCatalog
import com.example.ui.components.StudioLedColors
import com.example.viewmodel.CarAudioViewModel
import com.example.viewmodel.LibraryFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CarAudioEngineTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("car_audio_dsp_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `iso 31 band upper cutoff frequencies increase monotonically`() {
        val freqs = EqualizerSettings.FREQUENCIES_HZ_31
        var prevCutoff = 0f
        for (i in freqs.indices) {
            val cutoff = CarAudioEngine.computeIsoUpperCutoffHz(i, freqs)
            assertTrue("Cutoff at index $i ($cutoff) must exceed center ${freqs[i]}", cutoff > freqs[i])
            assertTrue("Cutoff at index $i ($cutoff) must exceed previous ($prevCutoff)", cutoff > prevCutoff)
            prevCutoff = cutoff
        }
        assertEquals(22000f, prevCutoff, 0.01f)
    }

    @Test
    fun `dsp bypass returns zero effective band gain`() {
        val dsp = DspSettings(
            dspMasterEnabled = false,
            bassGainDb = 12f,
            preampDb = 10f,
            hpfEnabled = true,
            hpfFrequencyHz = 120f
        )
        val eq = EqualizerSettings(isEnabled = true, bands31 = List(31) { 9f }, masterGainDb = 6f)
        for (f in EqualizerSettings.FREQUENCIES_HZ_31) {
            assertEquals(0f, CarAudioEngine.computeEffectiveBandGainDb(f, dsp, eq), 0.001f)
        }
    }

    @Test
    fun `butterworth hpf and lpf attenuate out of band frequencies`() {
        val dsp = DspSettings(
            dspMasterEnabled = true,
            bassGainDb = 0f,
            midGainDb = 0f,
            trebleGainDb = 0f,
            bassBoostEnabled = false,
            hpfEnabled = true,
            hpfFrequencyHz = 80f,
            hpfSlopeDb = 24,
            lpfEnabled = true,
            lpfFrequencyHz = 2000f,
            lpfSlopeDb = 24
        )
        val flatEq = EqualizerSettings(
            isEnabled = false,
            bands10 = List(10) { 0f },
            bands15 = List(15) { 0f },
            bands31 = List(31) { 0f },
            masterGainDb = 0f
        )

        val gain20Hz = CarAudioEngine.computeEffectiveBandGainDb(20, dsp, flatEq)
        val gain500Hz = CarAudioEngine.computeEffectiveBandGainDb(500, dsp, flatEq)
        val gain16kHz = CarAudioEngine.computeEffectiveBandGainDb(16000, dsp, flatEq)

        assertTrue("20Hz ($gain20Hz dB) should be deeply attenuated by 80Hz HPF", gain20Hz <= -17.5f)
        assertTrue("500Hz ($gain500Hz dB) should be near 0 dB in passband", gain500Hz > -1.0f && gain500Hz < 1.0f)
        assertTrue("16kHz ($gain16kHz dB) should be deeply attenuated by 2kHz LPF", gain16kHz <= -17.5f)
    }

    @Test
    fun `parametric peaking notch and shelf filters shape gain curve smoothly`() {
        val dsp = DspSettings(
            dspMasterEnabled = true,
            bassGainDb = 0f,
            midGainDb = 0f,
            trebleGainDb = 0f,
            bassBoostEnabled = false,
            hpfEnabled = false,
            lpfEnabled = false
        )
        val eq = EqualizerSettings(
            isEnabled = true,
            masterGainDb = 0f,
            bands10 = List(10) { 0f },
            bands31 = List(31) { 0f },
            parametricBands = listOf(
                ParametricBand(1, enabled = true, freqHz = 1000f, gainDb = 6f, q = 2.0f, filterType = ParametricFilterType.PEAKING),
                ParametricBand(2, enabled = true, freqHz = 250f, gainDb = 8f, q = 2.5f, filterType = ParametricFilterType.NOTCH),
                ParametricBand(3, enabled = true, freqHz = 80f, gainDb = 5f, q = 1.0f, filterType = ParametricFilterType.LOW_SHELF),
                ParametricBand(4, enabled = true, freqHz = 10000f, gainDb = 4f, q = 1.0f, filterType = ParametricFilterType.HIGH_SHELF)
            )
        )

        val peakAt1k = CarAudioEngine.computeEffectiveBandGainDb(1000, dsp, eq)
        val notchAt250 = CarAudioEngine.computeEffectiveBandGainDb(250, dsp, eq)
        val lowShelfAt25 = CarAudioEngine.computeEffectiveBandGainDb(25, dsp, eq)
        val highShelfAt16k = CarAudioEngine.computeEffectiveBandGainDb(16000, dsp, eq)

        assertTrue("Peaking 1kHz expected > +5 dB, got $peakAt1k", peakAt1k > 5.0f)
        assertTrue("Notch 250Hz expected < -6 dB, got $notchAt250", notchAt250 < -6.0f)
        assertTrue("Low shelf 25Hz expected > +3.5 dB, got $lowShelfAt25", lowShelfAt25 > 3.5f)
        assertTrue("High shelf 16kHz expected > +2.5 dB, got $highShelfAt16k", highShelfAt16k > 2.5f)
    }

    @Test
    fun `anti double processing prevents simultaneous dynamicsprocessing and legacy effects`() {
        val dpPlan = CarAudioEngine.resolveAntiDoubleProcessingPlan(
            useDynamicsProcessing = true,
            hasEqualizer = true,
            hasBassBoost = true,
            hasVirtualizer = true
        )
        assertTrue(dpPlan.antiDoubleProcessingSafe)
        assertTrue(dpPlan.dynamicsProcessingActive)
        assertFalse(dpPlan.usingFallbackEqualizer)
        assertFalse(dpPlan.bassBoostActive)

        val fallbackPlan = CarAudioEngine.resolveAntiDoubleProcessingPlan(
            useDynamicsProcessing = false,
            hasEqualizer = true,
            hasBassBoost = true,
            hasVirtualizer = true
        )
        assertTrue(fallbackPlan.antiDoubleProcessingSafe)
        assertFalse(fallbackPlan.dynamicsProcessingActive)
        assertTrue(fallbackPlan.usingFallbackEqualizer)
        assertTrue(fallbackPlan.bassBoostActive)
    }

    @Test
    fun `bass boost included in curve only when dynamics processing owns low end`() {
        val flatEq = EqualizerSettings(isEnabled = false, bands10 = List(10) { 0f }, bands31 = List(31) { 0f })
        val dsp = DspSettings(
            dspMasterEnabled = true,
            bassGainDb = 0f,
            midGainDb = 0f,
            trebleGainDb = 0f,
            bassBoostEnabled = true,
            bassBoostDb = 10f,
            bassBoostFreqHz = 50f,
            hpfEnabled = false,
            lpfEnabled = false
        )
        val gainWithCurve = CarAudioEngine.computeEffectiveBandGainDb(50, dsp, flatEq, includeBassBoostInCurve = true)
        val gainWithoutCurve = CarAudioEngine.computeEffectiveBandGainDb(50, dsp, flatEq, includeBassBoostInCurve = false)
        assertTrue("Expected gain in curve > 3 dB, got $gainWithCurve", gainWithCurve > 3.0f)
        assertEquals(0f, gainWithoutCurve, 0.05f)
    }

    @Test
    fun `studio signal bar labels and commercial tutorial catalog are complete`() {
        assertEquals(listOf("SUB", "GRAVE", "MEDIO", "PRES", "AGUD"), StudioLedColors.bandLabels)
        assertEquals(10, TutorialCatalog.topics.size)
        assertTrue(TutorialCatalog.topics.all { it.steps.isNotEmpty() && it.proTip.isNotBlank() })
    }

    @Test
    fun `all dsp features and automotive themes are unlocked and launch opens directly`() {
        val vm = CarAudioViewModel(app)
        assertTrue(vm.isVipUnlocked.value)
        assertFalse(vm.showSplashScreen.value)

        vm.setHiResQuality(true)
        assertTrue(vm.dspSettings.value.hiResQualityEnabled)

        vm.setTheme(CarAudioThemeType.OBSIDIAN_STUDIO_VIP)
        assertEquals(CarAudioThemeType.OBSIDIAN_STUDIO_VIP, vm.currentTheme.value)

        val vm2 = CarAudioViewModel(app)
        assertTrue(vm2.isVipUnlocked.value)
        assertFalse(vm2.showSplashScreen.value)
        assertEquals(CarAudioThemeType.OBSIDIAN_STUDIO_VIP, vm2.currentTheme.value)
        assertTrue(vm2.dspSettings.value.hiResQualityEnabled)
    }

    @Test
    fun `ab snapshot comparison captures applies swaps and persists`() {
        val vm = CarAudioViewModel(app)
        vm.setDspBassGain(2.0f)
        vm.updateEqualizerBand10(0, 3.0f)
        vm.captureAbSlot(AbSlot.A)

        vm.setDspBassGain(8.5f)
        vm.updateEqualizerBand10(0, 7.5f)
        vm.captureAbSlot(AbSlot.B)

        assertNotNull(vm.abSlotA.value)
        assertNotNull(vm.abSlotB.value)
        assertEquals(AbSlot.B, vm.activeAbSlot.value)

        vm.toggleAbComparison()
        assertEquals(AbSlot.A, vm.activeAbSlot.value)
        assertEquals(2.0f, vm.dspSettings.value.bassGainDb, 0.01f)
        assertEquals(3.0f, vm.equalizerSettings.value.bands10[0], 0.01f)

        vm.toggleAbComparison()
        assertEquals(AbSlot.B, vm.activeAbSlot.value)
        assertEquals(8.5f, vm.dspSettings.value.bassGainDb, 0.01f)
        assertEquals(7.5f, vm.equalizerSettings.value.bands10[0], 0.01f)

        val vm2 = CarAudioViewModel(app)
        assertNotNull(vm2.abSlotA.value)
        assertNotNull(vm2.abSlotB.value)
        assertEquals(AbSlot.B, vm2.activeAbSlot.value)
        assertEquals(2.0f, vm2.abSlotA.value!!.dspSettings.bassGainDb, 0.01f)
        assertEquals(8.5f, vm2.abSlotB.value!!.dspSettings.bassGainDb, 0.01f)
    }

    @Test
    fun `youtube music shortcuts favorites and search history persist and build official urls`() {
        val vm = CarAudioViewModel(app)
        vm.recordYtSearch("Bass Test 35Hz")
        vm.saveYtShortcut(
            title = "Subwoofer Demo 40Hz",
            artist = "Car Audio",
            customUrl = null,
            favorite = true
        )

        val searchUri = vm.buildYouTubeMusicSearchUri("Bass Test 35Hz").toString()
        assertTrue(searchUri.startsWith("https://music.youtube.com/search?q="))
        assertTrue(vm.ytSearchHistory.value.contains("Bass Test 35Hz"))
        assertEquals(1, vm.ytSavedItems.value.size)
        assertTrue(vm.ytSavedItems.value.first().isFavorite)

        val savedId = vm.ytSavedItems.value.first().id
        val vm2 = CarAudioViewModel(app)
        assertTrue(vm2.ytSearchHistory.value.contains("Bass Test 35Hz"))
        assertEquals(1, vm2.ytSavedItems.value.size)
        assertTrue(vm2.ytSavedItems.value.first().isFavorite)

        vm2.toggleYtFavorite(savedId)
        assertFalse(vm2.ytSavedItems.value.first().isFavorite)
        vm2.removeYtItem(savedId)
        assertTrue(vm2.ytSavedItems.value.isEmpty())
    }

    @Test
    fun `queue operations artists and albums work coherently`() {
        val vm = CarAudioViewModel(app)
        val t1 = AudioTrackItem("t1", "Pista Uno", artist = "Artista A", album = "Álbum X", durationSeconds = 180)
        val t2 = AudioTrackItem("t2", "Pista Dos", artist = "Artista B", album = "Álbum Y", durationSeconds = 200)
        val t3 = AudioTrackItem("t3", "Pista Tres", artist = "Artista A", album = "Álbum X", durationSeconds = 210)

        vm.addToQueue(t1, silent = true)
        vm.addToQueue(t2, silent = true)
        vm.playNextInQueue(t3)

        assertEquals(3, vm.queueTracks.value.size)
        assertEquals("t3", vm.queueTracks.value[0].id)
        assertEquals("t1", vm.queueTracks.value[1].id)
        assertEquals("t2", vm.queueTracks.value[2].id)

        vm.moveQueueItemDown(0)
        assertEquals("t1", vm.queueTracks.value[0].id)
        assertEquals("t3", vm.queueTracks.value[1].id)

        vm.moveQueueItemUp(2)
        assertEquals("t2", vm.queueTracks.value[1].id)

        vm.removeFromQueue(1)
        assertEquals(2, vm.queueTracks.value.size)

        vm.clearQueue()
        assertTrue(vm.queueTracks.value.isEmpty())
    }

    @Test
    fun `playlists and custom sound profiles persist independently`() {
        val vm = CarAudioViewModel(app)
        vm.createPlaylist("Estudio Nocturno")
        assertEquals(1, vm.playlists.value.size)
        val plId = vm.playlists.value.first().id

        vm.addTrackToPlaylist(plId, "user_track_1")
        vm.selectPlaylistFilter(plId)
        assertEquals(plId, vm.selectedPlaylistId.value)

        vm.setBalancePan(0.25f)
        vm.updateEqualizerBand10(0, 4.5f)
        vm.saveCustomSoundProfile("Referencia Plano", "Setup auriculares")
        assertEquals(1, vm.customSoundProfiles.value.size)

        val vm2 = CarAudioViewModel(app)
        assertEquals(1, vm2.playlists.value.size)
        assertEquals("Estudio Nocturno", vm2.playlists.value.first().name)
        assertTrue("user_track_1" in vm2.playlists.value.first().trackIds)
        assertEquals(1, vm2.customSoundProfiles.value.size)
        assertEquals("Referencia Plano", vm2.customSoundProfiles.value.first().name)

        vm2.deleteCustomSoundProfile(vm2.customSoundProfiles.value.first().id)
        vm2.deletePlaylist(plId)
        assertTrue(vm2.customSoundProfiles.value.isEmpty())
        assertTrue(vm2.playlists.value.isEmpty())
    }

    @Test
    fun `fault alerts record banner and history and clear cleanly`() {
        val vm = CarAudioViewModel(app)
        vm.reportSystemFault(
            title = "Error de Lectura",
            message = "No se pudo abrir la pista.",
            severity = FaultSeverity.ERROR,
            sendSystemNotification = false
        )

        assertEquals(1, vm.faultAlerts.value.size)
        assertEquals("Error de Lectura", vm.latestBannerAlert.value?.title)

        vm.dismissBannerAlert()
        assertNull(vm.latestBannerAlert.value)
        assertEquals(1, vm.faultAlerts.value.size)

        vm.clearFaultHistory()
        assertTrue(vm.faultAlerts.value.isEmpty())
    }

    @Test
    fun `viewmodel persists eq 10 and 31 bands shuffle repeat and settings across 9 sections`() {
        assertEquals(9, AppSection.entries.size)

        val vm1 = CarAudioViewModel(app)
        assertEquals(CarAudioThemeType.GRAPHITE_STUDIO, vm1.currentTheme.value)
        assertEquals(AppSection.HOME, vm1.selectedSection.value)

        vm1.selectSection(AppSection.AUDIO_TOOLS)
        assertEquals(AppSection.AUDIO_TOOLS, vm1.selectedSection.value)

        vm1.toggleShuffle()
        assertTrue(vm1.shuffleEnabled.value)

        assertEquals(RepeatMode.ALL, vm1.repeatMode.value)
        vm1.cycleRepeatMode()
        assertEquals(RepeatMode.ONE, vm1.repeatMode.value)

        vm1.setLiteVisualsEnabled(true)
        assertTrue(vm1.liteVisualsEnabled.value)

        vm1.updateEqualizerBand10(0, 6.5f)
        assertEquals(6.5f, vm1.equalizerSettings.value.bands10[0], 0.01f)
        assertEquals(6.5f, vm1.equalizerSettings.value.bands31[2], 0.01f)

        vm1.updateEqualizerBand31(17, 4.0f)
        assertEquals(4.0f, vm1.equalizerSettings.value.bands31[17], 0.01f)
        assertEquals(4.0f, vm1.equalizerSettings.value.bands10[5], 0.01f)

        val testTrack = AudioTrackItem(
            id = "user_fav_test",
            title = "Lossless Test",
            artist = "Estudio",
            durationSeconds = 120,
            category = TrackCategory.USER_CUSTOM
        )
        vm1.toggleFavorite(testTrack)
        vm1.setLibraryFilter(LibraryFilter.FAVORITES)
        vm1.setSearchQuery("Lossless")
        assertEquals("Lossless", vm1.searchQuery.value)
        assertEquals(LibraryFilter.FAVORITES, vm1.libraryFilter.value)

        val vm2 = CarAudioViewModel(app)
        assertTrue(vm2.shuffleEnabled.value)
        assertEquals(RepeatMode.ONE, vm2.repeatMode.value)
        assertTrue(vm2.liteVisualsEnabled.value)
        assertEquals(6.5f, vm2.equalizerSettings.value.bands10[0], 0.01f)
        assertEquals(4.0f, vm2.equalizerSettings.value.bands31[17], 0.01f)
        vm2.toggleShuffle()
        assertFalse(vm2.shuffleEnabled.value)
    }

    @Test
    fun `audio engine attaches session and syncs playback safely without record audio permission`() {
        val engine = CarAudioEngine(app)
        engine.attachToPlayerSession(123)
        assertEquals(123, engine.activeAudioSessionId)

        engine.setVisualizerActive(true)
        engine.syncPlaybackStateFromMedia3(
            track = AudioTrackItem("t_vis", "Test Bass", "Artist", durationSeconds = 120),
            playing = true,
            positionSeconds = 12
        )
        assertTrue(engine.isPlaying)
        assertEquals(12, engine.playbackPositionSeconds)

        engine.syncPlaybackStateFromMedia3(
            track = null,
            playing = false,
            positionSeconds = 0
        )
        assertFalse(engine.isPlaying)
        engine.release()
    }

    @Test
    fun `playback speed pitch and library sort order persist and reset properly`() {
        val vm = CarAudioViewModel(app)
        assertEquals(1.0f, vm.playbackSpeed.value, 0.01f)
        assertEquals(1.0f, vm.playbackPitch.value, 0.01f)
        assertEquals(LibrarySortOrder.RECENT, vm.librarySortOrder.value)

        vm.setPlaybackSpeed(1.25f)
        vm.setPlaybackPitch(0.95f)
        vm.setLibrarySortOrder(LibrarySortOrder.ARTIST)

        assertEquals(1.25f, vm.playbackSpeed.value, 0.01f)
        assertEquals(0.95f, vm.playbackPitch.value, 0.01f)
        assertEquals(LibrarySortOrder.ARTIST, vm.librarySortOrder.value)

        val vm2 = CarAudioViewModel(app)
        assertEquals(1.25f, vm2.playbackSpeed.value, 0.01f)
        assertEquals(0.95f, vm2.playbackPitch.value, 0.01f)
        assertEquals(LibrarySortOrder.ARTIST, vm2.librarySortOrder.value)

        vm2.resetPlaybackSpeedPitch()
        assertEquals(1.0f, vm2.playbackSpeed.value, 0.01f)
        assertEquals(1.0f, vm2.playbackPitch.value, 0.01f)
    }

    @Test
    fun `sleep timer starts toggles end of track and cancels cleanly`() {
        val vm = CarAudioViewModel(app)
        assertNull(vm.sleepTimerRemainingSec.value)
        assertFalse(vm.sleepTimerStopAfterTrack.value)

        vm.startSleepTimer(15)
        assertNotNull(vm.sleepTimerRemainingSec.value)
        assertEquals(15 * 60, vm.sleepTimerRemainingSec.value)
        assertFalse(vm.sleepTimerStopAfterTrack.value)

        vm.toggleSleepAfterCurrentTrack()
        assertTrue(vm.sleepTimerStopAfterTrack.value)
        assertNull(vm.sleepTimerRemainingSec.value)

        vm.cancelSleepTimer()
        assertNull(vm.sleepTimerRemainingSec.value)
        assertFalse(vm.sleepTimerStopAfterTrack.value)
    }

    @Test
    fun `play all play playlist and play folder build real queues`() {
        val vm = CarAudioViewModel(app)
        val tracks = listOf(
            AudioTrackItem("t1", "Alpha", "Artist 1", "Album 1", 120, folderName = "Rock"),
            AudioTrackItem("t2", "Beta", "Artist 2", "Album 2", 150, folderName = "Jazz"),
            AudioTrackItem("t3", "Gamma", "Artist 1", "Album 1", 180, folderName = "Rock")
        )
        vm.playAllTracks(shuffled = false, sourceList = tracks)
        assertEquals(3, vm.queueTracks.value.size)
        assertEquals("t1", vm.currentTrack.value?.id)

        vm.playFolder("Rock")
        assertEquals(listOf("t1", "t3"), vm.queueTracks.value.map { it.id })

        vm.createPlaylist("Favoritos Rock")
        val plId = vm.playlists.value.first().id
        vm.addTrackToPlaylist(plId, "t3")
        vm.playPlaylist(plId, shuffled = false)
        assertEquals(listOf("t3"), vm.queueTracks.value.map { it.id })
    }
}
