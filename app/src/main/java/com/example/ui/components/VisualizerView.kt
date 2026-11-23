package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.OutputDeviceStatus
import com.example.model.RtaBand

@Composable
fun VisualizerView(
    theme: CarAudioThemeType,
    rtaBands: List<RtaBand>,
    oscilloscopeWave: FloatArray,
    leftVu: Float,
    rightVu: Float,
    rmsDb: Float,
    splDb: Float,
    isClippingDetected: Boolean,
    clipEventsCount: Int,
    isMicRtaActive: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    dspMasterEnabled: Boolean,
    liteVisuals: Boolean,
    outputDeviceStatus: OutputDeviceStatus,
    powerTelemetry: DevicePowerTelemetry,
    onToggleLiteVisuals: (Boolean) -> Unit,
    onToggleMicRta: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("visualizer_view")
    ) {
        // Optimización rápida TECNO BG6 / ahorro de batería + Cabina reactiva
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassStudioCard(theme = theme, highlightBorder = isAnyAudioPlaying || isMicRtaActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatterySaver,
                            contentDescription = "Ahorro de batería",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Modo Ligero (TECNO BG6 / Ahorro)",
                                color = theme.textColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (liteVisuals) "Animación optimizada de bajo consumo" else "Gráficos 3D y halógenos neón completos",
                                color = theme.textSecondaryColor,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    Switch(
                        checked = liteVisuals,
                        onCheckedChange = onToggleLiteVisuals,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor),
                        modifier = Modifier.testTag("visualizer_lite_switch")
                    )
                }
            }

            Automotive3DCockpitView(
                theme = theme,
                isPlaying = isAnyAudioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                dspMasterEnabled = dspMasterEnabled,
                liteVisuals = liteVisuals
            )

            GlassStudioCard(
                theme = theme,
                highlightBorder = isAnyAudioPlaying || isMicRtaActive
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Analizador RTA 31 Bandas, Osciloscopio y Medidor dB",
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    AudioMetersView(
                        theme = theme,
                        rtaBands = rtaBands,
                        oscilloscopeWave = oscilloscopeWave,
                        leftVu = leftVu,
                        rightVu = rightVu,
                        rmsDb = rmsDb,
                        splDb = splDb,
                        isClippingDetected = isClippingDetected,
                        clipEventsCount = clipEventsCount,
                        isMicRtaActive = isMicRtaActive,
                        isAnyAudioPlaying = isAnyAudioPlaying,
                        bassEnergy = bassEnergy,
                        midEnergy = midEnergy,
                        trebleEnergy = trebleEnergy,
                        liteVisuals = liteVisuals,
                        outputDeviceStatus = outputDeviceStatus,
                        powerTelemetry = powerTelemetry,
                        onToggleMicRta = onToggleMicRta,
                        onRefreshTelemetry = onRefreshTelemetry,
                        scrollable = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
