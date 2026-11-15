# Car Audio DSP Pro — v5.0.0 (Edición Todo Rojo)

**Versión:** `v5.0.0` (`versionCode 50000`)  
**Plataforma:** Android 7.0+ (API 24+) · Target SDK 36  
**Arquitectura:** Kotlin + Jetpack Compose Material 3 + AudioFX (`DynamicsProcessing`, `Equalizer`, `BassBoost`, `Virtualizer`)

---

## Novedades de esta actualización (v5.0.0)

### 1. Estética "Todo Rojo" y Nuevo Logo Crimson Studio
- **Logo original y llamativo:** Nuevo emblema carmesí de alto contraste diseñado para pantallas AMOLED y consolas de vehículo.
- **Compatibilidad universal en cualquier teléfono:** Iconos adaptativos (`mipmap-anydpi-v26`) y PNG multi-densidad (`mdpi` a `xxxhdpi`) cuadrados y circulares (`ic_launcher.png` / `ic_launcher_round.png`).
- **Tema Rojo Carmesí completo:** Barra superior, navegación inferior, deslizadores, curva de respuesta EQ, analizador de barras, pantalla de arranque y ventana flotante unificados en rojo (`#FF172B` / `#FF5252` sobre fondo negro carmesí `#090203`).

### 2. Carga de Música desde tu Celular + Modificador en el Mismo Panel
- **Botón `VER MI MÚSICA` y `CARGAR MÚSICA`:** Escanea las canciones guardadas en tu teléfono (`MediaStore`) o abre archivos MP3/WAV/FLAC directamente **en el mismo panel principal (`MÚSICA`)**.
- **Modificador en vivo integrado:** Ecualiza y potencia cada pista mientras suena sin cambiar de pestaña:
  - Presets de 1 toque (`PUNCH BASS`, `CLUB`, `ROCK`, `YOUTUBE VOCAL`, `FLAT`).
  - Control directo de **Bass Boost (0 a 24 dB)** y **Preamp / Potencia (-12 a +18 dB)**.
  - Ecualizador rápido de **5 bandas** (`60Hz`, `230Hz`, `910Hz`, `3.6k`, `14k`).

### 3. Limpieza Total: Solo lo Funcional, Cero Relleno
- Se redujo la barra inferior a **4 pestañas esenciales**: `MÚSICA`, `EQ`, `DSP`, `AJUSTES`.
- Se eliminaron funciones decorativas, telemetría ficticia y vistas redundantes que no aportaban control real de audio.
- Todos los controles están conectados al motor **AudioFX en tiempo real**:
  - **EQ de Precisión:** 31 bandas ISO, 15 bandas gráficas, Paramétrico de 5 bandas y guardado de presets propios.
  - **DSP Studio:** Ganancia Master, Bass/Mid/Treble, Bass Boost, Amplitud Estéreo, Balance L/R, Compresor multibanda, Limitador anti-clip y filtros HPF / LPF.

### 4. Pantalla Virtual Flotante Roja (YouTube en Vivo)
- Al salir de la app o pulsar el botón flotante en la barra superior, aparece un **círculo rojo abierto flotante** sobre YouTube u otras aplicaciones.
- Toca el círculo para desplegar el mini ecualizador rojo en tiempo real con **Bass Boost**, **Potencia**, **5 bandas** y **presets**.

---

## Publicar Release v5.0.0 en GitHub

```bash
git add .
git commit -m "Release v5.0.0: Edicion Todo Rojo, nuevo logo, carga musical en panel y capturas"
git tag -a v5.0.0 -m "Car Audio DSP Pro v5.0.0 - Edicion Todo Rojo"
git push origin main --tags
```

El workflow `.github/workflows/build-apk.yml` compilará automáticamente el paquete `CarAudio-DSP-Pro-v5.0.0.apk` y adjuntará estas notas en GitHub Releases.
