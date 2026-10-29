# API de Gestión de Productos — Android + Firebase Realtime Database

**Materia:** Dispositivos Móviles — Hito 3
**Proyecto:** `FirebaseApi` (paquete `com.primerexample.firebaseapi`)
**Tecnología:** Android Studio, Kotlin, Jetpack Compose, Firebase Realtime Database (REST)

---

## 1. Objetivos y fundamentos

### 1.1 Objetivo de la práctica

El objetivo es desarrollar, en Android Studio, una **aplicación móvil Android** que
funcione como **cliente de una API REST de gestión de productos**, utilizando
**Firebase Realtime Database** como backend (Opción D de la guía).

La aplicación permite administrar un catálogo de productos con las cuatro
operaciones CRUD (**Crear, Leer, Actualizar, Eliminar**) sobre datos almacenados
en la nube, y demuestra que un dispositivo móvil puede operar como cliente de una
API real usando el protocolo **HTTP** con peticiones y respuestas en formato **JSON**.

El proyecto cumple con la especificación pedida:

| Endpoint REST | Método | Función en la app |
|---|---|---|
| `/api/productos` | GET | Listar productos (pantalla principal) |
| `/api/productos/{id}` | GET | Ver un producto (tarjeta en la lista) |
| `/api/productos` | POST | Crear producto (botón `+`) |
| `/api/productos/{id}` | PUT/PATCH | Editar producto (botón "Editar") |
| `/api/productos/{id}` | DELETE | Eliminar producto (botón "Eliminar") |

### 1.2 Fundamentación teórica

**a) Android y su arquitectura**

Android separa el código en capas. En este proyecto se aplican tres:

- **UI (Interfaz de usuario):** escrita con **Jetpack Compose**, toolkit
  declarativo donde se describe *qué* se ve y Compose decide *cómo* dibujarlo.
- **Estado / Controlador:** conserva los datos en pantalla y coordina las acciones.
- **Datos (Modelo):** contiene la lógica de negocio y el acceso a la base de datos.

Esta separación evita que la interfaz se bloquee: las llamadas de red se ejecutan
en un hilo secundario y el resultado se entrega a la interfaz de forma segura.

**b) Kotlin**

Lenguaje oficial de Android. Sus características principales usadas aquí:

- **Null safety:** el tipo `String?` obliga a comprobar si un valor puede ser nulo,
  eliminando los "NullPointerException".
- **Data classes:** `data class Producto(...)` genera automáticamente los
  constructores, `equals`, `toString` y `copy`.
- **Coroutines:** `suspend` y `withContext(Dispatchers.IO)` permiten operaciones
  de red sin bloquear la interfaz.

**c) JSON como formato de intercambio**

La app y Firebase se comunican en **JSON**, un formato de texto estructurado
clave-valor. Ejemplo de un producto:

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

**d) API REST y el protocolo HTTP**

Una API REST define operaciones usando los métodos del protocolo HTTP:

| Método | Significado |
|---|---|
| `GET` | Solicitar/leer un recurso |
| `POST` | Crear un recurso nuevo |
| `PATCH` / `PUT` | Modificar parcialmente / totalmente un recurso |
| `DELETE` | Eliminar un recurso |

La app implementa el **cliente**: envía peticiones HTTP a Firebase y procesa las
respuestas JSON.

**e) Firebase Realtime Database**

Base de datos en la nube de tipo **NoSQL**, organizada como un árbol de
claves y valores. A diferencia de las bases relacionales (con tablas y filas),
aquí cada documento es un nodo con clave única.

**Punto clave de seguridad:** el acceso a la base de datos **NO depende de
ocultar la URL**. Cualquier persona que conozca la dirección puede intentar
leerla. Lo que realmente controla el acceso son las **Firebase Security Rules**
(reglas de seguridad), que se configuran en la consola:

```json
{
  "rules": {
    "productos": {
      ".read": true,
      ".write": true
    }
  }
}
```

`".read": true` permite leer, `".write": true` permite escribir. Estas reglas son
la verdadera frontera de seguridad del sistema.

---

## 2. ¿Qué hace la aplicación?

La app es un **catálogo de productos** que opera contra la nube:

1. Al abrirse, **descarga automáticamente** los productos desde Firebase.
2. Muestra cada producto en una **tarjeta** con nombre, descripción, precio en
   bolivianos y stock.
3. Permite **crear** productos con el botón flotante `+`.
4. Permite **editar** y **eliminar** desde cada tarjeta.
5. Mantiene una **copia local** en JSON para no perder datos si la red falla.

**Interfaz final:**

```
Productos
┌─────────────────────────┐
│ Teclado mecanico        │
│ Teclado RGB con switches│
│ Bs 89.90     Stock: 12  │
│          Editar  Eliminar│
└─────────────────────────┘
```

