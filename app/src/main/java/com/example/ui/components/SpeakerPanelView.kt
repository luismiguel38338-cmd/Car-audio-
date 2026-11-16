package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.SubwooferPreset
import kotlin.math.roundToInt

@Composable
fun SpeakerPanelView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    eqSettings: EqualizerSettings,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    isVipUnlocked: Boolean,
    onBalancePanChanged: (Float) -> Unit,
    onFaderChanged: (Float) -> Unit,
    onSpeakerConfigChanged: (sizeLabel: String, count: Int) -> Unit,
    onTweeterConfigChanged: (sizeMm: Int, material: String, hpfHz: Float, levelDb: Float) -> Unit,
    onToggleBassBoost: (Boolean, Float, Float) -> Unit,
    onSubBassChanged: (Boolean, Float, Float) -> Unit,
    onSubsonicChanged: (Boolean, Float) -> Unit,
    onLpfChanged: (Boolean, Float) -> Unit,
    onBassPunchChanged: (Float) -> Unit,
    onPhaseChanged: (Int) -> Unit,
    onTimeAlignmentChanged: (Float) -> Unit,
    onApplySubPreset: (SubwooferPreset) -> Unit,
    onRequestVipDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dspOn = dspSettings.dspMasterEnabled
    val speakerSizes = remember { listOf("5.25\"", "6.5\"", "6x9\"", "8\"", "10\"", "12\"") }
    val tweeterMaterials = remember { listOf("Cúpula de Seda", "Titanio", "Aluminio", "Cerámico") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("speaker_panel_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. 5-LED Bar
        item(key = "speakers_5led") {
            FiveLedIndicatorBar(
                isAudioPlaying = isAnyAudioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                theme = theme,
                liteVisuals = liteVisuals,
                title = "PANEL DE ALTAVOCES • 5 LED"
            )
        }

        // 2. Live Audio-Synced Speaker & Tweeter Excursion Visualizer
        item(key = "speakers_visualizer") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.2.dp, theme.primaryColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Speaker, contentDescription = null, tint = theme.primaryColor)
                            Column {
                                Text("EXCURSIÓN DE CONOS Y TWEETERS", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                Text(
                                    text = "${dspSettings.speakerCount}x Bocinas ${dspSettings.speakerSizeLabel} • Tweeter ${dspSettings.tweeterSizeMm}mm",
                                    color = theme.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.cardColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAnyAudioPlaying) "SYNC ACTIVO" else "STANDBY",
                                color = if (isAnyAudioPlaying) theme.accentColor else theme.textSecondaryColor,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Dual Speaker + Tweeter Canvas synced to real bass/mid/treble energy
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.backgroundColor)
                    ) {
                        val cy = size.height / 2f
                        val subCenter = Offset(size.width * 0.28f, cy)
                        val midCenter = Offset(size.width * 0.62f, cy)
                        val tweetCenter = Offset(size.width * 0.85f, cy)

                        val subExcursion = if (isAnyAudioPlaying) bassEnergy else 0f
                        val midExcursion = if (isAnyAudioPlaying) midEnergy else 0f
                        val twExcursion = if (isAnyAudioPlaying) trebleEnergy else 0f

                        // Left: Subwoofer cone
                        val subR = size.minDimension * (0.36f + subExcursion * 0.09f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(theme.primaryColor.copy(alpha = 0.25f + subExcursion * 0.4f), Color.Transparent),
                                center = subCenter,
                                radius = subR * 1.25f
                            ),
                            center = subCenter,
                            radius = subR * 1.25f
                        )
                        drawCircle(color = Color(0xFF151C28), radius = subR, center = subCenter)
                        drawCircle(color = theme.primaryColor, radius = subR, center = subCenter, style = Stroke(width = 2.5.dp.toPx()))
                        drawCircle(color = theme.primaryColor.copy(alpha = 0.45f), radius = subR * 0.65f, center = subCenter, style = Stroke(width = 1.2.dp.toPx()))
                        drawCircle(color = theme.primaryColor, radius = subR * 0.28f, center = subCenter)

                        // Middle: Door Mid-Woofer
                        val midR = size.minDimension * (0.26f + midExcursion * 0.06f)
                        drawCircle(color = Color(0xFF151C28), radius = midR, center = midCenter)
                        drawCircle(color = theme.accentColor, radius = midR, center = midCenter, style = Stroke(width = 2.dp.toPx()))
                        drawCircle(color = theme.accentColor.copy(alpha = 0.45f), radius = midR * 0.34f, center = midCenter)

                        // Right: Silk/Titanium Tweeter Dome
                        val twR = size.minDimension * (0.14f + twExcursion * 0.04f)
                        drawCircle(color = Color(0xFF151C28), radius = twR, center = tweetCenter)
                        drawCircle(color = Color(0xFFF5F9FF), radius = twR, center = tweetCenter, style = Stroke(width = 1.8.dp.toPx()))
                        drawCircle(color = Color(0xFFF5F9FF).copy(alpha = 0.35f + twExcursion * 0.55f), radius = twR * 0.45f, center = tweetCenter)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("SUBWOOFER (${(bassEnergy * 100).toInt()}%)", color = theme.primaryColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("MEDIOS (${(midEnergy * 100).toInt()}%)", color = theme.accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("TWEETER (${(trebleEnergy * 100).toInt()}%)", color = Color(0xFFF5F9FF), fontSize = 9.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 3. Balance L/R & Fader Delantero/Trasero
        item(key = "speakers_balance_fader") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("ESCENARIO: BALANCE (L/R) & FADER (DEL/TRAS)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)

                    DspSliderRow(
                        label = "BALANCE IZQUIERDA / DERECHA",
                        value = dspSettings.balancePan,
                        range = -1.0f..1.0f,
                        unit = "Pan",
                        enabled = true,
                        theme = theme,
                        onValueChange = onBalancePanChanged
                    )

                    DspSliderRow(
                        label = "FADER TRASERO / DELANTERO",
                        value = dspSettings.faderFrontRear,
                        range = -1.0f..1.0f,
                        unit = "Fade",
                        enabled = true,
                        theme = theme,
                        onValueChange = onFaderChanged
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.cardColor)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "La salida estéreo nativa del teléfono controla Balance L/R; el Fader Delantero/Trasero requiere autoestéreo o amplificador DSP externo de 4 canales compatible.",
                            color = theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // 4. Speaker & Tweeter Configuration
        item(key = "speakers_and_tweeters_config") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                        Text("CONFIGURACIÓN DE ALTAVOCES Y TWEETERS", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    Text("Diámetro de Altavoces en Puertas", color = theme.textSecondaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        speakerSizes.forEach { sizeLbl ->
                            FilterChip(
                                selected = dspSettings.speakerSizeLabel == sizeLbl,
                                onClick = { onSpeakerConfigChanged(sizeLbl, dspSettings.speakerCount) },
                                label = { Text(sizeLbl, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.cardColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }

                    Text("Cantidad de Bocinas", color = theme.textSecondaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4, 6, 8).forEach { count ->
                            FilterChip(
                                selected = dspSettings.speakerCount == count,
                                onClick = { onSpeakerConfigChanged(dspSettings.speakerSizeLabel, count) },
                                label = { Text("$count ch", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.accentColor,
                                    selectedLabelColor = Color(0xFF06150E),
                                    containerColor = theme.cardColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }

                    Text("Material y Corte de Tweeters", color = theme.textSecondaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tweeterMaterials.forEach { mat ->
                            FilterChip(
                                selected = dspSettings.tweeterMaterial == mat,
                                onClick = {
                                    onTweeterConfigChanged(dspSettings.tweeterSizeMm, mat, dspSettings.tweeterHpfHz, dspSettings.tweeterLevelDb)
                                },
                                label = { Text(mat, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.cardColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }

                    DspSliderRow(
                        label = "CORTE TWEETER HPF (${dspSettings.tweeterHpfHz.roundToInt()} Hz)",
                        value = dspSettings.tweeterHpfHz,
                        range = 1800f..8000f,
                        unit = "Hz",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = { onTweeterConfigChanged(dspSettings.tweeterSizeMm, dspSettings.tweeterMaterial, it, dspSettings.tweeterLevelDb) }
                    )

                    DspSliderRow(
                        label = "ATENUACIÓN / GANANCIA TWEETER",
                        value = dspSettings.tweeterLevelDb,
                        range = -6f..6f,
                        unit = "dB",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = { onTweeterConfigChanged(dspSettings.tweeterSizeMm, dspSettings.tweeterMaterial, dspSettings.tweeterHpfHz, it) }
                    )
                }
            }
        }

        // 5. Subwoofer Control & Crossover Card
        item(key = "speakers_subwoofer_stage") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                            Text("CONTROL DE SUBWOOFER Y SUB-BASS", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Switch(
                            checked = dspSettings.subBassEnabled,
                            onCheckedChange = { onSubBassChanged(it, dspSettings.subBassBoostDb, dspSettings.subBassFreqHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SubwooferPreset.entries.forEach { preset ->
                            val locked = preset.isVip && !isVipUnlocked
                            FilterChip(
                                selected = false,
                                onClick = { if (locked) onRequestVipDialog() else onApplySubPreset(preset) },
                                label = {
                                    Text(
                                        text = if (locked) "${preset.label} (VIP)" else preset.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(containerColor = theme.cardColor, labelColor = theme.textColor)
                            )
                        }
                    }

                    DspSliderRow(
                        label = "BASS BOOST SUBWOOFER",
                        value = dspSettings.bassBoostDb,
                        range = 0f..24f,
                        unit = "dB",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = { onToggleBassBoost(it > 0.5f, it, dspSettings.bassBoostFreqHz) }
                    )

                    DspSliderRow(
                        label = "SUB-BASS (${dspSettings.subBassFreqHz.roundToInt()} Hz)",
                        value = dspSettings.subBassBoostDb,
                        range = 0f..18f,
                        unit = "dB",
                        enabled = dspOn && dspSettings.subBassEnabled,
                        theme = theme,
                        onValueChange = { onSubBassChanged(true, it, dspSettings.subBassFreqHz) }
                    )

                    DspSliderRow(
                        label = "IMPACTO / PUNCH",
                        value = dspSettings.bassPunchLevel,
                        range = 0f..10f,
                        unit = "dB",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = onBassPunchChanged
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filtro Subsónico (${dspSettings.subsonicFrequencyHz.toInt()} Hz)", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = dspSettings.subsonicFilterActive,
                            onCheckedChange = { onSubsonicChanged(it, dspSettings.subsonicFrequencyHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                        )
                    }

                    DspSliderRow(
                        label = "CORTE PASA-BAJAS SUBWOOFER LPF",
                        value = dspSettings.lpfFrequencyHz.coerceIn(50f, 250f),
                        range = 50f..250f,
                        unit = "Hz",
                        enabled = dspOn && dspSettings.lpfEnabled,
                        theme = theme,
                        onValueChange = { onLpfChanged(true, it) }
                    )

                    DspSliderRow(
                        label = "ALINEACIÓN TEMPORAL (DELAY)",
                        value = dspSettings.timeAlignmentMs,
                        range = 0f..15f,
                        unit = "ms",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = onTimeAlignmentChanged
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fase del Subwoofer", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 180).forEach { deg ->
                                val active = dspSettings.phaseDegrees == deg
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) theme.primaryColor else theme.cardColor)
                                        .clickable { onPhaseChanged(deg) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("${deg}°", color = if (active) theme.onPrimaryColor else theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
