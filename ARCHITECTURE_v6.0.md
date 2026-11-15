# Car Audio DSP Pro v6.0 — Auditoría de Arquitectura y Rendimiento

**Repositorio de origen:** `https://github.com/luismiguel38338-cmd/Car-audio-.git`  
**Nota de entorno:** Este espacio de trabajo no contiene carpeta `.git` local; los cambios se estructuran por fases verificables y quedan listos para incorporarse a la rama de trabajo `v6.0`.

---

## 1. Inventario de Funciones Existentes (Conservadas al 100%)

1. **Reproductor Local (`MÚSICA` — `PlayerView`)**
   - Escaneo de biblioteca local (`MediaStore.Audio.Media`) con permisos `READ_MEDIA_AUDIO` (API 33+) / `READ_EXTERNAL_STORAGE` (API <= 32).
   - Selector múltiple de archivos (`OpenMultipleDocuments`) con persistencia de URIs en `SharedPreferences`.
   - Controles de reproducción (`Play/Pause`, `Siguiente`, `Anterior`, barra de progreso, volumen maestro).
   - Modificador en vivo en el mismo panel (presets rápidos, Bass Boost, Preamp y EQ de 5 bandas).

2. **Ecualizador de Precisión (`EQ` — `EqualizerProcessorView`)**
   - Curva gráfica de respuesta en tiempo real (20Hz – 20kHz).
   - Ecualizador gráfico de **31 bandas ISO** (con filtros por rango de octavas y ajuste fino +/- 0.5 dB).
   - Ecualizador gráfico de **15 bandas**.
   - Ecualizador **Paramétrico de 5 bandas** (frecuencia central, ganancia y factor Q).
   - Catálogo de presets y guardado/eliminación de presets personalizados.

3. **Procesador DSP Master (`DSP` — `DspMasterView`)**
   - Control de energía DSP Master y modo Bypass.
   - Medidores VU estéreo L/R, indicador de Clipping y espectro en tiempo real.
   - Controles de tonalidad y ganancia (`Preamp`, `Bass`, `Mid`, `Treble`, `Master Output`).
   - `Hardware Bass Boost`, `Amplitud Estéreo (Virtualizer)`, `Balance L/R`, `Compresor`, `Limitador anti-clip` y filtros de corte `HPF` / `LPF`.

4. **Ajustes y Pantalla Virtual Flotante (`AJUSTES` — `AppSettingsView`, `FloatingEqualizerService`)**
   - Burbuja flotante circular abierta roja (`SYSTEM_ALERT_WINDOW`) + consola flotante compacta para ajustar 5 bandas, Bass Boost, Potencia y presets sobre apps externas como YouTube.
   - Captura de sesiones externas (`AudioSessionReceiver`), pantalla de intro (`CarAudioSplashScreen`), selector de 12 temas (Todo Rojo por defecto), `Keep Screen On` y restablecimiento de fábrica.

---

## 2. Medición y Diagnóstico Previo a Optimización (v5.0.0)

| Área | Cuello de Botella Detectado | Impacto | Corrección Planificada v6.0 |
| :--- | :--- | :--- | :--- |
| **Dependencias / Arranque** | Room, KSP, Retrofit, Moshi, OkHttp, Firebase AI y AppCheck compilados sin uso en `app/src`. | Mayor tamaño APK, carga innecesaria de clases al inicio y más RAM base. | Comentar librerías no usadas manteniendo `coil-compose` y Compose/Lifecycle. |
| **Telemetry Polling** | Bucle `while(isActive)` cada 100ms continuo incluso en pausa o segundo plano, actualizando múltiples `StateFlow` por tick. | Consumo de CPU/batería en reposo y recomposiciones constantes. | Pausar/espaciar telemetría cuando no hay audio activo o UI en segundo plano; `collectAsStateWithLifecycle`. |
| **Compose & Canvas** | `EqCurveCanvas31` recalcula 100 puntos logarítmicos en cada redibujo; `DspMasterView` crea `Brush.verticalGradient` 31 veces por frame; `PlayerView` sin `contentType` ni `@Immutable`. | Caídas de FPS en teléfonos modestos. | Modelos `@Immutable`, `drawWithCache` / `remember` para `Path` y `Brush`, `contentType` en `LazyColumn`. |
| **Motor AudioFX** | `applyDspSettings()` instancia nuevos objetos `DynamicsProcessing.Eq/Mbc/Limiter` en cada movimiento del slider; cortes ISO aritméticos en lugar de media geométrica; sin `AudioFocus` ni pausa al desconectar auriculares. | Micro-cortes al arrastrar sliders, imprecisión en bandas altas, riesgo de clipping sin compensación de headroom. | Reutilización de etapas `DynamicsProcessing`, media geométrica ISO real $\sqrt{f_i \cdot f_{i+1}}$, curvas suaves HPF/LPF/Paramétrico, `AudioFocus` + `ACTION_AUDIO_BECOMING_NOISY`. |
| **FFT / Visualización** | `processPlaybackFft()` asigna nuevo `FloatArray` y 31 objetos `RtaBand` (`map { copy() }`) en cada callback del `Visualizer`. | Presión constante sobre el Garbage Collector (GC). | Buffers pre-asignados sin asignaciones en hot-path y actualización incremental. |
| **Reproducción en 2º Plano** | `MediaPlayer` vive solo en singleton sin servicio foreground ni `MediaSession`; `MediaMetadataRetriever` sin `finally` al extraer metadatos. | Android puede matar la música en segundo plano; posible fuga nativa si falla la lectura de archivo. | `CarAudioPlaybackService` foreground + `MediaSession` nativa + acciones en notificación + carátulas con caché LRU + `try/finally`. |
| **Compatibilidad Modesta** | Sin detección de dispositivo de salida (`Bluetooth`/`Auriculares`/`Altavoz`) ni modo ligero para dispositivos de poca RAM; texto de YouTube podía sugerir garantía universal. | Falta de transparencia técnica y mayor carga gráfica en gama baja. | `AudioDeviceCallback`, detección `ActivityManager.isLowRamDevice`, aviso técnico honesto sobre restricciones de Android en sesiones externas. |
