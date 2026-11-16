package com.example.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val currentUser = auth.currentUser
    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class CloudUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Oyente",
    val email: String? = null,
    val photoUrl: String? = null,
    val vipStatus: String = "free",
    val vipPromoDismissed: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val themeId: String = "graphite_studio",
    val lastTrackKey: String? = null,
    val lastTrackTitle: String? = null,
    val lastTrackArtist: String? = null,
    val lastPositionMs: Long = 0L,
    val favoriteTrackKeys: List<String> = emptyList(),
    val queueTrackKeys: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isVip: Boolean get() = vipStatus == "vip"

    fun toCreateMap(): Map<String, Any> {
        val raw = mapOf(
            "userId" to userId,
            "displayName" to displayName.trim().ifEmpty { "Oyente" }.take(80),
            "email" to email?.take(160),
            "photoUrl" to photoUrl?.take(500),
            "vipStatus" to "free",
            "vipPromoDismissed" to vipPromoDismissed,
            "onboardingCompleted" to onboardingCompleted,
            "themeId" to themeId.take(60),
            "lastTrackKey" to lastTrackKey?.take(600),
            "lastTrackTitle" to lastTrackTitle?.take(160),
            "lastTrackArtist" to lastTrackArtist?.take(120),
            "lastPositionMs" to lastPositionMs.coerceIn(0L, 86_400_000L),
            "favoriteTrackKeys" to favoriteTrackKeys.take(200),
            "queueTrackKeys" to queueTrackKeys.take(150),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        @Suppress("UNCHECKED_CAST")
        return raw.filterValues { it != null } as Map<String, Any>
    }
}

@IgnoreExtraProperties
data class CloudSoundPreset(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val eq10Bands: List<Double> = List(10) { 0.0 },
    val eq31Bands: List<Double> = emptyList(),
    val preampDb: Double = 0.0,
    val bassBoostDb: Double = 0.0,
    val midGainDb: Double = 0.0,
    val trebleGainDb: Double = 0.0,
    val stereoBalance: Double = 0.0,
    val virtualizerPercent: Double = 0.0,
    val compressorEnabled: Boolean = false,
    val limiterEnabled: Boolean = true,
    val hpfHz: Double = 25.0,
    val lpfHz: Double = 18000.0,
    val subsonicHz: Double = 25.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "name" to name.trim().ifEmpty { "Mi Preset" }.take(60),
        "description" to description.take(200),
        "eq10Bands" to if (eq10Bands.size == 10) eq10Bands else List(10) { 0.0 },
        "eq31Bands" to if (eq31Bands.size == 31) eq31Bands else emptyList(),
        "preampDb" to preampDb.coerceIn(-18.0, 18.0),
        "bassBoostDb" to bassBoostDb.coerceIn(0.0, 24.0),
        "midGainDb" to midGainDb.coerceIn(-18.0, 18.0),
        "trebleGainDb" to trebleGainDb.coerceIn(-18.0, 18.0),
        "stereoBalance" to stereoBalance.coerceIn(-1.0, 1.0),
        "virtualizerPercent" to virtualizerPercent.coerceIn(0.0, 100.0),
        "compressorEnabled" to compressorEnabled,
        "limiterEnabled" to limiterEnabled,
        "hpfHz" to hpfHz.coerceIn(15.0, 500.0),
        "lpfHz" to lpfHz.coerceIn(200.0, 22000.0),
        "subsonicHz" to subsonicHz.coerceIn(15.0, 80.0),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "name" to name.trim().ifEmpty { "Mi Preset" }.take(60),
        "description" to description.take(200),
        "eq10Bands" to if (eq10Bands.size == 10) eq10Bands else List(10) { 0.0 },
        "eq31Bands" to if (eq31Bands.size == 31) eq31Bands else emptyList(),
        "preampDb" to preampDb.coerceIn(-18.0, 18.0),
        "bassBoostDb" to bassBoostDb.coerceIn(0.0, 24.0),
        "midGainDb" to midGainDb.coerceIn(-18.0, 18.0),
        "trebleGainDb" to trebleGainDb.coerceIn(-18.0, 18.0),
        "stereoBalance" to stereoBalance.coerceIn(-1.0, 1.0),
        "virtualizerPercent" to virtualizerPercent.coerceIn(0.0, 100.0),
        "compressorEnabled" to compressorEnabled,
        "limiterEnabled" to limiterEnabled,
        "hpfHz" to hpfHz.coerceIn(15.0, 500.0),
        "lpfHz" to lpfHz.coerceIn(200.0, 22000.0),
        "subsonicHz" to subsonicHz.coerceIn(15.0, 80.0),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@IgnoreExtraProperties
data class CloudPlaylist(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val trackKeys: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "name" to name.trim().ifEmpty { "Lista" }.take(80),
        "trackKeys" to trackKeys.take(200),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "name" to name.trim().ifEmpty { "Lista" }.take(80),
        "trackKeys" to trackKeys.take(200),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
