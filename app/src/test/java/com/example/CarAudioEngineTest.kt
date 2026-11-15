package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.CarAudioEngine
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.ParametricBand
import com.example.model.ParametricFilterType
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.viewmodel.AppTab
import com.example.viewmodel.CarAudioViewModel
import com.example.viewmodel.LibraryFilter
import com.example.model.FaultSeverity
import com.example.model.SubwooferPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            bands31 = List(31) { 0f },
            bands15 = List(15) { 0f },
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
    fun `bass system subsonic cut and punch shape low frequencies`() {
        val flatEq = EqualizerSettings(isEnabled = false, bands31 = List(31) { 0f }, bands15 = List(15) { 0f })
        val dspPunch = DspSettings(
            dspMasterEnabled = true,
            bassGainDb = 0f,
            bassBoostEnabled = false,
            hpfEnabled = false,
            lpfEnabled = false,
            subsonicFilterActive = true,
            subsonicFrequencyHz = 35f,
            bassPunchLevel = 8.0f
        )
        val gain20Hz = CarAudioEngine.computeEffectiveBandGainDb(20, dspPunch, flatEq)
        val gain75Hz = CarAudioEngine.computeEffectiveBandGainDb(75, dspPunch, flatEq)

        assertTrue("Subsonic 35Hz should strongly attenuate 20Hz, got $gain20Hz", gain20Hz < -4.0f)
        assertTrue("Bass punch should boost 75Hz region, got $gain75Hz", gain75Hz > 3.0f)
    }

    @Test
    fun `vip code 2020 unlocks persisted vip features and rejects invalid codes`() {
        val vm = CarAudioViewModel(app)
        assertFalse(vm.isVipUnlocked.value)

        // Reject wrong code
        assertFalse(vm.unlockVipWithCode("1234"))
        assertFalse(vm.isVipUnlocked.value)

        // Gated actions stay blocked while locked
        vm.setHiResQuality(true)
        assertFalse(vm.dspSettings.value.hiResQualityEnabled)
        vm.setTheme(CarAudioThemeType.CARBON_GOLD)
        assertEquals(CarAudioThemeType.CRIMSON_RED, vm.currentTheme.value)

        // Unlock with secret code 2020
        assertTrue(vm.unlockVipWithCode("2020"))
        assertTrue(vm.isVipUnlocked.value)

        vm.setHiResQuality(true)
        vm.setTubeWarmth(6.5f)
        vm.setTheme(CarAudioThemeType.VIP_OBSIDIAN_DIAMOND)
        vm.applySubwooferPreset(SubwooferPreset.DEEP_SUB_32HZ)

        assertTrue(vm.dspSettings.value.hiResQualityEnabled)
        assertEquals(6.5f, vm.dspSettings.value.tubeWarmthLevel, 0.01f)
        assertEquals(CarAudioThemeType.VIP_OBSIDIAN_DIAMOND, vm.currentTheme.value)

        // Verify persistence
        val vm2 = CarAudioViewModel(app)
        assertTrue(vm2.isVipUnlocked.value)
        assertTrue(vm2.dspSettings.value.hiResQualityEnabled)
        assertEquals(CarAudioThemeType.VIP_OBSIDIAN_DIAMOND, vm2.currentTheme.value)

        // Lock reverts VIP exclusives
        vm2.lockVip()
        assertFalse(vm2.isVipUnlocked.value)
        assertFalse(vm2.dspSettings.value.hiResQualityEnabled)
        assertEquals(CarAudioThemeType.CRIMSON_RED, vm2.currentTheme.value)
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
    fun `viewmodel persists dsp eq shuffle repeat and favorites`() {
        val vm1 = CarAudioViewModel(app)
        assertEquals(CarAudioThemeType.CRIMSON_RED, vm1.currentTheme.value)

        vm1.selectTab(AppTab.DSP)
        assertEquals(AppTab.DSP, vm1.selectedTab.value)

        vm1.toggleShuffle()
        assertTrue(vm1.shuffleEnabled.value)

        // Default RepeatMode is ALL -> ONE -> OFF -> ALL
        assertEquals(RepeatMode.ALL, vm1.repeatMode.value)
        vm1.cycleRepeatMode()
        assertEquals(RepeatMode.ONE, vm1.repeatMode.value)

        vm1.setLiteVisualsEnabled(true)
        assertTrue(vm1.liteVisualsEnabled.value)

        vm1.updateEqualizerBand(0, 7.5f)
        assertEquals(7.5f, vm1.equalizerSettings.value.bands15[0], 0.01f)
        assertEquals(7.5f, vm1.equalizerSettings.value.bands31[0], 0.01f)

        val testTrack = AudioTrackItem(
            id = "user_fav_test",
            title = "Bass Test",
            artist = "Car Audio",
            durationSeconds = 120,
            category = TrackCategory.USER_CUSTOM
        )
        vm1.toggleFavorite(testTrack)
        vm1.setLibraryFilter(LibraryFilter.FAVORITES)
        vm1.setSearchQuery("Bass")
        assertEquals("Bass", vm1.searchQuery.value)
        assertEquals(LibraryFilter.FAVORITES, vm1.libraryFilter.value)

        // Recreate ViewModel to verify SharedPreferences durability
        val vm2 = CarAudioViewModel(app)
        assertTrue(vm2.shuffleEnabled.value)
        assertEquals(RepeatMode.ONE, vm2.repeatMode.value)
        assertTrue(vm2.liteVisualsEnabled.value)
        assertEquals(7.5f, vm2.equalizerSettings.value.bands15[0], 0.01f)
        vm2.toggleShuffle()
        assertFalse(vm2.shuffleEnabled.value)
    }
}
