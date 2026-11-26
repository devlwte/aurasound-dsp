# Guía de Publicación en Google Play Console: AuraSound DSP

Esta guía contiene todos los textos exactos, respuestas a formularios de políticas y pasos para publicar **AuraSound DSP** en tu cuenta de Google Play Console sin riesgo de rechazo.

---

## 1. Archivos Listos para Subir

Todos los recursos preparados se encuentran en:
`C:\Users\PC\Desktop\IAJobs\aurasound-dsp\`

| Elemento | Archivo | Especificación Play Console |
| :--- | :--- | :--- |
| **App Bundle (.aab)** | `app\build\outputs\bundle\release\app-release.aab` | Archivo binario firmado oficial (9.9 MB). |
| **Ícono de la App** | `play_store_assets\app_icon_512.png` | 512 x 512 px PNG (32 bits). |
| **Gráfico de Funciones** | `play_store_assets\feature_graphic_1024x500.png` | 1024 x 500 px PNG. |
| **Captura 1 (Principal)** | `play_store_assets\screenshot_1_main_spectrum.png` | Captura real de pantalla (720x1610). |
| **Captura 2 (Sliders)** | `play_store_assets\screenshot_2_anti_distortion_sliders.png` | Captura de controles DRC y ecualización. |
| **Política de Privacidad** | `play_store_assets\privacy_policy.html` | Archivo HTML listo para alojar. |

---

## 2. Ficha de Play Store (Detalles de la Aplicación)

### Nombre de la aplicación (máx. 30 caracteres):
```text
AuraSound DSP: Pure Volume
```

### Descripción breve (máx. 80 caracteres):
```text
Aumentador de volumen dinámico y ecualizador con limitador anti-distorsión DRC.
```

### Descripción completa (hasta 4000 caracteres):
```text
Lleva el audio de tu dispositivo a un nivel profesional sin sacrificar la fidelidad acústica ni dañar tus altavoces.

AuraSound DSP es un procesador dinámico de señal digital (DSP) diseñado para optimizar el volumen percibido (RMS), realzar la claridad de voces y expandir la respuesta en graves mediante algoritmos de compresión multibanda y limitación de picos de grado de estudio.

TECNOLOGÍA ANTI-DISTORSIÓN DRC (DYNAMIC RANGE COMPRESSION)
A diferencia de los amplificadores de volumen convencionales que recortan la señal digital produciendo saturación, chasquidos y chicharreo, AuraSound DSP incorpora un limitador de picos brickwall calibrado a -1.5 dBFS. El algoritmo comprime suavemente los transitorios milisegundos antes de que saturen el conversor analógico-digital (DAC), garantizando potencia limpia y sin distorsión.

DETECCIÓN INTELIGENTE DE SALIDA DE AUDIO
AuraSound detecta automáticamente la ruta de reproducción activa para adaptar el perfil acústico en tiempo real:
• Parlantes y Auriculares Bluetooth (A2DP): Desbloquea el headroom dinámico y la máxima extensión de frecuencias.
• Auriculares con Cable y DAC USB-C: Habilita el virtualizador 3D estéreo y curvas de escucha de alta fidelidad.
• Altavoz Integrado del Teléfono: Aplica un filtro de protección acústica que atenúa frecuencias subsónicas (<80 Hz) para evitar la vibración mecánica excesiva del transductor interno.

ANALIZADOR DE ESPECTRO FFT EN TIEMPO REAL
Visualizador de audio de 10 bandas de frecuencia (32 Hz a 16 kHz) a 48 kHz con dinámicas de ataque instantáneo, decaimiento exponencial y agujas Peak-Hold suspendidas en el pico armónico.

PUREVOICE™ - INTELIGIBILIDAD VOCAL
¿Te cuesta escuchar diálogos en podcasts, vídeos de YouTube o películas? PureVoice™ aplica una ganancia paramétrica en la banda de 1.5 kHz a 3.5 kHz, donde reside la claridad de la voz humana, permitiendo entender cada palabra sin necesidad de forzar el volumen general.

PRESETS DE ESTUDIO
• Studio Reference: Respuesta plana y neutral para melómanos y audiófilos.
• Bass Impact: Graves contundentes y redondeados sin ahogar los tonos medios.
• Pure Voice: Máxima inteligibilidad de voces para podcasts, noticias y audiolibros.
• Party / Club: Ganancia RMS elevada con compresión densa.
• Night Protection: Atenuación de picos para evitar sobresaltos en sesiones nocturnas.

FUNCIONAMIENTO EN SEGUNDO PLANO
AuraSound DSP opera a través de un servicio en primer plano optimizado y ligero, compatible con tus aplicaciones de streaming favoritas (Spotify, YouTube, Netflix, reproductores locales y videojuegos).

