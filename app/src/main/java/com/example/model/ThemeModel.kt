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
    val clipAlertColor: Color = Color(0xFFF5A623),
    val errorColor: Color = Color(0xFFE06C75),
    val isVipOnly: Boolean = false,
    val isLightTheme: Boolean = false
) {
    NEON_DSP_PRO(
        id = "neon_dsp_pro",
        displayName = "Epic Violet DSP Pro",
        subtitle = "Negro OLED profundo, iluminación LED violeta, morado y detalles plata",
        primaryColor = Color(0xFFA855F7),
        accentColor = Color(0xFFC084FC),
        backgroundColor = Color(0xFF05040A),
        surfaceColor = Color(0xFF0E0A1B),
        cardColor = Color(0xFF191230),
        onPrimaryColor = Color(0xFFFFFFFF),
        textColor = Color(0xFFF8FAFC),
        textSecondaryColor = Color(0xFFA79EC0),
        meterPeakColor = Color(0xFFE9D5FF),
        isVipOnly = false
    ),
    GRAPHITE_STUDIO(
        id = "graphite_studio",
        displayName = "Grafito Discreto",
        subtitle = "Gris grafito mate, plata suave y acento azul hielo",
        primaryColor = Color(0xFFE2E8F0),
        accentColor = Color(0xFF7DD3FC),
        backgroundColor = Color(0xFF0B0E13),
        surfaceColor = Color(0xFF131820),
        cardColor = Color(0xFF1B222D),
        onPrimaryColor = Color(0xFF0B0E13),
        textColor = Color(0xFFF8FAFC),
        textSecondaryColor = Color(0xFF94A3B8),
        meterPeakColor = Color(0xFF7DD3FC),
        isVipOnly = false
    ),
    SLATE_SILVER(
        id = "slate_silver",
        displayName = "Plata Estudio",
        subtitle = "Pizarra oscura limpia con detalles metálicos neutros",
        primaryColor = Color(0xFFCBD5E1),
        accentColor = Color(0xFF38BDF8),
        backgroundColor = Color(0xFF0E1117),
        surfaceColor = Color(0xFF161B24),
        cardColor = Color(0xFF202734),
        onPrimaryColor = Color(0xFF0E1117),
        textColor = Color(0xFFF8FAFC),
        textSecondaryColor = Color(0xFF9CA3AF),
        meterPeakColor = Color(0xFF38BDF8),
        isVipOnly = false
    ),
    CARBON_ICE(
        id = "carbon_ice",
        displayName = "Negro Carbono",
        subtitle = "Negro profundo OLED con acentos limpios de contraste",
        primaryColor = Color(0xFF7DD3FC),
        accentColor = Color(0xFFE2E8F0),
        backgroundColor = Color(0xFF07090D),
        surfaceColor = Color(0xFF10141C),
        cardColor = Color(0xFF181F2C),
        onPrimaryColor = Color(0xFF061018),
        textColor = Color(0xFFF1F5F9),
        textSecondaryColor = Color(0xFF8E9BB0),
        meterPeakColor = Color(0xFF7DD3FC),
        isVipOnly = false
    ),
    OBSIDIAN_STUDIO_VIP(
        id = "obsidian_studio_vip",
        displayName = "Ámbar Cálido",
        subtitle = "Iluminación cálida tenue ideal para viajes largos",
        primaryColor = Color(0xFFFBBF24),
        accentColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF0B0C0F),
        surfaceColor = Color(0xFF14161C),
        cardColor = Color(0xFF1E222B),
        onPrimaryColor = Color(0xFF0B0C0F),
        textColor = Color(0xFFFAFAFA),
        textSecondaryColor = Color(0xFFA1A1AA),
        meterPeakColor = Color(0xFFFBBF24),
        isVipOnly = false
    ),
    PLATINUM_ICE_VIP(
        id = "platinum_ice_vip",
        displayName = "Esmeralda Suave",
        subtitle = "Gris oscuro equilibrado con detalle verde menta discreto",
        primaryColor = Color(0xFF93C5FD),
        accentColor = Color(0xFF34D399),
        backgroundColor = Color(0xFF080C12),
        surfaceColor = Color(0xFF111824),
        cardColor = Color(0xFF192334),
        onPrimaryColor = Color(0xFF080C12),
        textColor = Color(0xFFF8FAFC),
        textSecondaryColor = Color(0xFF94A3B8),
        meterPeakColor = Color(0xFF34D399),
        isVipOnly = false
    ),
    MIDNIGHT_COBALT_VIP(
        id = "midnight_cobalt_vip",
        displayName = "Noche Tenue",
        subtitle = "Brillo reducido anti-deslumbramiento para conducción nocturna",
        primaryColor = Color(0xFF93C5FD),
        accentColor = Color(0xFF7DD3FC),
        backgroundColor = Color(0xFF06080B),
        surfaceColor = Color(0xFF0E131B),
        cardColor = Color(0xFF161D29),
        onPrimaryColor = Color(0xFF06080B),
        textColor = Color(0xFFE2E8F0),
        textSecondaryColor = Color(0xFF7E8B9E),
        meterPeakColor = Color(0xFF7DD3FC),
        isVipOnly = false
    ),
    DAYLIGHT_STUDIO(
        id = "daylight_studio",
        displayName = "Luz de Día (Modo Claro)",
        subtitle = "Alta legibilidad solar para conducción diurna con acento violeta",
        primaryColor = Color(0xFF6D28D9),
        accentColor = Color(0xFF7C3AED),
        backgroundColor = Color(0xFFF1F5F9),
        surfaceColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFE2E8F0),
        onPrimaryColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF0F172A),
        textSecondaryColor = Color(0xFF475569),
        meterPeakColor = Color(0xFF6D28D9),
        clipAlertColor = Color(0xFFD97706),
        errorColor = Color(0xFFDC2626),
        isVipOnly = false,
        isLightTheme = true
    );

    val textPrimaryColor: Color get() = textColor
    val silverDetailColor: Color get() = if (isLightTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val ledGlowColor: Color get() = accentColor
    val glassBackgroundColor: Color get() = if (isLightTheme) surfaceColor.copy(alpha = 0.94f) else surfaceColor.copy(alpha = 0.80f)

    companion object {
        fun fromId(id: String?): CarAudioThemeType {
            return entries.firstOrNull { it.id == id } ?: NEON_DSP_PRO
        }
    }
}
