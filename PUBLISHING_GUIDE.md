# Guía Completa de Publicación Multi-Plataforma — Car Audio DSP Pro v6.5.0

Esta guía explica cómo publicar tu repositorio desde **Google AI Studio** a **GitHub** y distribuir **Car Audio DSP Pro** automáticamente en todas las plataformas compatibles (Web Oficial, GitHub Releases, Google Play Store, Samsung Galaxy Store, Amazon Appstore, Huawei AppGallery y gestores como Obtainium).

---

## Paso 1: Publicar los Cambios en tu Repositorio de GitHub

El entorno del contenedor de AI Studio no almacena credenciales ni conexión `.git` directa por seguridad. Para subir todos estos archivos al repositorio `https://github.com/luismiguel38338-cmd/Car-audio-`:

1. En **Google AI Studio**, abre el menú superior/lateral de opciones del proyecto (**Settings / Export** o el icono de **GitHub**).
2. Selecciona **Push to GitHub** (o **Export as ZIP** si prefieres subirlo manualmente con Git en tu PC).
3. Elige el repositorio `luismiguel38338-cmd/Car-audio-` y la rama `main` (o crea un Pull Request y fusiónalo a `main`).

En cuanto hagas el `push` a `main`, **GitHub Actions** se ejecutará automáticamente:
- Correrá las pruebas unitarias, de motor DSP y renderizado Compose.
- Compilará el APK instalable `CarAudio-DSP-Pro-v6.5.0.apk`.
- Desplegará la página oficial y Política de Privacidad en **GitHub Pages**.

---

## Paso 2: Activar GitHub Pages (Web Oficial + Política de Privacidad)

La URL pública de tu Política de Privacidad es obligatoria para publicar en Google Play Store y otras tiendas:

1. Entra a tu repositorio en GitHub → **Settings** → **Pages**.
2. En **Build and deployment → Source**, selecciona **GitHub Actions**.
3. El workflow `.github/workflows/deploy-pages.yml` publicará automáticamente `/docs/` y las capturas reales en:
   - **Web Oficial:** `https://luismiguel38338-cmd.github.io/Car-audio-/`
   - **Política de Privacidad (para tiendas):** `https://luismiguel38338-cmd.github.io/Car-audio-/privacidad.html`

---

## Paso 3: Configurar Secrets en GitHub (Firma Oficial .AAB / .APK y API Online)

Para que GitHub Actions compile automáticamente el paquete firmado `.aab` (Google Play) y `.apk` Release (otras tiendas), configura estos Secrets en **Settings → Secrets and variables → Actions → New repository secret**:

### A. Generar tu Keystore oficial (solo la primera vez, en tu PC)
```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```
> **IMPORTANTE:** Guarda `my-upload-key.jks` y tus contraseñas en un lugar seguro (nunca subas el archivo `.jks` al repositorio).

Convierte el keystore a Base64 para guardarlo como Secret en GitHub:
- **Linux / macOS:** `base64 -w 0 my-upload-key.jks`
- **Windows PowerShell:** `[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks"))`