COMPROMISO DE PRIVACIDAD
AuraSound DSP procesa todo el flujo de audio de forma 100% local en la memoria de tu dispositivo. No grabamos, no almacenamos ni transmitimos datos de audio ni información personal a servidores externos.

*Aviso de salud auditiva: Escuchar a niveles de volumen excesivamente altos durante períodos prolongados puede causar fatiga auditiva. Recomendamos utilizar la ganancia con moderación.*
```

---

## 3. Política de Privacidad (Requisito Obligatorio)

Google Play exige una URL pública debido a los permisos `RECORD_AUDIO` y `BLUETOOTH_CONNECT`.

**Opciones gratuitas para obtener la URL en 2 minutos:**
1. **GitHub Pages (Recomendado):**
   * Sube `privacy_policy.html` a un repositorio público en GitHub (llamado por ejemplo `aurasound-privacy`).
   * En *Settings -> Pages*, activa GitHub Pages.
   * Obtendrás una URL como: `https://tu-usuario.github.io/aurasound-privacy/privacy_policy.html`.
2. **Google Sites / Notion:**
   * Crea una página pública y pega el texto de `privacy_policy.html`.

---

## 4. Respuestas a los Cuestionarios de Google Play Console

### A. Seguridad de los datos (Data Safety)
* **¿Tu app recopila o comparte alguno de los tipos de datos de usuario requeridos?** -> Marca **NO**.
* *(Explicación:* Aunque la app solicita `RECORD_AUDIO`, este se usa únicamente para alimentar la API `Visualizer` en la RAM local para dibujar las barras del espectro; no se almacena ni se envía ningún dato a internet).

### B. Declaración de Servicio en Primer Plano (Foreground Service)
Google Play solicita declarar el tipo de servicio en primer plano:
* **Tipo seleccionado:** `MEDIA_PLAYBACK` (Reproducción y procesamiento multimedia).
* **Justificación para los revisores de Google (Copiar y pegar):**
```text
AuraSound DSP is an audio enhancement and digital signal processing utility. The Foreground Service (mediaPlayback) is essential to maintain the real-time audio dynamics processing, peak limiter, and equalizer active while the user listens to third-party media players (such as Spotify, YouTube, or podcast apps) with the screen turned off or while multitasking in other applications. The notification provides users with immediate playback control to toggle the DSP engine, switch presets, and adjust volume without needing to reopen the application.
```

### C. Clasificación de Contenido (IARC)
* Categoría de la app: **Utilidad / Herramientas**.
* Violencia / Sangre: **No**.
* Contenido sexual: **No**.
* Lenguaje soez: **No**.
* Sustancias controladas: **No**.
* Funciones de redes sociales o compartir ubicación: **No**.
* **Resultado:** Clasificación **PEGI 3 / Everyone** (Apto para todo público).

### D. Anuncios (Ads)
* ¿Tu app contiene anuncios? -> **No**.

### E. Público Objetivo
* Rango de edad: **13 años o más** (13-17 y 18+).
  *(No selecciones menores de 13 años para evitar someterte a las políticas restrictivas de Familias de Google Play).*

---

## 5. Pasos para Subir el Release en Play Console

1. Entra a [Google Play Console](https://play.google.com/console).
2. Haz clic en **Crear aplicación**:
   * Nombre: `AuraSound DSP: Pure Volume`
   * Idioma predeterminado: Español (España o Latinoamérica)
   * Tipo: Aplicación
   * Gratis o de pago: Gratis
3. Ve a **Presencia en Google Play Store -> Ficha de Play Store principal**:
   * Pega los textos de la sección 2.
   * Sube el ícono `app_icon_512.png`.
   * Sube el banner `feature_graphic_1024x500.png`.
   * Sube las capturas de teléfono `screenshot_1_main_spectrum.png` y `screenshot_2_anti_distortion_sliders.png`.
4. Ve a **Contenido de la aplicación -> Formularios de políticas**:
   * Completa Política de Privacidad, Seguridad de Datos, Anuncios, Clasificación y Servicio en Primer Plano con las respuestas de la sección 4.
1. Ve a **Producción** (o **Prueba cerrada** si tu cuenta es personal):
   * Crea una nueva versión.
   * Arrastra y suelta el nuevo archivo: `aurasound-dsp\app\build\outputs\bundle\release\app-release.aab`.
   * Nombre de la versión: `1.0.5 (6)`.
   * Notas de la versión: `Afinación del motor acústico DSP, respuesta de espectro FFT ultra fluida y limitador DRC anti-distorsión calibrado.`
2. Guarda y haz clic en **Enviar a revisión**.
