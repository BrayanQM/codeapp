# Guía de capturas para la presentación

> Base de todo: `/home/eynor/Documents/semestr7/Dispositivos Moviles/hito3/Firebase`
> Las imágenes van en: `/home/eynor/Documents/semestr7/Dispositivos Moviles/hito3/document/imagenes/`

## Cómo tomar cada captura en Android Studio

1. Abrí el archivo con **Ctrl+P** (búsqueda rápida), escribí el nombre.
2. Poné el cursor en la **línea de inicio** y usá **Shift+Alt+↓** para seleccionar
   hasta la línea de fin.
3. Copiá con **Ctrl+C**.
4. Pegá en un editor o en Paint/WPS y capturá la región **con el número de línea
   visible**. El número de línea en la captura es lo que prueba que es código real.
5. Guardá con el nombre exacto de la tabla, en la carpeta `imagenes/`.

Tip: dejá el tema claro (o oscuro) **consistente** en todas las capturas.

---

## Capturas de la consola de Firebase (navegador)

Estas no son de Android Studio. Abrí Chrome en incognito, entrá a
`console.firebase.google.com/project/classfirebase-d856a/database`.

| Archivo a guardar | Qué mostrar en pantalla |
|---|---|
| `consola_firebase_url.png` | La pestaña **Datos** de Realtime Database, con la URL visible arriba y los 3 productos listados |
| `consola_security_rules.png` | La pestaña **Reglas**, con el JSON de las reglas |

---

## Capturas de código (Android Studio)

| # | Archivo a guardar | Diapositiva | Ruta completa del archivo | Líneas |
|---|---|---|---|---|
| 1 | `codigo_build_gradle.png` | 4 | `app/build.gradle.kts` | **8 a 13** y **30 a 33** |
| 2 | `codigo_rutas.png` | 5 | `app/src/main/java/com/primerexample/firebaseapi/data/FirebaseApi.kt` | **49 a 54** |
| 3 | `codigo_http.png` | 6 | `app/src/main/java/com/primerexample/firebaseapi/data/FirebaseApi.kt` | **56 a 71** |
| 4 | `codigo_corrutina.png` | 7 | `app/src/main/java/com/primerexample/firebaseapi/data/ProductoRepository.kt` | **95 a 98** |
| 5 | `codigo_corrutina2.png` | 7 | `app/src/main/java/com/primerexample/firebaseapi/ui/ProductoController.kt` | **69 a 76** |
| 6 | `codigo_errores.png` | 8 | `app/src/main/java/com/primerexample/firebaseapi/data/FirebaseApi.kt` | **88 a 96** |
| 7 | `codigo_modelo.png` | 5 (opcional) | `app/src/main/java/com/primerexample/firebaseapi/model/Producto.kt` | **6 a 15** |
| 8 | `codigo_localproperties.png` | 4 (opcional) | `local.properties` | **21 a 24** |

### Detalle de la captura 1 (la más importante de la 4)

Seleccioná **dos bloques separados** en `app/build.gradle.kts` o hacé dos capturas
y las pegás una debajo de otra. Tiene que verse:

```kotlin
val localProperties = Properties().apply {          // línea 8
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
...
buildConfigField("String", "FIREBASE_DB_URL", ...)  // línea 32
```

### Detalle de la captura 3 (la que más impresiona)

`FirebaseApi.kt` líneas 56 a 71. Tiene que verse completo:

- `private fun peticion(metodo: String, url: String, cuerpo: String?): String {`
- `val conexion = (URL(url).openConnection() as HttpURLConnection).apply {`
- `requestMethod = metodo`
- `connectTimeout = TIEMPO_LIMITE_MS`
- `setRequestProperty("Content-Type", "application/json; charset=utf-8")`

Esa captura demuestra de golpe las 3 cosas de la diapositiva 6: la URL, el método
HTTP y el JSON.

---

## Capturas de la app (ya las tenés)

| Archivo a guardar | Diapositiva | Origen |
|---|---|---|
| `app_lista.png` | 10 | **Ya está lista** en `imagenes/` |
| `app_formulario.png` | 10 | Copiá de `../capturas/fig2_formulario.png` |
| `app_confirmacion.png` | 10 | Copiá de `../capturas/fig3_confirmacion.png` |

Para la **diapositiva 10** (App y Firebase en paralelo) armá una imagen con las dos:
la app a la izquierda y la consola de Firebase a la derecha, y guardala como
`app_y_firebase.png`.

---

## El diagrama de la diapositiva 2 — YA LISTO

`imagenes/diagrama_flujo.png` (1200x620) ya está generado con los colores de la
presentación: tres recuadros (App Android → HTTP + JSON → Firebase) con las
flechas de ida y vuelta, y una nota al pie que explica qué hace cada capa.

No tenés que dibujarlo. Si querés cambiar algo, se edita el SVG y se vuelve a
exportar con `rsvg-convert`.

---

## Checklist antes de presentar

- [ ] Las 8 imágenes están en la carpeta `imagenes/`
- [ ] El HTML abre sin caja vacía en ningún `<img>`
- [ ] Los números de línea se ven en todas las capturas de código
- [ ] La URL que aparece en las capturas es `classfirebase-d856a`, no un ejemplo
- [ ] Probá la presentación en el mismo proyector (fuentes y colores)
