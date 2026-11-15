# Car Audio DSP Pro — v6.0.0 (Alto Rendimiento & Alta Fidelidad)

**Versión:** `v6.0.0` (`versionCode 60000`)  
**Plataforma:** Android 7.0+ (API 24+) · Target SDK 36  
**Arquitectura:** Kotlin + Jetpack Compose Material 3 + MediaSession + AudioFX (`DynamicsProcessing`, `Equalizer`, `BassBoost`, `Virtualizer`)

---

## Novedades de esta actualización (v6.0.0)

### 1. Velocidad Extrema y Menor Consumo (RAM y Batería)
- **Arranque y compilación optimizados:** Limpieza de dependencias no utilizadas en runtime y carga de carátulas en segundo plano (`Dispatchers.IO`) con caché `AlbumArtCache` (`LruCache` RGB_565).
- **Compose sin tirones:** Anotaciones `@Immutable` en modelos de audio/EQ, `collectAsStateWithLifecycle()`, `LazyColumn` con `key` y `contentType`, curvas precalculadas con `drawWithCache` y `Crossfade` ágil.
- **Cero asignaciones en FFT / RTA:** Reutilización de búferes de espectro y pausa automática del `Visualizer` al minimizar la app o cambiar a otra pestaña.
- **Modo Ligero para teléfonos modestos:** Detección automática de dispositivos de poca RAM (`isLowRamDevice`) + interruptor manual en `AJUSTES`.

### 2. Calidad de Audio DSP Mejorada (Sin Cortes)
- **31 Bandas ISO + 15 Bandas + Paramétrico 5 Bandas:** Frecuencias de corte geométricas precisas (`sqrt(f_i * f_{i+1})`), curvas gaussianas/sigmoidales (`Peaking`, `Low Shelf`, `High Shelf`, `Notch`) e interpolación suave 15-a-31 bandas.
- **Protección Anti-Clipping y Headroom:** Limitador inteligente + etapas cacheadas `DynamicsProcessing` + coalescencia de envíos hardware de 16 ms para mover controles en vivo sin clics ni cortes.
- **Filtros HPF / LPF Butterworth:** Roll-off progresivo y suave para proteger subwoofers y medios.
- **Enrutamiento y Foco de Audio:** Manejo de `AudioFocus`, pausa automática al desconectar auriculares/Bluetooth (`ACTION_AUDIO_BECOMING_NOISY`) y detección en tiempo real de salida (**Bluetooth**, **USB Audio**, **Auriculares**, **Altavoz**).

### 3. Reproductor Completo en Segundo Plano
- **Carátulas, Búsqueda y Filtros:** Carga de arte incrustado/álbum en el reproductor, buscador en vivo y filtros (`Todas`, `Favoritas`, `Cargadas`).
- **Controles de Cola y MediaSession:** Botones **Aleatorio (Shuffle)**, **Repetir (Off / Todo / 1)**, **Favoritos**, servicio en primer plano `CarAudioPlaybackService` y notificación `MediaStyle` para pantalla bloqueada y controles Bluetooth/volante.

### 4. Compatibilidad Transparente (Android 7–16)
- En tu música local cargada en la app, la cadena DSP + EQ funciona al 100% por hardware (`AudioFX Directo`).
- Para YouTube y apps externas mediante la **Pantalla Virtual Flotante Roja**, la app se enlaza a la sesión compartida cuando el sistema y el reproductor externo exponen `audioSessionId` (sin prometer procesamiento si la app externa lo bloquea).

---

## Publicar Rama v6.0 y Release v6.0.0 en GitHub

```bash
git checkout -b v6.0
git add .
git commit -m "Release v6.0.0: Optimizacion de rendimiento, motor DSP mejorado y MediaSession"
git tag -a v6.0.0 -m "Car Audio DSP Pro v6.0.0"
git push -u origin v6.0 --tags
```

El workflow `.github/workflows/build-apk.yml` ejecutará las pruebas unitarias/Robolectric (`testDebugUnitTest`), compilará `CarAudio-DSP-Pro-v6.0.0.apk` y adjuntará estas notas en GitHub Releases.
