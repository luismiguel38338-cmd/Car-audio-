package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType

@Composable
fun VipUnlockDialog(
    theme: CarAudioThemeType,
    onDismiss: () -> Unit,
    onAttemptUnlock: (String) -> Boolean
) {
    var codeInput by remember { mutableStateOf("") }
    var showDigits by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val tryUnlock = {
        if (codeInput.isBlank()) {
            errorMsg = "Ingresa tu código de acceso VIP."
        } else if (onAttemptUnlock(codeInput)) {
            errorMsg = null
            onDismiss()
        } else {
            errorMsg = "Código incorrecto. Verifica tu clave VIP."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceColor,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor.copy(alpha = 0.18f))
                        .border(1.2.dp, theme.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "VIP Studio",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "ACCESO VIP STUDIO",
                        color = theme.textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Desbloquea calidad Hi-Res y módulos Pro",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.cardColor)
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val perks = listOf(
                        "Motor Hi-Res Studio y Calidez de Bulbos",
                        "Claridad Vocal, Presencia y Excitador Armónico",
                        "Bajos Sub-Harmonic 32Hz, SPL y Pendiente 48 dB/oct",
                        "Presets Master VIP, Paramétrico Pro y Temas Exclusivos"
                    )
                    perks.forEach { perk ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = perk,
                                color = theme.textColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = {
                        if (it.length <= 12) {
                            codeInput = it
                            errorMsg = null
                        }
                    },
                    label = { Text("Código VIP", fontSize = 12.sp) },
                    placeholder = { Text("Ingresa tu clave de acceso", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryColor)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showDigits = !showDigits }) {
                            Icon(
                                imageVector = if (showDigits) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showDigits) "Ocultar código" else "Mostrar código",
                                tint = theme.textSecondaryColor
                            )
                        }
                    },
                    visualTransformation = if (showDigits) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { tryUnlock() }),
                    singleLine = true,
                    isError = errorMsg != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.4f),
                        focusedTextColor = theme.textColor,
                        unfocusedTextColor = theme.textColor,
                        cursorColor = theme.primaryColor,
                        focusedLabelColor = theme.primaryColor,
                        unfocusedLabelColor = theme.textSecondaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vip_code_input")
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = theme.errorColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("vip_code_error")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { tryUnlock() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primaryColor,
                    contentColor = theme.onPrimaryColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("vip_unlock_confirm_button")
            ) {
                Text("DESBLOQUEAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = theme.textSecondaryColor, fontWeight = FontWeight.Bold)
            }
        }
    )
}
