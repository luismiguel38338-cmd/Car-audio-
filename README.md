# CarAudio DSP Pro & Analizador de Planta

Aplicación nativa para Android desarrollada con **Kotlin** y **Jetpack Compose (Material Design 3)**, diseñada como procesador digital de audio (DSP), analizador de espectro en tiempo real (RTA), monitor de telemetría de amplificador/planta y reproductor musical de alta fidelidad para car audio y sonido open show.

---

## 🚀 Características Principales

1. **Procesador DSP & Crossover Digital**:
   - Filtros de Cruce: HPF (Paso Alto para medios/voces), LPF (Paso Bajo para subwoofers) y Filtro Subsónico (10Hz–50Hz para cajones porteados).
   - Pendientes configurables (12 dB/oct, 24 dB/oct, 48 dB/oct).
   - Refuerzo de Graves (Bass Boost 0 a +18 dB) con selector de frecuencia central (35Hz, 45Hz, 55Hz).
   - Corrección de Tiempo acústico (Time Alignment) en milisegundos y conversión a centímetros.
   - Inversor de fase de subwoofer (0° / 180°).
   - Ecualizador Paramétrico de 8 bandas con presets profesionales (*Open Show Pro*, *SPL Bass Monster*, *SQL Audiophile*, *Reggaeton*, *Rock Punch*).

2. **Analizador Acústico RTA en Tiempo Real & Medidores SPL**:
   - Analizador de espectro RTA de 30 bandas (20Hz a 20kHz) con retención de picos (*peak hold*).
   - Medidor sonómetro SPL en decibelios (dB SPL) en vivo y registro de dB SPL máximo.
   - Vúmetros analógicos/digitales estéreo duales (Canal Izquierdo L / Canal Derecho R).
   - Osciloscopio con renderizado continuo de forma de onda.
   - Modo Micrófono en vivo (`RECORD_AUDIO`) para calibrar el sonido en el habitáculo del vehículo.

3. **Monitor de Planta Car Audio & Diagnóstico**:
   - Voltímetro digital de precisión con indicador de alternador (14.4V), batería en reposo y alerta de bajo voltaje (<11.5V).
   - Monitoreo térmico en °C y protección contra sobrecalentamiento.
   - Potenciómetro de salida calculado en Watts RMS (@ 1 Ohm).
   - Detección de distorsión y recorte armónico (*Clipping*) con contador en tiempo real y LED avisador.

4. **Sistema de Notificaciones del Sistema**:
   - Soporte para Android 13+ con solicitud y explicación del permiso `POST_NOTIFICATIONS`.
   - Alertas críticas inmediatas en la barra de estado si se detecta Clipping o caída de voltaje peligroso para las bobinas y el alternador.

5. **12 Temas de Color Exclusivos**:
   - Neon Cyber, Crimson Red, Electric Blue, Carbon Gold, Ultra Violet, Blaze Orange, Acid Lime, Ice Cyan, Vapor Pink, Titanium SQL, Toxic Yellow y Magma Burst.

6. **Reproductor Musical & Tonos de Calibración**:
   - Pistas de prueba integradas (barridos 30Hz-80Hz, tonos SPL 45Hz, test vocal Open Show, SQL balance, Ruido Rosa para RTA).
   - Botón para cargar cualquier archivo de audio local (`.mp3`, `.wav`, `.flac`, `.m4a`) desde la memoria del teléfono o memoria USB mediante el selector del sistema.

---

## 🛠️ Requisitos de Compilación

- **Android Studio** Ladybug o superior / Android Studio Koala / Hedgehog
- **JDK**: Java 17 o superior
- **Gradle**: 8.x / 9.x (incluye script wrapper `gradlew`)
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK / Compile SDK**: 36

---

## 📦 Cómo Generar el APK en tu Computadora o Servidor

### 1. Clonar el repositorio:
```bash
git clone <URL_DEL_REPOSITORIO_GITHUB>
cd <CARPETA_DEL_PROYECTO>
```

### 2. Otorgar permisos de ejecución al Wrapper de Gradle:
- En Linux / macOS:
  ```bash
  chmod +x gradlew
  ```

### 3. Compilar el APK de Depuración (Debug APK):
- En Linux / macOS:
  ```bash
  ./gradlew assembleDebug
  ```
- En Windows:
  ```cmd
  gradlew.bat assembleDebug
  ```

### 4. Ubicación del APK Generado:
Una vez finalizada la compilación, el archivo APK listo para instalar en tu teléfono o estéreo Android se encontrará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Instalación en el Dispositivo Android
1. Transfiere el archivo `app-debug.apk` a tu teléfono, tableta o autoestéreo Android (mediante USB, Google Drive, WhatsApp o Telegram).
2. Habilita "Instalar aplicaciones de fuentes desconocidas" si tu sistema lo solicita.
3. Abre el archivo y presiona **Instalar**.
