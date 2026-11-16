const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-caraudio-5d2a4385-604b-45bc-9b57-672ffe39a918";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid) {
  return {
    userId: uid,
    displayName: "Alice Listener",
    email: "alice@example.com",
    vipStatus: "free",
    vipPromoDismissed: false,
    onboardingCompleted: true,
    themeId: "graphite_studio",
    createdAt: new Date(),
    updatedAt: new Date(),
  };
}

function validPresetPayload(uid) {
  return {
    userId: uid,
    name: "Studio Clean",
    description: "Warm balanced mobile profile",
    eq10Bands: [1.0, 1.5, 0.5, 0.0, 0.0, 0.5, 1.0, 1.5, 2.0, 1.5],
    eq31Bands: [],
    preampDb: 0.0,
    bassBoostDb: 3.0,
    midGainDb: 0.5,
    trebleGainDb: 1.5,
    stereoBalance: 0.0,
    virtualizerPercent: 30.0,
    compressorEnabled: false,
    limiterEnabled: true,
    hpfHz: 28.0,
    lpfHz: 18500.0,
    subsonicHz: 25.0,
    createdAt: new Date(),
    updatedAt: new Date(),
  };
}

function validPlaylistPayload(uid) {
  return {
    userId: uid,
    name: "Night Drive Mix",
    trackKeys: ["track_1", "track_2"],
    createdAt: new Date(),
    updatedAt: new Date(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profiles, presets, or playlists", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("soundPresets").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("playlists").get());
});

test("Authenticated owner can create, update, and delete own profile, while VIP escalation and Ghost fields are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  // Reject self-granted VIP on create
  await assertFails(profileRef.set({ ...validProfilePayload(ALICE_UID), vipStatus: "vip" }));

  // Reject ghost field on create
  await assertFails(profileRef.set({ ...validProfilePayload(ALICE_UID), isAdmin: true }));

  // Valid create succeeds
  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID)));

  // Valid partial update succeeds
  await assertSucceeds(profileRef.update({
    vipPromoDismissed: true,
    themeId: "silver_ice",
    updatedAt: new Date(),
  }));

  // Reject client attempting to escalate vipStatus on update
  await assertFails(profileRef.update({
    vipStatus: "vip",
    updatedAt: new Date(),
  }));

  // Delete own account document succeeds
  await assertSucceeds(profileRef.delete());
});

test("Cross-user isolation prevents Bob from reading, listing, or modifying Alice's data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("soundPresets").doc("preset_1").set(validPresetPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_1").set(validPlaylistPayload(ALICE_UID))
  );

  // Blanket user collection list denied even to authenticated user
  await assertFails(bobDb.collection("users").get());
  await assertFails(aliceDb.collection("users").get());

  // Cross-user read & write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).delete());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("soundPresets").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("soundPresets").doc("preset_2").set(validPresetPayload(BOB_UID))
  );
});

test("SoundPreset and Playlist validation enforces schema bounds and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const presetRef = aliceDb.collection("users").doc(ALICE_UID).collection("soundPresets").doc("preset_custom");
  const playlistRef = aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("playlist_custom");

  // Invalid EQ array length fails
  await assertFails(presetRef.set({ ...validPresetPayload(ALICE_UID), eq10Bands: [0, 1] }));
  // Out-of-range preamp fails
  await assertFails(presetRef.set({ ...validPresetPayload(ALICE_UID), preampDb: 40 }));

  await assertSucceeds(presetRef.set(validPresetPayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("soundPresets").get());
  await assertSucceeds(presetRef.update({ name: "Updated Preset", updatedAt: new Date() }));

  // Invalid playlist name fails
  await assertFails(playlistRef.set({ ...validPlaylistPayload(ALICE_UID), name: "" }));
  await assertSucceeds(playlistRef.set(validPlaylistPayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("playlists").get());
  await assertSucceeds(playlistRef.delete());
});
