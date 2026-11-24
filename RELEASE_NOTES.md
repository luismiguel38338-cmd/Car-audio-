# Car Audio DSP Pro — v6.5.0 (Potencia tu Música • Epic Violet DSP Pro)

**Versión:** `v6.5.0` (`versionCode 65000`)  
**Autor / Marca:** **LM Produce — Producción de Luis Miguel** (`luismiguel38338@gmail.com`)  
**Plataforma:** Android 7.0+ (API 24+) · Target / Compile SDK 36  
**Motor:** Kotlin + Jetpack Compose Material 3 + Media3 / MediaSession + AudioFX (`DynamicsProcessing`, `Equalizer`, `BassBoost`, `Virtualizer`)

---

## Novedades Principales de v6.5.0

### 1. Experiencia Reproductor del Teléfono + Cabina 3D
- **Inicio rápido centrado en tu música:** Acceso inmediato a tus canciones locales recientes, favoritas, listas, accesos a **Mi Música**, **Música Online**, **Descargas** y botones directos de **Modo de Graves (Normal / Potente / Extremo)** sin fricción.
- **Acceso directo real:** Sin pantallas de bloqueo ni inicio de sesión obligatorio, y sin sonidos automáticos al arrancar la app.
- **Reproductor Media3 completo:** Reproducir/pausar, anterior/siguiente, barra de progreso interactiva, control de volumen, modo **Aleatorio (`Shuffle`)**, **Repetir (`Off / Todo / 1`)**, cola reordenable con ventana optimizada, **Temporizador de apagado**, carátulas en caché (`AlbumArtCache`) y controles en pantalla bloqueada y notificación `MediaStyle`.

### 2. Rediseño Visual Epic Violet DSP Pro (Glassmorphism OLED)
- **Estética comercial de alta gama:** Fondo negro OLED profundo (`#05040A`), tarjetas de vidrio translúcido con reflejos y bordes luminosos en **Violeta Neón (`#A855F7`)**, **Morado LED (`#C084FC`)** y detalles en **Plata Estudio (`#E2E8F0`)**.
- **Componentes 3D y medidores:** Perillas circulares 3D táctiles, cabina con subwoofers reactivos al ritmo, curva gráfica de frecuencias animada y medidores independientes de **Graves, Medios y Agudos**.
- **7 temas seleccionables en Ajustes:** Desde *Epic Violet DSP Pro* hasta perfiles discretos nocturnos anti-deslumbramiento.

### 3. Ecualizador DSP Real + Bass Boost + Subwoofer
- **10 bandas + 31 bandas ISO de 1/3 de octava + Paramétrico de 5 bandas:** Procesamiento real por hardware con `DynamicsProcessing` (API 28+) y respaldo automático `Equalizer`, evitando el doble procesamiento.
- **Comparador instantáneo A / B:** Guarda y alterna dos configuraciones completas en vivo mientras escuchas tu música.
- **10 Presets calibrados + perfiles personales:** *Dembow dominicano, Reggaetón, Bachata, Merengue, Trap, Hip-Hop, Electrónica, Rock, Música vocal* y *Personalizado*.
- **Bass Boost (Normal / Potente / Extremo)** con limitador anti-clipping obligatorio en modo Extremo y pantalla dedicada **Subwoofer** con filtros subsónico y crossover `HPF / LPF`.

### 4. Música Online Legal (Jamendo API v3 + YouTube Music) y Descargas Offline
- **Jamendo API v3:** Búsqueda real de canciones, artistas y álbumes libres de derechos (requiere configurar `JAMENDO_CLIENT_ID` en Secrets / `.env`).
- **Descargas Offline reales:** Progreso porcentual en tiempo real, pausa y reanudación HTTP `Range`, cancelación, eliminación y verificación estricta de licencia (`audiodownload_allowed`).
- **YouTube Music oficial:** Búsquedas rápidas, historial y accesos directos favoritos hacia la app oficial sin extracción no autorizada.

### 5. Rendimiento para Teléfonos Modestos (TECNO BG6 y Android 7–16)
- **Modo Ligero (`liteVisuals`)** automático en dispositivos con poca RAM y configurable manualmente en Ajustes/Visualizador.
- Cero asignaciones en bucle FFT/RTA, pausa automática del `Visualizer` en segundo plano y manejo limpio de `AudioFocus` + desconexión segura de Bluetooth/auriculares.

---

## Paquetes Generados Automáticamente por GitHub Actions

Al publicar una etiqueta `v6.5.0` (o ejecutar el workflow **Build, Test & Multi-Platform Release**), el repositorio genera:
1. `CarAudio-DSP-Pro-v6.5.0.apk` — Instalador directo Android / GitHub Releases / Obtainium.
2. `CarAudio-DSP-Pro-v6.5.0-googleplay.aab` — Android App Bundle firmado para **Google Play Console** (cuando los Secrets de firma están configurados).
3. `CarAudio-DSP-Pro-v6.5.0-release-signed.apk` — APK Release firmado para **Samsung Galaxy Store**, **Amazon Appstore**, **Huawei AppGallery** y distribución directa.
4. `SHA256SUMS.txt` — Verificación criptográfica de integridad.
