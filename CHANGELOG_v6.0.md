# Registro de Cambios — Car Audio DSP Pro v6.0

Todas las modificaciones conservan al 100% las pantallas, servicios, controles, configuraciones y estética roja/negra ("Todo Rojo") existentes.

## Fase 1: Auditoría, Versionado y Limpieza de Dependencias
- Documentado inventario completo de funciones y diagnóstico de rendimiento en `ARCHITECTURE_v6.0.md`.
- Actualizado `versionCode = 60000` y `versionName = "6.0.0"` en `app/build.gradle.kts`.
- Comentadas dependencias no utilizadas en runtime (`room`, `retrofit`, `moshi`, `okhttp`, `firebase-ai`, `firebase-appcheck`) y sus compiladores KSP para reducir peso del APK, tiempo de arranque en frío y consumo base de RAM.
- Preservadas intactas las configuraciones de firma (`signingConfigs`, `debugConfig`, `release`) y `debug.keystore`.

## Fase 2: Modelos y Motor DSP de Alta Precisión (Sin Cortes)
- Marcados `@Immutable` en `AudioModels.kt` y `EqualizerModels.kt` para evitar recomposiciones redundantes en Compose.
- Añadidos modelos `RepeatMode`, `OutputKind`, `OutputDeviceStatus`, `AudioSessionTarget` y propiedad `isFavorite` en `AudioTrackItem`.
- Mejorado `CarAudioEngine.kt`:
  - Separación geométrica ISO de 1/3 de octava (`computeIsoUpperCutoffHz = sqrt(f_i * f_{i+1})`) para las 31 bandas.
  - Curvas paramétricas gaussianas/sigmoidales (Peaking, Low Shelf, High Shelf, Notch), pendientes Butterworth HPF/LPF suaves y compensación inteligente anti-clipping cuando el limitador está activo.
  - Reutilización de etapas `DynamicsProcessing.Eq`, `DynamicsProcessing.Mbc` y `DynamicsProcessing.Limiter` (API 28+) + coalescencia hardware de 16 ms (`fxLock`) y `applyQuickPreset()` atómico para arrastrar controles sin cortes ni clics.
  - Manejo completo de `AudioFocus`, `ACTION_AUDIO_BECOMING_NOISY` (pausa segura al desconectar audífonos/BT) y `AudioDeviceCallback` para detección en vivo de Bluetooth, USB DAC, Auriculares y Altavoz.
  - Búferes pre-asignados cero-asignaciones (`oscBuffer`, `waveformBuffer`, `fftMagnitudesBuffer`, `rtaLevelsDb`, `rtaPeaksDb`) y pausa automática del `Visualizer` en segundo plano.

## Fase 3: Reproducción en Segundo Plano, Carátulas y Notificaciones
- Creado `AlbumArtCache.kt` con caché `LruCache` (24 carátulas, `RGB_565` submuestreado en `Dispatchers.IO`) y liberación garantizada de `MediaMetadataRetriever`.
- Añadida notificación `Notification.MediaStyle` en `CarAudioNotificationHelper.kt` con controles de transporte (Anterior, Play/Pausa, Siguiente, Cerrar) y carátula incrustada.
- Creado y registrado `CarAudioPlaybackService.kt` (`MediaSession` + soporte AVRCP en consola de vehículo y pantalla bloqueada).

## Fase 4: ViewModel, Persistencia y Funciones de Biblioteca
- Actualizado `CarAudioViewModel.kt`:
  - Detección de dispositivos con poca RAM (`ActivityManager.isLowRamDevice`) + ajuste `liteVisualsEnabled`.
  - Bucle de telemetría consciente del ciclo de vida (`setUiActive`), reduciendo frecuencia en segundo plano o modo ligero.
  - Persistencia completa en `SharedPreferences` (`apply()`): tema, DSP, EQ 31/15 bandas, presets personalizados, favoritos, aleatorio (`shuffle`) y repetición (`repeatMode`).
  - Búsqueda en vivo (`searchQuery`), filtros (`Todas`, `Favoritas`, `Cargadas`), carátulas de álbum (`MediaStore.Audio.Media.ALBUM_ID`), interpolación suave 15-a-31 bandas y sincronización continua con `CarAudioPlaybackService`.

## Fase 5: Optimización de Interfaz Jetpack Compose y Accesibilidad
- Arranque optimizado en `CarAudioSplashScreen.kt` (~400 ms) con soporte `liteVisuals`.
- Caché de gradientes (`drawWithCache` / `remember`) en `DspMasterView.kt`, `EqCurveCanvas31` de `EqualizerProcessorView.kt` y tarjetas de `PlayerView.kt`.
- Actualizado `PlayerView.kt` con carátula en disco, chip de dispositivo de salida/sesión, controles Shuffle/Repeat/Favoritos, barra de búsqueda, filtros y lista `LazyColumn` con `key` + `contentType`.
- Actualizado `AppSettingsView.kt` con interruptor de Modo Ligero (baja RAM) y tarjeta informativa transparente sobre compatibilidad AudioFX en YouTube/apps externas.
- Actualizado `MainActivity.kt` con `collectAsStateWithLifecycle()`, observador de ciclo de vida (`ON_START`/`ON_STOP`), `Crossfade` adaptativo y contenedor `BoxWithConstraints` (`widthIn(max = 840.dp)`) para tablets y pantallas de auto, además de áreas táctiles >= 48dp.
- Actualizado badge de versión a `v6.0.0` en `FloatingEqualizerService.kt`.

## Fase 6: Pruebas Automatizadas y Capturas Verificadas
- Creado `CarAudioEngineTest.kt` (pruebas unitarias DSP de límites ISO 31 bandas, bypass, rolloff Butterworth HPF/LPF, curvas paramétricas y persistencia Robolectric del ViewModel).
- Actualizado `GreetingScreenshotTest.kt` con 5 capturas Roborazzi (`01_musica_panel.png` a `05_ajustes_pro.png`).

## Fase 7: Seguridad, ProGuard y Publicación GitHub
- Endurecido `app/proguard-rules.pro` para proteger Activity, Services, Receivers, ViewModels y modelos.
- Actualizado `.github/workflows/build-apk.yml` (rama `v6.0`, ejecución de pruebas `testDebugUnitTest` y APK `CarAudio-DSP-Pro-v6.0.0.apk`).
- Actualizados `README.md`, `RELEASE_NOTES.md` y `CHANGELOG_v6.0.md`.
