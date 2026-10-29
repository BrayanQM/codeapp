# API de Productos — Firebase Realtime Database

APK minimalista en **Jetpack Compose** que registra, consulta, modifica y elimina
productos. Cada cambio se guarda **en dos lugares a la vez**:

1. **Firebase Realtime Database** (nube) — vía su API REST usando la clave secreta.
2. **Un archivo JSON local** en `filesDir/database/productos.json` de la app.

Cumple los 5 endpoints de la consigna:

| Método | Ruta Firebase | Acción en la app |
|---|---|---|
| GET | `/productos.json` | Ver la lista (botón *Actualizar* y al abrir) |
| GET | `/productos/{id}.json` | Abrir un producto (tap en *Editar*) |
| POST | `/productos.json` | Botón **+** → *Guardar* |
| PATCH | `/productos/{id}.json` | *Editar* → *Guardar* |
| DELETE | `/productos/{id}.json` | *Eliminar* |

---

## 1. Poner la URL de tu base de datos

El proyecto ya trae la **clave secreta** en `local.properties`. Solo falta la URL.
Copiá la que aparece en:

> Firebase console → **Realtime Database** → pestaña *Datos* → ícono de ajustes ⚙️
> → **URL de base de datos en la nube**

y editá `local.properties`:

```properties
firebase.db.url=https://TU-PROYECTO-default-rtdb.firebaseio.com
firebase.db.secret=8e5IqzU1fIFyhCEop524yGheU5wZRyy7MYbTzPqQ
```

> `local.properties` está en `.gitignore`, así que la clave no se sube al repositorio.
> Si la app queda en rojo con *"Firebase sin configuring"*, es que la URL no está puesta.

**Reglas de la base de datos:** deben permitir lectura y escritura. En la consola,
*Realtime Database → Reglas*, probar:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

---

## 2. Compilar e instalar

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O desde Android Studio: botón **Run**.

---

## 3. Cómo funciona

### El JSON: la misma forma en los dos lados

Firebase Realtime Database es, literalmente, **un árbol JSON en la nube**. Cada
producto es una clave y su valor es un objeto:

```json
{
  "-Nseed001": {
    "nombre": "Teclado mecanico",
    "descripcion": "Teclado RGB con switches",
    "precio": 89.9,
    "stock": 12
  }
}
```

El archivo local usa **exactamente el mismo formato**, porque la app manda a
Firebase el mismo cuerpo JSON que escribe en disco. Eso hace que sincronizar sea
trivial y que `ProductoJson` sirva para los dos.

### Las dos rutas del archivo

- `app/src/main/assets/database/productos.json` → **dentro del APK**. Es la semilla.
  `assets/` es de solo lectura, así que no se puede guardar ahí.
- `filesDir/database/productos.json` → la **carpeta `database` real y escribible**
  del almacenamiento interno de la app. En el primer arranque la app copia la
  semilla acá (`LocalJsonStore.preparar()`) y de ahí en adelante es la copia
  vigente. Sin permisos y sin que el usuario la vea.

### La conexión

No se usa el SDK de Firebase: se habla **HTTP + JSON** con `HttpURLConnection`,
igual que lo haría un `curl` o Postman. La autenticación va en la query:

```
https://<proyecto>.firebaseio.com/productos.json?auth=<CLAVE_SECRETA>
```

- **POST** en la raíz: Firebase genera el id (push ID) y lo devuelve como
  `{"name": "-Nabc123..."}`. Por eso el id se asigna *después* de guardar.
- **PATCH** actualiza solo los campos enviados sin borrar el resto.

### Por qué se escribe primero en local

`ProductoRepository` escribe en el archivo JSON **antes** de llamar a Firebase.
Si no hay red, el dato no se pierde: queda en el celular y la barra de estado
avisa *"Guardado solo en database/productos.json"*. En la pantalla hay un panel
que muestra en todo momento si los datos vienen de Firebase o del archivo local,
y la ruta exacta del archivo.

---

## 4. Archivos

```
app/src/main/
├── AndroidManifest.xml                      permiso INTERNET
├── assets/database/productos.json            semilla dentro del APK
└── java/com/primerexample/firebaseapi/
    ├── MainActivity.kt                      Activity de Compose
    ├── model/Producto.kt                    modelo + toJson()
    ├── data/
    │   ├── ProductoJson.kt                  serialización, la misma para ambos lados
    │   ├── LocalJsonStore.kt                lee/escribe filesDir/database/
    │   ├── FirebaseApi.kt                   REST: GET POST PATCH DELETE
    │   └── ProductoRepository.kt           coordina archivo + Firebase
    └── ui/
        ├── ProductoApp.kt                   lista y formulario
        ├── ProductoController.kt            estado de la pantalla
        └── theme/Theme.kt                   tema Material 3
```

---

## 5. Probar la API sin la app

La misma API se puede verificar con `curl` o Postman, útil para la defensa:

```bash
BASE="https://TU-PROYECTO-default-rtdb.firebaseio.com/productos.json"
CLAVE="8e5IqzU1fIFyhCEop524yGheU5wZRyy7MYbTzPqQ"

curl "$BASE?auth=$CLAVE"                                              # listar
curl -X POST -H "Content-Type: application/json" -d \
  '{"nombre":"Mouse","descripcion":"Inalambrico","precio":34.5,"stock":25}' \
  "$BASE?auth=$CLAVE"                                                # crear
curl "$BASE/-Nabc123?auth=$CLAVE"                                    # ver uno
curl -X PATCH -H "Content-Type: application/json" -d '{"precio":29.9}' \
  "$BASE/-Nabc123?auth=$CLAVE"                                       # actualizar
curl -X DELETE "$BASE/-Nabc123?auth=$CLAVE"                          # eliminar
```

Postman: importá la colección, definí la variable `{{base}}` con la URL y
`{{clave}}` con la clave secreta, y `{{base}}/productos.json?auth={{clave}}`.

---

## 6. Tests

```bash
./gradlew :app:testDebugUnitTest
```

Cubren el formato del JSON, el round-trip (escribir → leer), que el `id` viva
solo como clave y los valores por defecto ante campos ausentes.

---

## Nota de seguridad

La clave secreta queda compilada dentro del APK, que se puede descomprimir. Para
un trabajo práctico está bien, pero en producción se usaría autenticación real
(Firebase Auth) y reglas de seguridad. **No subas `coneccion.md` al repositorio**:
agregalo a `.gitignore` y, si ya lo subiste, rota la clave en la consola.
