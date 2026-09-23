# 🚀 Car Audio DSP Pro - Release v3.2.0: High-Power DSP & Professional Controls Update

**Versión:** `v3.2.0-release`  
**Nombre del Lanzamiento:** *Ultimate High-Headroom DSP & Precision Controls*  
**Plataforma:** Android 8.0+ (API 26+)  
**Tecnologías:** Kotlin, Jetpack Compose, Material 3, Android AudioFx, Coroutines & Flow  

---

## 📌 Resumen de la Actualización

Esta versión representa una evolución integral en la potencia de procesamiento de audio y la ergonomía de control en cabina o en competencia de Car Audio / Open Show / SPL. Se han rediseñado todos los controladores para ofrecer **mayor margen dinámico (headroom)**, **rangos ampliados de ganancia**, **botones de micro-ajuste táctil rápido** y una **limpieza profesional del código fuente** siguiendo los estándares de desarrollo de Android modernos.

---

## ⚡ Nuevas Funcionalidades y Mejoras de Potencia

### 1. 🎛️ Controles DSP Mucho Más Potentes (Expanded Gain & Headroom)
* **Master Gain Extendido:** Rango ampliado de `-40.0 dB` a `+18.0 dB` (anteriormente limitado a +6 dB), permitiendo un empuje masivo para etapas de potencia y amplificadores SPL.
* **Preamplificador DSP:** Margen expandido de `-18.0 dB` a `+18.0 dB`.
* **Tonos DSP de 3 Vías (Bass, Mid, Treble):** Rango extendido a `-18.0 dB .. +18.0 dB`.
* **Ecualizador Gráfico de 31 y 15 Bandas:** Faders expandidos a `-15.0 dB .. +15.0 dB` con código de color dinámico de advertencia sobre +6 dB.
* **Ecualizador Paramétrico de 5 Bandas:** Ganancia por banda de `-18.0 dB .. +18.0 dB` con control de factor Q de 0.3 a 10.0.
* **Bass Boost Extremo (SPL Punch):** Capacidad de empuje incrementada de 18 dB a **24.0 dB**, con selector de frecuencias centrales desde 30 Hz hasta 80 Hz.
* **Sub Bass de Ultra-Bajas:** Rango aumentado a **18.0 dB** cubriendo frecuencias críticas de 25 Hz a 60 Hz.
* **Loudness Contour:** Rango ampliado a 15.0 dB para máxima presencia acústica a bajo/medio volumen.
* **Amplitud Estéreo (Spatializer):** Rango de apertura de 0% (Mono Sum) hasta 250% (Escenario Abierto Extremo).
* **Compresor y Limitador Anti-Clip:** Umbral ampliado hasta `-48 dBFS` con relaciones de compresión de hasta `20:1` y techo limitador de `-24 dBFS` para protección rigurosa de tweeters y subwoofers.

### 2. 🎯 Botones de Micro-Paso Táctil en Todos los Controles
* Se agregaron botones de incremento y reseteo inmediato `[-]`, `[0]`, `[+]` (`-1 dB`, `0 dB`, `+1 dB` o micro-pasos de `0.5 dB`):
  * En cada slider del Master DSP.
  * En cada una de las 31 bandas y 15 bandas del Ecualizador.
  * En la ganancia del Ecualizador Paramétrico.
  * En los faders de efectos (Bass Boost, Sub Bass, Loudness, Amplitud, Crossovers y Dinámica).
* Ahora es posible clavar la calibración exacta sin tener que deslizar milimétricamente con el dedo.

### 3. 💥 Presets Rápidos de Pegada DSP (Quick Punch Presets)
Acceso con un solo toque desde la pantalla DSP Master:
* **FLAT (0 dB):** Respuesta plana y neutra de referencia.
* **BASS +6dB:** Graves cálidos y con pegada para música urbana / hip-hop.
* **VOCES +4dB:** Realce de rango medio para claridad en podcasts y voces de Open Show.
* **SPL PUNCH:** Configuración agresiva (+8dB Graves, +3dB Agudos, +4dB Preamp) para volumen alto.
* **EXTREMO:** Máxima ganancia (+12dB Graves, +6dB Agudos, +6dB Preamp) para exhibición y SPL.

### 4. 🧹 Limpieza y Optimización Profesional del Código
* **Eliminación de código muerto:** Se retiraron vistas y componentes obsoletos que no se utilizaban, reduciendo el peso y la complejidad del proyecto.
* **Telemetría Verídica:** Se eliminó la simulación ficticia de voltaje (ahora reporta `N/A` de forma honesta si no hay sensor CAN/OBD conectado por hardware), preservando la detección en tiempo real de **clipping** y **RTA** acústico con el micrófono del dispositivo.
* **Calibración del Detector de Saturación:** Ajustado para el nuevo headroom dinámico, evitando alertas falsas de distorsión ante ganancias altas legítimas.

---

## 📦 Instrucciones para Publicar este Release en GitHub

Si deseas publicar esta versión en tu repositorio de GitHub, puedes seguir estos sencillos pasos desde tu terminal o la interfaz web:

### Opción A: Desde la consola Git
```bash
# 1. Asegúrate de añadir todos los cambios
git add .

# 2. Haz commit con el mensaje de lanzamiento
git commit -m "chore(release): v3.2.0 - High-Power DSP & Precision Controls"

# 3. Crea una etiqueta (tag) anotada
git tag -a v3.2.0 -m "Car Audio DSP Pro v3.2.0 - High-Power Controls & UI Upgrade"

# 4. Sube los cambios y las etiquetas a tu repositorio remoto
git push origin main
git push origin v3.2.0
```

### Opción B: Crear el Release en GitHub Web
1. Entra a tu repositorio en **GitHub**.
2. En la barra lateral derecha, haz clic en **Releases** -> **Draft a new release**.
3. En **Choose a tag**, escribe `v3.2.0` y selecciona **Create new tag: v3.2.0 on main**.
4. En **Release title**, escribe: `Car Audio DSP Pro v3.2.0 - High-Power DSP & Precision Controls`.
5. En la descripción, copia y pega el contenido de este archivo (`RELEASE_NOTES.md`).
6. Si compilaste el APK con Gradle (`gradle assembleRelease` o `gradle assembleDebug`), arrastra el archivo `.apk` a la sección **Attach binaries by dropping them here**.
7. Haz clic en **Publish release**.

---

## 🧪 Pruebas y Verificación
- Pruebas unitarias locales (Robolectric) ejecutadas con éxito.
- Pipeline de AudioFx verificado con manejo de límites dinámicos para evitar excepciones de `IllegalArgumentException` o desbordamiento en enteros de miliBelios (`mB`).