### B. Lista de Secrets en GitHub Actions
| Secret | Descripción | Obligatorio para |
| :--- | :--- | :--- |
| `KEYSTORE_BASE64` | Archivo `my-upload-key.jks` codificado en Base64 (`alias = upload`) | `.aab` Google Play y `.apk` Release |
| `STORE_PASSWORD` | Contraseña del keystore | `.aab` Google Play y `.apk` Release |
| `KEY_PASSWORD` | Contraseña de la clave `upload` | `.aab` Google Play y `.apk` Release |
| `JAMENDO_CLIENT_ID` | Tu Client ID gratuito de [devportal.jamendo.com](https://devportal.jamendo.com/) | Catálogo Música Online Jamendo |

*(Nota: Para compilar desde AI Studio, agrega también `JAMENDO_CLIENT_ID` en el panel **Secrets** de AI Studio).*

---

## Paso 4: Lanzar una Release Automática Multi-Plataforma

Cuando quieras publicar una versión oficial en **GitHub Releases** con todos los instaladores adjuntos:

**Opción A (Desde la web de GitHub, sin consola):**
1. Ve a la pestaña **Actions** → **Build, Test & Multi-Platform Release**.
2. Pulsa **Run workflow**.
3. Marca la casilla **Crear GitHub Release al finalizar** y escribe la etiqueta (por ejemplo `v6.5.0`).
4. Al terminar, en **Releases** aparecerán listos para descargar:
   - `CarAudio-DSP-Pro-v6.5.0-googleplay.aab` *(Google Play Store)*
   - `CarAudio-DSP-Pro-v6.5.0-release-signed.apk` *(Samsung Galaxy Store, Amazon Appstore, Huawei AppGallery, Directo)*
   - `CarAudio-DSP-Pro-v6.5.0.apk` *(APK de prueba rápida compatible con el botón de `docs/index.html`)*
   - `SHA256SUMS.txt` *(Sumas de verificación)*

**Opción B (Con etiqueta Git desde consola):**
```bash
git tag -a v6.5.0 -m "Car Audio DSP Pro v6.5.0"
git push origin v6.5.0
```

---

## Paso 5: Checklist por Plataforma de Publicación

### 1. Google Play Store / Google Play Console
- **Archivo a subir:** `CarAudio-DSP-Pro-v6.5.0-googleplay.aab`
- **Package ID:** `com.aistudio.caraudio.vxpqtz` · **Target SDK:** `36` · **Min SDK:** `24`
- **Ficha de Play Store lista en:** `fastlane/metadata/android/es-ES/` y `en-US/`
  - Título: `Car Audio DSP Pro` (17 caracteres, sin emojis ni palabras promocionales)
  - Descripción corta y completa + notas de versión `65000.txt`
  - Capturas de pantalla en `screenshots/` (copiadas también en `fastlane/metadata/android/es-ES/images/phoneScreenshots/`)
- **URL de Política de Privacidad:** `https://luismiguel38338-cmd.github.io/Car-audio-/privacidad.html`
- **Seguridad de Datos (Data Safety):**
  - La app no exige cuenta obligatoria y no incluye SDKs de publicidad activos (`AD_ID` no utilizado).
  - La biblioteca local y ajustes DSP se guardan localmente en el dispositivo.
  - Si conectas tu propio Firebase/Google Sign-In opcional (`google-services.json`), declara autenticación por cuenta de Google únicamente si habilitas sincronización en la nube.
- **Declaración de Permisos en Play Console:**
  - `READ_MEDIA_AUDIO`: Escaneo y reproducción de la música guardada por el usuario.
  - `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Reproducción de música en segundo plano y pantalla bloqueada.
  - `RECORD_AUDIO` *(opcional)*: Analizador de espectro RTA en tiempo real cuando el usuario lo activa manualmente.
  - `SYSTEM_ALERT_WINDOW` *(opcional)*: Ventana flotante rápida para ajustar el ecualizador sobre otras apps. *(Nota para Play Creator Program: si decides publicar en un programa con restricciones estrictas de permisos sensibles, revisa si deseas mantener `SYSTEM_ALERT_WINDOW` opcional o desactivarlo en esa variante).*

### 2. GitHub Releases & Obtainium (Actualizaciones Directas Android)
- Los usuarios pueden instalar `CarAudio-DSP-Pro-v6.5.0.apk` o `CarAudio-DSP-Pro-v6.5.0-release-signed.apk` desde:
  `https://github.com/luismiguel38338-cmd/Car-audio-/releases/latest`
- En **Obtainium**, basta con pegar la URL del repositorio `https://github.com/luismiguel38338-cmd/Car-audio-` y seleccionará automáticamente el `.apk` de cada nueva Release.

### 3. Samsung Galaxy Store / Amazon Appstore / Huawei AppGallery
- **Archivo a subir:** `CarAudio-DSP-Pro-v6.5.0-release-signed.apk`
- Usa los textos e imágenes ya preparados en `fastlane/metadata/android/es-ES/` y `en-US/`.
- Mantén el **mismo `my-upload-key.jks`** para firmar actualizaciones compatibles entre tiendas directas (en Google Play se usa Play App Signing con el `.aab`).

### 4. Web Oficial (GitHub Pages)
- Publicada desde `/docs/` con el enlace directo a `releases/latest/download/CarAudio-DSP-Pro-v6.5.0.apk`, simulador interactivo de respuesta en frecuencia DSP, galería de capturas reales y Política de Privacidad completa.
