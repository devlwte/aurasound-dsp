# AuraSound DSP

Subidor de volumen para Android que no distorsiona. Sube la ganancia de todo el audio del teléfono y pone un limitador justo después, así la música suena más fuerte sin que los picos revienten.

Funciona con cualquier app (Spotify, YouTube, juegos) porque se engancha a la salida global de audio. Necesita Android 9 o más reciente.

## Qué hace

- Perilla de volumen: de 0 a +17,5 dB en el preset Studio y hasta +30 dB en Party.
- Limitador anti-distorsión que se puede apagar si prefieres el sonido sin tocar.
- Refuerzo de graves y efecto espacial, que se ajustan solos según salga el audio por el altavoz, por cable o por Bluetooth.
- Visualizador de espectro en tiempo real.
- Notificación con botones para pausar, cambiar de preset o subir el volumen sin abrir la app.

## Compilar

Hace falta JDK 17 o el que trae Android Studio.

```
./gradlew assembleRelease
```

El APK queda en `app/build/outputs/apk/release/`. Para firmarlo crea un `keystore.properties` en la raíz con tu propia llave; ese archivo y las llaves no están en el repo.

## Limitaciones

- Algunos fabricantes no dejan aplicar efectos a la salida global. En esos teléfonos el volumen extra puede no notarse.
- El control de claridad de voz todavía no hace nada.
