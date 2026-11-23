# CAR AUDIO DSP PRO — v6.5.0 (Potencia tu Música)

**Marca:** LM Produce — Producción de Luis Miguel  
**Plataforma:** Android 7.0+ (API 24+) · Target / Compile SDK 36  
**Estilo:** NEON DSP PRO (Negro OLED `#06070B` + Azul Eléctrico `#00D4FF` + Violeta Neón `#A855F7`)

Aplicación nativa para Android (**Kotlin + Jetpack Compose Material 3**) diseñada como reproductor musical real y estudio DSP automotriz profesional optimizado para teléfonos de gama baja y media (incluyendo **TECNO BG6** y **Android 13+**).

---

## Características Principales (v6.5.0)

1. **Acceso Directo y Cero Sonidos Automáticos**
   - Entrada directa a la cabina sin inicio de sesión obligatorio ni pantallas de bloqueo.
   - Sin sonidos automáticos al arrancar la aplicación.

2. **Reproductor Musical Completo (Media3 + MediaSession)**
   - Escaneo automático de música local (`MediaStore`) y carga mediante el selector de archivos de Android (MP3, FLAC, WAV, AAC, OGG).
   - Controles completos: reproducir/pausar, anterior/siguiente, barra de progreso interactiva, volumen, aleatorio (`Shuffle`), repetir (`Off / Todo / 1`), cola de reproducción, favoritas, listas, reproducción en segundo plano y pantalla bloqueada.

3. **Ecualizador DSP Profesional + Sistema de Bajos y Subwoofer 3D**
   - **10 bandas**, **31 bandas ISO** y **Paramétrico de 5 bandas**, más balance L/R, amplitud estéreo, ganancia master, loudness, compresor y limitador anti-clipping.
   - **10 presets por género calibrados:** *Dembow dominicano, Reggaetón, Bachata, Merengue, Trap, Hip-Hop, Electrónica, Rock, Música vocal* y *Personalizado* (junto a presets de estudio).
   - **Bass Boost (Normal / Potente / Extremo)** con protección anti-clipping y **Subwoofer 3D** con altavoces reactivos al ritmo.

4. **Música Online Legal (Jamendo API v3 + YouTube Music) y Descargas Offline**
   - Catálogo real libre de derechos mediante la API oficial de **Jamendo** (requiere configurar `JAMENDO_CLIENT_ID` en Secrets de AI Studio / `.env`).
   - Sección oficial para búsquedas y accesos directos favoritos a **YouTube Music** sin extracción ilegal.
   - Gestor real de **Descargas Offline** con progreso en porcentaje, pausa/reanudar HTTP `Range`, cancelación, eliminación y verificación de licencia (`audiodownload_allowed`).

5. **Visualizador RTA, Perfil Local y Acerca de**
   - Espectro RTA de 31 bandas, osciloscopio, medidores VU L/R, modo visual ligero para ahorro de RAM/batería en TECNO BG6 y estadísticas locales del usuario.

---

## Configuración de Claves API (Secrets) y Compilación

Copia `.env.example` a `.env` o define `JAMENDO_CLIENT_ID` en el panel **Secrets** de AI Studio:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

El APK generado queda en:
```
app/build/outputs/apk/debug/app-debug.apk
```
