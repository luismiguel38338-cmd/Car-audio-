package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CloudPlaylist
import com.example.model.CloudSoundPreset
import com.example.model.CloudUserProfile
import com.example.model.OperationType
import com.example.model.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseUserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión con Google antes de acceder a Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<CloudUserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        CloudUserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path, auth)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String?,
        photoUrl: String?,
        defaultThemeId: String = "graphite_studio"
    ): Result<CloudUserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }

        if (existingSnap.exists()) {
            val profile = existingSnap.toObject(
                CloudUserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            if (profile != null) {
                val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
                val cleanName = displayName.trim().ifEmpty { profile.displayName }.take(80)
                if (cleanName != profile.displayName) updates["displayName"] = cleanName
                if (!email.isNullOrBlank() && email.take(160) != profile.email) updates["email"] = email.take(160)
                if (!photoUrl.isNullOrBlank() && photoUrl.take(500) != profile.photoUrl) updates["photoUrl"] = photoUrl.take(500)
                if (updates.size > 1) {
                    try {
                        docRef.update(updates).await()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
                        throw e
                    }
                }
                return@runCatching profile
            }
        }

        val initial = CloudUserProfile(
            id = uid,
            userId = uid,
            displayName = displayName.trim().ifEmpty { "Oyente" }.take(80),
            email = email?.take(160),
            photoUrl = photoUrl?.take(500),
            vipStatus = "free",
            vipPromoDismissed = false,
            onboardingCompleted = false,
            themeId = defaultThemeId
        )
        try {
            docRef.set(initial.toCreateMap()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
        initial
    }

    suspend fun updateProfilePreferences(
        themeId: String? = null,
        vipPromoDismissed: Boolean? = null,
        onboardingCompleted: Boolean? = null,
        lastTrackKey: String? = null,
        lastTrackTitle: String? = null,
        lastTrackArtist: String? = null,
        lastPositionMs: Long? = null,
        favoriteTrackKeys: List<String>? = null,
        queueTrackKeys: List<String>? = null
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
        if (themeId != null) updates["themeId"] = themeId.take(60)
        if (vipPromoDismissed != null) updates["vipPromoDismissed"] = vipPromoDismissed
        if (onboardingCompleted != null) updates["onboardingCompleted"] = onboardingCompleted
        if (lastTrackKey != null) updates["lastTrackKey"] = lastTrackKey.take(600)
        if (lastTrackTitle != null) updates["lastTrackTitle"] = lastTrackTitle.take(160)
        if (lastTrackArtist != null) updates["lastTrackArtist"] = lastTrackArtist.take(120)
        if (lastPositionMs != null) updates["lastPositionMs"] = lastPositionMs.coerceIn(0L, 86_400_000L)
        if (favoriteTrackKeys != null) updates["favoriteTrackKeys"] = favoriteTrackKeys.take(200)
        if (queueTrackKeys != null) updates["queueTrackKeys"] = queueTrackKeys.take(150)

        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    fun observeSoundPresets(userId: String): Flow<List<CloudSoundPreset>> = flow {
        val colRef = db.collection("users").document(userId).collection("soundPresets")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudSoundPreset::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path, auth)
                    }
                    throw error
                }
        )
    }

    suspend fun saveSoundPreset(preset: CloudSoundPreset): Result<String> = runCatching {
        val uid = requireUserId()
        val presetId = preset.id.ifBlank { "preset_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("users").document(uid).collection("soundPresets").document(presetId)
        val payload = preset.copy(id = presetId, userId = uid)
        try {
            if (preset.id.isBlank() || preset.createdAt == null) {
                docRef.set(payload.toCreateMap()).await()
            } else {
                docRef.update(payload.toUpdateMap()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
        presetId
    }

    suspend fun deleteSoundPreset(presetId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(presetId.isNotBlank()) { "Preset ID cannot be blank" }
        val docRef = db.collection("users").document(uid).collection("soundPresets").document(presetId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    fun observePlaylists(userId: String): Flow<List<CloudPlaylist>> = flow {
        val colRef = db.collection("users").document(userId).collection("playlists")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudPlaylist::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path, auth)
                    }
                    throw error
                }
        )
    }

    suspend fun savePlaylist(playlist: CloudPlaylist): Result<String> = runCatching {
        val uid = requireUserId()
        val playlistId = playlist.id.ifBlank { "pl_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
        val payload = playlist.copy(id = playlistId, userId = uid)
        try {
            if (playlist.id.isBlank() || playlist.createdAt == null) {
                docRef.set(payload.toCreateMap()).await()
            } else {
                docRef.update(payload.toUpdateMap()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
        playlistId
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(playlistId.isNotBlank()) { "Playlist ID cannot be blank" }
        val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    suspend fun deleteAllUserData(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        try {
            val presetsSnap = userRef.collection("soundPresets").get().await()
            val playlistsSnap = userRef.collection("playlists").get().await()
            val batch = db.batch()
            presetsSnap.documents.forEach { batch.delete(it.reference) }
            playlistsSnap.documents.forEach { batch.delete(it.reference) }
            batch.delete(userRef)
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userRef.path, auth)
            throw e
        }
    }
}
