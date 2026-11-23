package com.example.ads

import com.example.model.AdSlotConfig
import com.example.model.AdSlotType

/**
 * Modular ad slot abstraction prepared for future official Google AdMob integration.
 * In compliance with Google Play Creator Program rules, no ad SDK or AD_ID permission
 * is bundled unless explicitly opted in by the developer.
 */
object AdSlotManager {
    const val IS_AD_SDK_BUNDLED = false

    val slotConfigs: List<AdSlotConfig> = listOf(
        AdSlotConfig(
            slotType = AdSlotType.BANNER,
            isSdkEnabled = false,
            isConfigured = false,
            policySummary = "Espacio reservado para Banner no intrusivo. SDK de anuncios desactivado por cumplimiento Google Play."
        ),
        AdSlotConfig(
            slotType = AdSlotType.INTERSTITIAL,
            isSdkEnabled = false,
            isConfigured = false,
            policySummary = "Espacio reservado para Intersticial opcional al abrir listas o descargas. Desactivado por política actual."
        ),
        AdSlotConfig(
            slotType = AdSlotType.REWARDED,
            isSdkEnabled = false,
            isConfigured = false,
            policySummary = "Espacio reservado para Anuncio Recompensado opcional. Desactivado por política actual."
        )
    )

    fun statusDescription(): String =
        "Monetización preparada de forma modular (Banner, Intersticial y Recompensado) sin SDK publicitario activo."
}