---

## 3. Arquitectura del proyecto

El código está organizado en **capas**, cada una con una responsabilidad clara:

```
app/src/main/java/com/primerexample/firebaseapi/
│
├── MainActivity.kt              Activity: punto de entrada, monta la UI
│
├── model/
│   └── Producto.kt              Modelo de datos + formato de precio
│
├── data/                        CAPA DE DATOS
│   ├── FirebaseApi.kt           Cliente HTTP: habla con Firebase
│   ├── ProductoRepository.kt    Decide: ¿Firebase o archivo local?
│   ├── LocalJsonStore.kt        Copia local en archivo
│   └── ProductoJson.kt          Conversión objeto <-> JSON
│
└── ui/                          CAPA DE INTERFAZ
    ├── ProductoApp.kt           Pantallas Compose (lista y formulario)
    ├── ProductoController.kt    Estado de la pantalla
    └── theme/Theme.kt           Colores y tipografía
```

### 3.1 El flujo de una operación (ejemplo: crear producto)

Este es el recorrido completo de un POST, que conviene explicar en la defensa:

```
1. Usuario llena el formulario y pulsa "Guardar"
              ↓
2. ProductoApp.kt → ProductoController.guardar(producto)
   (la interfaz solo delega, no sabe de Firebase)
              ↓
3. ProductoRepository.crear()      ← LA CAPA DE DECISIÓN
   Primero guarda en el archivo local (por si falla la red)
              ↓
4. FirebaseApi.crear()             ← LA CAPA DE RED
   Construye: POST https://classfirebase-d856a-default-rtdb.firebaseio.com/productos.json
              ↓
5. Internet → Firebase
              ↓
6. Firebase responde con el id generado: {"name": "-P2ecsuxzsScVD4ERCt9"}
              ↓
7. ProductoRepository reemplaza el id temporal por el id real de Firebase
              ↓
8. La interfaz se actualiza y muestra "Guardado en Firebase"
```

**Punto clave de la defensa:** la interfaz (`ProductoApp.kt`) **nunca** habla
directamente con Firebase. Siempre pasa por el controlador y el repositorio.
Eso se llama **separación de responsabilidades**, y permite que mañana la app
use otra base de datos sin tocar la interfaz.

### 3.2 El patrón "offline first"

`ProductoRepository` implementa una estrategia defensiva:

- **Primero** escribe en el archivo local.
- **Luego** intenta enviar a Firebase.
- **Si falla la red**, el producto **no se pierde**: queda en el local y la app
  avisa que solo se guardó localmente.

Así la app sigue siendo usable aunque el móvil esté sin internet o Firebase
esté caído.

---

## 4. El proceso de conexión a Firebase

Esta es la parte técnica central. El proceso completo, en orden:

### Paso 1 — Crear el proyecto en Firebase

En la consola (console.firebase.google.com) se creó el proyecto
`classfirebase-d856a` (ClassFirebase). Un proyecto es el contenedor de todos los
recursos: base de datos, autenticación, hosting, etc.

### Paso 2 — Crear la base de datos

Se fue a **Build → Realtime Database → Crear base de datos**, se eligió ubicación
y se seleccionó **"Modo de prueba"**.

> El "Modo de prueba" permite leer y escribir sin autenticación durante las
> primeras 30 días. Es lo que permite hacer la demostración sin configurar
> usuarios.

Esto generó la **URL**, que es la dirección de la base de datos:

```
https://classfirebase-d856a-default-rtdb.firebaseio.com
```

### Paso 3 — Configurar la URL en el proyecto Android

La URL **no** se escribe dentro del código. Se coloca en `local.properties`
(archivo de configuración local, que **no** se sube al repositorio):

```properties
firebase.db.url=https://classfirebase-d856a-default-rtdb.firebaseio.com
firebase.db.token=
```

**¿Por qué en ese archivo y no en el código?**

- `local.properties` está en `.gitignore`, así la configuración de cada
  computadora queda separada del código.
- Si la URL cambia (otro proyecto, otra región), se edita **una sola línea** sin
  tocar Kotlin.

### Paso 4 — Llevar la configuración al código

`app/build.gradle.kts` lee ese archivo en tiempo de compilación y genera
constantes de `BuildConfig`:

```kotlin
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

buildConfigField("String", "FIREBASE_DB_URL", "\"${config("firebase.db.url")}\"")
```

Y `ProductoRepository` la toma de ahí:

```kotlin
private val api = FirebaseApi(
    BuildConfig.FIREBASE_DB_URL,
    BuildConfig.FIREBASE_DB_TOKEN
)
```

Este mecanismo se llama **`buildConfigField`** y permite inyectar configuración
desde Gradle hacia el código.

### Paso 5 — Realizar las peticiones HTTP

