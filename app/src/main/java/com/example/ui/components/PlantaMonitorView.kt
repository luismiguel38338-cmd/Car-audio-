package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AmpTelemetry
import com.example.model.CarAudioThemeType

@Composable
fun PlantaMonitorView(
    theme: CarAudioThemeType,
    telemetry: AmpTelemetry,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onTestClippingAlert: () -> Unit,
    onTestVoltageAlert: () -> Unit,
    onSendStatusNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Amplifier Installation Graphic
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .testTag("amp_hero_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.car_audio_amp_hero_1788805982422),
                    contentDescription = "Planta Car Audio Amplificador",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(if (telemetry.isPlantaConnected) theme.primaryColor else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLANTA CAR AUDIO MONOBLOCK 3000W RMS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Clip badge
                if (telemetry.isClipping) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(theme.clipAlertColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "¡CLIP DETECTADO!",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Live Diagnostic Gauges (Voltaje, Temp, Potencia RMS, Clip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Voltage Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("voltage_gauge_card"),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(16.dp),
                border = if (telemetry.isLowVoltage) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.clipAlertColor, Color.Yellow))) else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricalServices,
                        contentDescription = "Voltaje",
                        tint = if (telemetry.isLowVoltage) theme.clipAlertColor else theme.primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "VOLTAJE",
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${"%.1f".format(telemetry.voltage)}V",
                        color = if (telemetry.isLowVoltage) theme.clipAlertColor else theme.textColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (telemetry.voltage >= 13.8f) "Alternador OK" else if (telemetry.voltage >= 12.2f) "Batería" else "¡Bajo Voltaje!",
                        color = if (telemetry.isLowVoltage) theme.clipAlertColor else theme.primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Temperature Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("temp_gauge_card"),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Temperatura",
                        tint = if (telemetry.isOverheated) theme.clipAlertColor else theme.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TEMPERATURA",
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${telemetry.temperatureC.toInt()}°C",
                        color = if (telemetry.isOverheated) theme.clipAlertColor else theme.textColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (telemetry.temperatureC < 65f) "Normal" else if (telemetry.temperatureC < 80f) "Caliente" else "¡Protección!",
                        color = if (telemetry.isOverheated) theme.clipAlertColor else theme.accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Output Watts Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("power_gauge_card"),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Power,
                        contentDescription = "Potencia",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "POTENCIA RMS",
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${telemetry.outputPowerWatts}W",
                        color = theme.textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "@ 1 Ohm",
                        color = theme.primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Status LEDs & Diagnostic Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amp_status_leds_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LUCES LED DE DIAGNÓSTICO DE LA PLANTA",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Power LED
                    LedIndicator(
                        label = "POWER",
                        isActive = true,
                        activeColor = theme.primaryColor,
                        theme = theme
                    )
                    // Clip LED
                    LedIndicator(
                        label = "CLIP",
                        isActive = telemetry.isClipping,
                        activeColor = theme.clipAlertColor,
                        theme = theme
                    )
                    // Protect LED
                    LedIndicator(
                        label = "PROTECT",
                        isActive = telemetry.protectionModeActive,
                        activeColor = Color(0xFFFF0000),
                        theme = theme
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.surfaceColor, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (telemetry.protectionModeActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = "Estado",
                        tint = if (telemetry.protectionModeActive) theme.clipAlertColor else theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (telemetry.protectionModeActive) {
                            "⚠️ Planta en modo PROTECT o bajo voltaje. Revisa cableado y batería."
                        } else {
                            "✅ Planta operando estable en 14.4V sin sobrecalentamiento. Total Clips: ${telemetry.clipCount}"
                        },
                        color = theme.textColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Notification System & Alert Tester Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notifications_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.5f), theme.accentColor.copy(alpha = 0.5f))))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notificaciones",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NOTIFICACIONES & ALERTAS CRÍTICAS",
                            color = theme.textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!hasNotificationPermission) {
                        Button(
                            onClick = onRequestNotificationPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor,
                                contentColor = theme.onPrimaryColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pedir Permiso", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Activo", tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Permiso Concedido", color = theme.primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "El sistema envía alertas a la barra de estado de Android cuando se detecta recorte de señal (clipping para no quemar las bobinas) o bajón de batería menor a 11.5V.",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTestClippingAlert,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_clip_notification_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.surfaceColor,
                            contentColor = theme.clipAlertColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar Clip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onTestVoltageAlert,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_voltage_notification_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.surfaceColor,
                            contentColor = Color(0xFFFFB300)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BatteryAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar Voltaje", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSendStatusNotification,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_status_notification_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.surfaceColor,
                            contentColor = theme.primaryColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Notif Estado", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LedIndicator(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    theme: CarAudioThemeType
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color.DarkGray.copy(alpha = 0.5f))
                .border(2.dp, if (isActive) activeColor.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) activeColor else theme.textSecondaryColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
