package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.onGoogleSignInClicked
import com.example.auth.signOutFromGoogle
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.CloudUserProfile
import com.example.model.LocalProfileStats
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileView(
    theme: CarAudioThemeType,
    stats: LocalProfileStats,
    topTracks: List<AudioTrackItem>,
    favoriteTracks: List<AudioTrackItem>,
    playCounts: Map<String, Int>,
    onUpdateProfileName: (String) -> Unit,
    onPlayTrack: (AudioTrackItem) -> Unit,
    userProfile: CloudUserProfile? = null,
    cloudSyncActive: Boolean = false,
    isDeletingAccount: Boolean = false,
    onGoogleSignInSuccess: (uid: String, displayName: String, email: String?, photoUrl: String?) -> Unit = { _, _, _, _ -> },
    onSignOut: () -> Unit = {},
    onDeleteAccountAndCloudData: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var showEditName by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var authStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }
    var draftName by remember(stats.displayName) { mutableStateOf(stats.displayName) }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            containerColor = theme.cardColor,
            title = {
                Text(
                    text = "Eliminar cuenta y respaldo en la nube",
                    color = theme.errorColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp
                )
            },
            text = {
                Text(
                    text = "Se eliminarán tu perfil de Firebase, presets sincronizados, listas en la nube y preferencias locales de sincronización. Tus archivos MP3/FLAC en el teléfono NO serán borrados.",
                    color = theme.textSecondaryColor,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            },
            confirmButton = {
                Button(
                    enabled = !isDeletingAccount,
                    onClick = {
                        onDeleteAccountAndCloudData(
                            {
                                showDeleteAccountDialog = false
                                authStatusMessage = "Cuenta y datos en la nube eliminados correctamente."
                            },
                            { err ->
                                showDeleteAccountDialog = false
                                authStatusMessage = err
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.errorColor, contentColor = Color.White),
                    modifier = Modifier.testTag("profile_confirm_delete_account_button")
                ) {
                    Text(if (isDeletingAccount) "Eliminando..." else "Eliminar definitivamente", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = { showDeleteAccountDialog = false }
                ) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            containerColor = theme.cardColor,
            title = { Text("Nombre de tu Cabina", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Nombre local") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        focusedTextColor = theme.textColor,
                        unfocusedTextColor = theme.textColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfileName(draftName)
                        showEditName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp)
            .testTag("profile_view"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "profile_header_card") {
            GlassStudioCard(theme = theme, highlightBorder = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_car_audio_logo_1795273778926),
                        contentDescription = "Avatar Cabina",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, theme.primaryColor, CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stats.displayName, color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Modo local sin cuenta obligatoria", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("LM Produce — Producción de Luis Miguel", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                    }
                    IconButton(
                        onClick = { showEditName = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("profile_edit_name_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar nombre", tint = theme.accentColor)
                    }
                }
            }
        }

        item(key = "profile_cloud_sync_card") {
            val isSignedIn = userProfile != null || cloudSyncActive
            GlassStudioCard(
                theme = theme,
                highlightBorder = isSignedIn,
                modifier = Modifier.testTag("profile_cloud_sync_card")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isSignedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isSignedIn) theme.accentColor else theme.textSecondaryColor
                            )
                            Column {
                                Text(
                                    text = "Cuenta y Respaldo en la Nube (Opcional)",
                                    color = theme.textColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isSignedIn) {
                                        "Conectado: ${userProfile?.email ?: userProfile?.displayName ?: "Cuenta Google"}"
                                    } else {
                                        "Todas las funciones DSP son locales y gratuitas. Inicia sesión solo si deseas respaldo en la nube."
                                    },
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        SmartStatusLedBadge(
                            status = if (isSignedIn) SmartLightStatus.ACTIVE_READY else SmartLightStatus.INFO_NEUTRAL,
                            text = if (isSignedIn) "SYNC ON" else "LOCAL",
                            theme = theme
                        )
                    }

                    if (!authStatusMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.backgroundColor,
                            border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = authStatusMessage.orEmpty(),
                                color = theme.textColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }

                    if (!isSignedIn) {
                        Button(
                            enabled = !isSigningIn,
                            onClick = {
                                if (FirebaseApp.getApps(context).isEmpty()) {
                                    authStatusMessage = "Firebase no está inicializado en esta compilación."
                                    return@Button
                                }
                                isSigningIn = true
                                authStatusMessage = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isSigningIn = false
                                        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
                                        if (user != null) {
                                            onGoogleSignInSuccess(
                                                user.uid,
                                                user.displayName ?: stats.displayName,
                                                user.email,
                                                user.photoUrl?.toString()
                                            )
                                            authStatusMessage = "Respaldo en Firebase sincronizado correctamente."
                                        }
                                    },
                                    onAuthError = { err ->
                                        isSigningIn = false
                                        authStatusMessage = err
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isSigningIn = false
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor,
                                contentColor = theme.onPrimaryColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_google_signin_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = theme.onPrimaryColor
                                )
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSigningIn) "Conectando con Google..." else "Iniciar sesión con Google (Opcional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    signOutFromGoogle(
                                        credentialManager = credentialManager,
                                        scope = scope,
                                        onSignOutComplete = {
                                            onSignOut()
                                            authStatusMessage = "Sesión cerrada. El modo local sigue completo."
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.cardColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("profile_signout_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cerrar sesión", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                enabled = !isDeletingAccount,
                                onClick = { showDeleteAccountDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.errorColor.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("profile_delete_account_button")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = theme.errorColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Eliminar cuenta", color = theme.errorColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item(key = "profile_stats_grid") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatCard(
                        title = "Reproducciones",
                        value = "${stats.totalPlays}",
                        icon = Icons.Default.Headphones,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        title = "Tiempo de escucha",
                        value = formatMinutesSecondsClock(stats.totalListeningSeconds.toInt()),
                        icon = Icons.Default.Timer,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatCard(
                        title = "Favoritas",
                        value = "${stats.favoritesCount}",
                        icon = Icons.Default.Favorite,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        title = "Descargas / Presets",
                        value = "${stats.downloadsCount} / ${stats.customPresetsCount}",
                        icon = Icons.Default.PlayArrow,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (topTracks.isNotEmpty()) {
            item(key = "profile_top_header") {
                Text("Canciones más escuchadas", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            items(topTracks.take(6), key = { "top_${it.stableKey}" }) { tr ->
                val plays = playCounts[tr.stableKey] ?: playCounts[tr.id] ?: 0
                GlassStudioCard(
                    theme = theme,
                    modifier = Modifier.clickable { onPlayTrack(tr) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackArtwork(track = tr, theme = theme, size = 40.dp, cornerRadius = 8.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(tr.artist, color = theme.textSecondaryColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("$plays rep.", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        if (favoriteTracks.isNotEmpty()) {
            item(key = "profile_favs_header") {
                Text("Tus favoritas recientes", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            items(favoriteTracks.take(4), key = { "pfav_${it.stableKey}" }) { tr ->
                GlassStudioCard(
                    theme = theme,
                    modifier = Modifier.clickable { onPlayTrack(tr) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackArtwork(track = tr, theme = theme, size = 38.dp, cornerRadius = 8.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr.title, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(tr.artist, color = theme.textSecondaryColor, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    GlassStudioCard(
        theme = theme,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                Text(title, color = theme.textSecondaryColor, fontSize = 10.5.sp)
            }
            Text(value, color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}
