package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.CloudPlaylist
import com.example.model.CloudSoundPreset
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FirebaseUserRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun authenticatedOwner_canCreateObserveUpdateAndDeleteProfilePresetsAndPlaylists() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FirebaseUserRepository(firestore, auth)

        val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfile(
                displayName = "Alice Studio",
                email = ALICE_EMAIL,
                photoUrl = null,
                defaultThemeId = "graphite_studio"
            )
        }
        assertTrue(profileResult.isSuccess)
        val profile = profileResult.getOrThrow()
        assertFalse(profile.isVip)

        val updatePrefResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateProfilePreferences(
                vipPromoDismissed = true,
                themeId = "silver_ice",
                lastTrackTitle = "Studio Track",
                lastPositionMs = 42000L
            )
        }
        assertTrue(updatePrefResult.isSuccess)

        val observedProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it != null && it.vipPromoDismissed }
        }
        assertNotNull(observedProfile)
        assertEquals("silver_ice", observedProfile?.themeId)
        assertEquals(42000L, observedProfile?.lastPositionMs)

        val presetId = "preset_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val presetSave = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveSoundPreset(
                CloudSoundPreset(
                    id = presetId,
                    name = "Referencia Nocturna",
                    description = "Perfil equilibrado",
                    eq10Bands = listOf(1.0, 1.5, 0.5, 0.0, 0.0, 0.5, 1.0, 1.5, 2.0, 1.5),
                    bassBoostDb = 4.0,
                    trebleGainDb = 2.0
                )
            )
        }
        assertTrue(presetSave.isSuccess)

        val observedPresets = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSoundPresets(uid).first { list -> list.any { it.id == presetId } }
        }
        assertTrue(observedPresets.any { it.name == "Referencia Nocturna" })

        val playlistId = "pl_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val playlistSave = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.savePlaylist(
                CloudPlaylist(
                    id = playlistId,
                    name = "Favoritas Hi-Res",
                    trackKeys = listOf("track_a", "track_b")
                )
            )
        }
        assertTrue(playlistSave.isSuccess)

        val observedPlaylists = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePlaylists(uid).first { list -> list.any { it.id == playlistId } }
        }
        assertTrue(observedPlaylists.any { it.name == "Favoritas Hi-Res" })

        // Delete all user data
        val deleteAll = withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteAllUserData() }
        assertTrue(deleteAll.isSuccess)
        val afterDelete = withTimeout(FLOW_TIMEOUT_MS) { repository.observeUserProfile(uid).first() }
        assertNull(afterDelete)
    }

    @Test
    fun crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirebaseUserRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserProfile("Alice", ALICE_EMAIL, null)
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirebaseUserRepository(firestore, auth)

        var caughtFirestoreException: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeUserProfile(aliceUid).first()
            }
        } catch (e: Exception) {
            caughtFirestoreException = e.unwrapFirestoreException()
        }
        assertNotNull("Expected FirebaseFirestoreException for cross-user read", caughtFirestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caughtFirestoreException?.code)
    }

    @Test
    fun unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirebaseUserRepository(firestore, auth)
        auth.signOut()

        var caughtFirestoreException: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeSoundPresets(aliceUid).first()
            }
        } catch (e: Exception) {
            caughtFirestoreException = e.unwrapFirestoreException()
        }
        assertNotNull("Expected FirebaseFirestoreException when unauthenticated", caughtFirestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caughtFirestoreException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_audio@test.com"
        const val BOB_EMAIL = "bob_audio@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