`FirebaseApi.kt` es el cliente REST. Construye las URL y ejecuta las llamadas:

```kotlin
private fun rutaRaiz() = "$urlBase/$COLECCION.json"   // .../productos.json
private fun ruta(id: String) = "$urlBase/$COLECCION/$id.json"  // .../productos/-Nseed001.json
```

Y ejecuta la petición con `HttpURLConnection`:

```kotlin
val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
    requestMethod = metodo          // GET, POST, PATCH, DELETE
    connectTimeout = TIEMPO_LIMITE_MS
    readTimeout = TIEMPO_LIMITE_MS
    setRequestProperty("Accept", "application/json")
}
```

### Paso 6 — Verificar con un script

El archivo `verificar-firebase.sh` prueba los 5 endpoints **sin abrir la app**,
para confirmar que el backend responde:

```bash
./verificar-firebase.sh
```

Salida esperada:

```
1) GET  /productos.json        (listar)      OK (HTTP 200)
2) POST /productos.json        (crear)       OK (HTTP 200)
3) GET  /productos/{id}.json   (ver uno)     OK (HTTP 200)
4) PATCH /productos/{id}.json  (actualizar)  OK (HTTP 200)
5) DELETE /productos/{id}.json (eliminar)    OK (HTTP 200)
```

---

## 5. Problemas encontrados y cómo se solucionaron

Esta sección es **importante para la defensa**: demuestra diagnóstico real.

### Problema 1 — La app nunca se conectaba

**Síntoma:** la app funcionaba pero siempre mostraba los datos locales, nunca
los de Firebase.

**Diagnóstico:** se revisó la configuración y se encontró que
`local.properties` tenía:

```properties
firebase.db.url=PEGAR_AQUI_LA_URL
```

Es decir, un **marcador de posición** que nunca se reemplazó. La app detectaba
que la URL no era válida y usaba el archivo local, por eso "funcionaba" sin
conectarse nunca.

**Solución:** se Felizizó con la URL real obtained de la consola.

### Problema 2 — La "Clave Secreta" ya no existe

**Síntoma:** el proyecto traía una supuesta clave secreta
(`8e5IqzU1fIFy...`) que se usaba así: `?auth=CLAVE`.

**Diagnóstico:** Firebase **eliminó las database secrets en octubre de 2024**.
Ningún proyecto nuevo tiene una. La clave solo funcionaba porque la base de
datos quedó en "Modo de prueba", que no exige autenticación.

**Solución:** se eliminó la autenticación por clave secreta del código. El
parámetro `auth` ahora es **opcional**, y solo se usa si las reglas exigen
autenticación.

**Lección:** una clave secreta incrustada en el código no es segura de todos
modos, porque cualquiera puede extraerla del APK con un descompresor. Firebase
usa las **reglas de seguridad**, no las claves ocultas.

### Problema 3 — La base de datos no existía

**Síntoma:** todas las peticiones devolvían `HTTP 404`.

**Diagnóstico:** se comprobó la existencia de la base de datos probando las
posibles direcciones. Todas devolvieron 404, confirmando que el proyecto de
Firebase **no tenía ninguna base de datos creada**.

**Solución:** se creó desde la consola en modo prueba.

### Problema 4 — Firebase vacío borraba los datos locales

**Síntoma:** un bug lógico propio. Al abrir la app, `cargar()` hacía:

```kotlin
val productos = api.listar()   // vacío la primera vez
local.escribir(productos)      // sobreescribía el local con lista vacía
```

Como la base de datos estaba recién creada y vacía, el primer arranque
**borraba el catálogo local**, y el botón "Subir JSON a Firebase" ya no tenía
nada que enviar. La app parecía rota.

**Solución:** se añadió una validación en `ProductoRepository.kt`:

```kotlin
val productos = api.listar()
if (productos.isEmpty()) {
    return@conHilo Resultado(local.leer(), Origen.LOCAL,
        "Firebase esta vacio. Usa 'Subir JSON a Firebase' para sembrarlo")
}
local.escribir(productos)
```

Ahora Firebase vacío **no** destruye el respaldo local.

### Problema 5 — Errores invisibles

**Síntoma:** si Firebase fallaba, la app solo mostraba el JSON local sin
explicar por qué.

**Solución:** en `FirebaseApi.kt` se añadieron mensajes de error según el
código HTTP:

| Código | Significado | Mensaje mostrado |
|---|---|---|
| 404 | La base de datos no existe | Indica crearla en la consola |
| 401 / 403 | Reglas de seguridad bloquean | Indica ponerla en modo prueba |

Esto convierte un fallo silencioso en un mensaje accionable.

---

## 6. Guía para la demostración

**Preparación (antes de presentar):**

