package com.example.model

import androidx.compose.ui.graphics.Color

enum class CarAudioThemeType(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val primaryColor: Color,
    val accentColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardColor: Color,
    val onPrimaryColor: Color,
    val textColor: Color,
    val textSecondaryColor: Color,
    val meterPeakColor: Color,
    val clipAlertColor: Color = Color(0xFFE5A93B),
    val errorColor: Color = Color(0xFFD9776C),
    val isVipOnly: Boolean = false
) {
    GRAPHITE_STUDIO(
        id = "graphite_studio",
        displayName = "Grafito Estudio",
        subtitle = "Gris grafito, gris plata, blanco y acentos azul hielo discretos",
        primaryColor = Color(0xFFD6DCE5),
        accentColor = Color(0xFF78B7E8),
        backgroundColor = Color(0xFF0E1014),
        surfaceColor = Color(0xFF161920),
        cardColor = Color(0xFF1E232D),
        onPrimaryColor = Color(0xFF0E1014),
        textColor = Color(0xFFF5F7FA),
        textSecondaryColor = Color(0xFF9AA3B2),
        meterPeakColor = Color(0xFF78B7E8),
        isVipOnly = false
    ),
    SLATE_SILVER(
        id = "slate_silver",
        displayName = "Plata Pizarra",
        subtitle = "Gris oscuro neutro con acabados en aluminio cepillado",
        primaryColor = Color(0xFFE2E6EC),
        accentColor = Color(0xFF8CC3EC),
        backgroundColor = Color(0xFF111318),
        surfaceColor = Color(0xFF191D24),
        cardColor = Color(0xFF232833),
        onPrimaryColor = Color(0xFF111318),
        textColor = Color(0xFFF8FAFC),
        textSecondaryColor = Color(0xFFA0A9B8),
        meterPeakColor = Color(0xFF96CCF5),
        isVipOnly = false
    ),
    CARBON_ICE(
        id = "carbon_ice",
        displayName = "Carbón & Hielo",
        subtitle = "Alto contraste minimalista con detalles azul hielo suaves",
        primaryColor = Color(0xFF78B7E8),
        accentColor = Color(0xFFDCE3ED),
        backgroundColor = Color(0xFF0B0D11),
        surfaceColor = Color(0xFF14181F),
        cardColor = Color(0xFF1C222B),
        onPrimaryColor = Color(0xFF09131C),
        textColor = Color(0xFFF3F6FA),
        textSecondaryColor = Color(0xFF94A0B0),
        meterPeakColor = Color(0xFF78B7E8),
        isVipOnly = false
    ),
    OBSIDIAN_STUDIO_VIP(
        id = "obsidian_studio_vip",
        displayName = "Obsidiana Master VIP",
        subtitle = "Grafito profundo de consola con precisión cristalina",
        primaryColor = Color(0xFFEBF0F6),
        accentColor = Color(0xFF83C0EE),
        backgroundColor = Color(0xFF08090C),
        surfaceColor = Color(0xFF12151B),
        cardColor = Color(0xFF1A1E27),
        onPrimaryColor = Color(0xFF08090C),
        textColor = Color(0xFFFAFCFF),
        textSecondaryColor = Color(0xFF97A1B2),
        meterPeakColor = Color(0xFF83C0EE),
        isVipOnly = true
    ),
    PLATINUM_ICE_VIP(
        id = "platinum_ice_vip",
        displayName = "Platino Ártico VIP",
        subtitle = "Superficie gris acero con reflejos azul hielo discretos",
        primaryColor = Color(0xFF93C7EE),
        accentColor = Color(0xFFE5EAF2),
        backgroundColor = Color(0xFF101319),
        surfaceColor = Color(0xFF181D26),
        cardColor = Color(0xFF212734),
        onPrimaryColor = Color(0xFF0B131C),
        textColor = Color(0xFFF7F9FC),
        textSecondaryColor = Color(0xFFA2ACBD),
        meterPeakColor = Color(0xFF93C7EE),
        isVipOnly = true
    ),
    MIDNIGHT_COBALT_VIP(
        id = "midnight_cobalt_vip",
        displayName = "Acero Nocturno VIP",
        subtitle = "Tono oscuro de estudio con indicadores plata y hielo",
        primaryColor = Color(0xFFD0D8E4),
        accentColor = Color(0xFF6BAEE0),
        backgroundColor = Color(0xFF0C0F14),
        surfaceColor = Color(0xFF151922),
        cardColor = Color(0xFF1D2330),
        onPrimaryColor = Color(0xFF0C0F14),
        textColor = Color(0xFFF4F7FB),
        textSecondaryColor = Color(0xFF8E99AA),
        meterPeakColor = Color(0xFF6BAEE0),
        isVipOnly = true
    );

    val textPrimaryColor: Color get() = textColor

    companion object {
        fun fromId(id: String?): CarAudioThemeType {
            return entries.firstOrNull { it.id == id } ?: GRAPHITE_STUDIO
        }
    }
}