1. Verificar la conexión ejecutando `./verificar-firebase.sh` — debe dar 5/5 OK.
2. Comprobar que la base de datos tiene datos en la consola de Firebase.
3. Tener el teléfono con depuración USB activa y la app instalada.

**Demostración sugerida:**

1. Abrir la app → se cargan los productos desde Firebase automáticamente.
2. Pulsar `+` → crear un producto nuevo → **Guardar**.
3. Ir a la consola de Firebase y mostrar que el producto apareció.
4. Volver a la app → pulsar **Editar** → cambiar el precio → **Guardar**.
5. Verificar el cambio en la consola.
6. Pulsar **Eliminar** → **leer con atención el nombre del producto en el
   diálogo** → confirmar.
7. Verificar la eliminación en la consola.

> **Importante:** el diálogo de confirmación muestra *"Se eliminará X de
> Firebase"*. Leer siempre el nombre antes de confirmar, para no borrar el
> producto equivocado.

---

## 7. Posibles preguntas del docente

**P: ¿Por qué no usaste Firebase SDK sino peticiones REST?**
R: La guía de la materia pide una **API REST** con endpoints y pruebas desde
Postman, por eso implementé el cliente HTTP. La alternativa sería usar el SDK
oficial (`google-services.json` + Firestore), que es más breve pero no expone
las peticiones HTTP que la guía exige demostrar.

**P: ¿Cómo sé que la app no está guardando la clave secreta?**
R: Porque se eliminó del código. Además, el archivo de *service account*
(private key) **nunca** se incluye en la app: es para uso del servidor. Está en
`.gitignore` y se puede verificar descomprimiendo el APK y buscando la cadena
`BEGIN PRIVATE KEY` (no aparece).

**P: ¿Es seguro que cualquiera pueda leer la base de datos con la URL?**
R: No, y esa es precisamente la lección. La seguridad **no** depende de
ocultar la URL (es pública por naturaleza), sino de las **Security Rules**.
En producción se usarían reglas que exijan autenticación.

**P: ¿Qué pasa si el móvil no tiene internet?**
R: El patrón *offline first* lo cubre: primero se guarda en el archivo local y
luego se intenta subir a Firebase. Si falla, el dato no se pierde y la app
informa que se guardó solo localmente.

**P: ¿Por qué el precio usa punto y no coma?**
R: Porque en Bolivia el separador decimal es el punto (Bs 34.50). El formato
se genera con `Locale.US` para garantizarlo. El formulario además acepta coma
y la convierte automáticamente.

**P: ¿Qué diferencia hay entre PUT y PATCH?**
R: `PUT` reemplaza el recurso **completo**, mientras que `PATCH` modifica solo
los campos que se envían. En la app el método `actualizar()` usa `PATCH`, y el
método `subir()` (carga masiva del JSON local) usa `PUT`. Firebase acepta ambos
en la misma URL. Aclaración honesta: en la app se envían todos los campos al
editar, así que técnicamente un `PUT` también sería válido; se eligió `PATCH`
porque es más seguro ante campos que se agregaran en el futuro, ya que no
borra los datos que no se incluyen en la petición.

---

## 8. Limitaciones y trabajo futuro

Es honesto mencionarlas en la defensa; demuestran criterio técnico.

- **Sin autenticación de usuarios:** cualquiera con la URL puede escribir. Se
  podría integrar Firebase Authentication para que cada usuario vea solo sus
  productos.
- **Reglas abiertas:** en modo prueba las reglas permiten todo. Antes de pasar
  a producción deben cerrarse con reglas específicas.
- **Sin validación en el servidor:** la app valida los campos, pero un cliente
  malicioso podría enviar datos inválidos directamente por REST. La validación
  real debe hacerse en las **reglas de Firebase**.
- **Sin caché avanzada:** el respaldo local es funcional pero no
  sincroniza cambios automáticos si el dispositivo estuvo sin conexión.

---

## 9. Comandos útiles

```bash
# Compilar
./gradlew :app:assembleDebug

# Ejecutar los tests
./gradlew :app:testDebugUnitTest

# Instalar en el teléfono por USB
./gradlew :app:installDebug

# Verificar la API sin abrir la app
./verificar-firebase.sh

# Desinstalar
adb uninstall com.primerexample.firebaseapi
```

---

## 10. Conclusión

Se desarrolló una aplicación Android en Kotlin con Jetpack Compose que funciona
como cliente de una API REST sobre Firebase Realtime Database, implementando las cuatro
operaciones CRUD, con una arquitectura por capas, manejo de errores y un
respaldo local que evita la pérdida de datos.

El trabajo más importante no fue escribir el código, sino **diagnóstico**:
identificar que la base de datos no existía, que la autenticación usada estaba
obsoleta, y que la configuración nunca se había completado. Cada uno de esos
problemas se verificó comprobando el comportamiento real del servidor, no
suponiendo.
