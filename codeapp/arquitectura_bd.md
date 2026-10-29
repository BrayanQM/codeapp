# Documentación de Arquitectura de Base de Datos y Microservicios
**Proyecto:** Juego Educativo (Fundamentos de Programación)
**Plataformas:** Android (Kotlin + Jetpack Compose)
**Backend:** Supabase (PostgreSQL) + Firebase (Realtime Database)

# enlaces de bases de datos
FIREBASE
https://gen-lang-client-0559775518-default-rtdb.firebaseio.com/

SUPABASE_URL
https://bllmygtaumniqwvfyjgg.supabase.co

API KEY
sb_publishable_f3p7nrhpdfZQWgIQvYX6JA_F7lyZkW-

ANON PUBLIC
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJsbG15Z3RhdW1uaXF3dmZ5amdnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE4MzUyMzMsImV4cCI6MjEwNzQxMTIzM30.VD_ec1izPm3mtbvp1Lugwxt4fNMSrbxIOOo6lMus6X8


---

## 1. Visión General de la Arquitectura (Enfoque de Microservicios)

El sistema utiliza una arquitectura orientada a servicios (Microservicios adaptados a BaaS - Backend as a Service) para separar responsabilidades, mejorar la escalabilidad y permitir actualizaciones de contenido en caliente.

El cliente (App Android) actúa como el orquestador principal que se comunica con dos servicios independientes:

1.  **Servicio de Identidad y Estado (Supabase):** Gestiona la autenticación, la integridad de los datos del usuario, el progreso relacional y la lógica de negocio estricta (rachas, XP, logros).
2.  **Servicio de Contenido (Firebase):** Actúa como un CDN / Base de datos documental estática de solo lectura para el cliente. Sirve los niveles y mecánicas de juego en formato JSON sin bloquear la base de datos transaccional.

---

## 2. Especificaciones: Supabase (PostgreSQL)

**Propósito:** Manejo de estado, Auth e integridad referencial.
**Relaciones:** Modelo fuertemente tipado con llaves foráneas y eliminación en cascada (`ON DELETE CASCADE`).

### Esquema de Tablas (Esquema `public`)

#### Tabla: `perfiles`
Extiende la tabla interna `auth.users` de Supabase.
*   `id` (UUID, PK, FK a auth.users): Identificador único del usuario.
*   `nombre_usuario` (TEXT, UNIQUE): Nombre a mostrar en la app.
*   `url_avatar` (TEXT, NULLABLE): Enlace a la foto de perfil.
*   `fecha_creacion` (TIMESTAMP): Fecha de registro.

#### Tabla: `progreso_jugador`
Maneja el estado actual del jugador (Relación 1:1 con `perfiles`).
*   `id_usuario` (UUID, PK, FK a perfiles).
*   `id_nivel_actual` (TEXT): **Llave de enlace con Firebase** (ej. "nivel_01").
*   `experiencia_total` (INTEGER): Puntos globales.
*   `dias_racha` (INTEGER): Días consecutivos jugando.
*   `fecha_ultima_jugada` (TIMESTAMP): Para cálculo de rachas diarias.

#### Tablas de Logros (Relación Muchos a Muchos)
1.  **`logros` (Catálogo Maestro):** `id` (SERIAL), `nombre_codigo`, `titulo`, `descripcion`, `recompensa_xp`.
2.  **`logros_jugador` (Estado):** `id_usuario` (UUID), `id_logro` (INTEGER), `fecha_desbloqueo` (TIMESTAMP). PK Compuesta (`id_usuario`, `id_logro`).

### Automatización (Base de datos)
Existe un **Trigger (Disparador)** a nivel de base de datos (`manejar_nuevo_usuario()`). Cuando el Microservicio de Auth registra un usuario, PostgreSQL crea automáticamente su fila en `perfiles` y en `progreso_jugador`. *El desarrollador no debe insertar datos manualmente aquí al registrarse.*

---

## 3. Especificaciones: Firebase (Realtime Database)

**Propósito:** Entrega rápida de contenido dinámico (Niveles y Ejercicios).
**Estructura:** Árbol JSON NoSQL (Documental).

### Modelo de Datos (Nodo `/niveles`)
El contenido es polimórfico. Un nivel contiene un array de `ejercicios`, y cada ejercicio tiene un campo `tipo` que le indica al motor de UI (Jetpack Compose) qué vista renderizar.

```json
{
  "nivel_01": {
    "id": "nivel_01",
    "titulo": "Variables y Tipos de Datos",
    "xp_ganancia": 50,
    "ejercicios": [
      {
        "tipo": "opcion_multiple",
        "pregunta": "¿Qué tipo de dato guarda un nombre?",
        "opciones": ["String", "Int", "Boolean"],
        "respuesta_correcta": "String"
      },
      {
        "tipo": "completar_espacio",
        "pregunta": "La palabra clave para variables mutables es: ___",
        "opciones": ["var", "val", "const"],
        "respuesta_correcta": "var"
      }
    ]
  }
}


# AQUI ESTA PARA LA ESTRUCTURA DE CARPETAS Y PATRONES


Para mantener un proyecto escalable, testeable y ordenado, el estándar actual en Android (con Kotlin y Jetpack Compose) es utilizar la arquitectura **MVVM (Model-View-ViewModel)** combinada con los principios de **Clean Architecture**.

Aquí tienes la estructura de carpetas (Packages) que debes entregarle a tu desarrollador, junto con los patrones de diseño y las recomendaciones para llevar la app a nivel de producción.

## 1. Estructura de Carpetas y Archivos

Esta estructura divide la aplicación en tres capas principales: **Data** (Datos), **Domain** (Lógica de negocio) y **Presentation** (Interfaz gráfica).

```text
com.tuempresa.juegoprogramacion
│
├── 📁 di                 # Inyección de Dependencias (Dependency Injection)
│   ├── AppModule.kt      # Provee instancias de Supabase, Firebase y Repositorios
│
├── 📁 data               # Capa de Datos (¿De dónde viene la info?)
│   ├── 📁 remote         # Conexiones a los microservicios
│   │   ├── FirebaseClient.kt   # Lógica para descargar el JSON de Firebase
│   │   ├── SupabaseClient.kt   # Lógica para auth y queries a PostgreSQL
│   ├── 📁 model          # Data Classes planas (DTOs) exactas a la BD
│   │   ├── NivelDto.kt
│   │   ├── PerfilDto.kt
│   ├── 📁 repository     # Implementación real de la obtención de datos
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── GameRepositoryImpl.kt
│
├── 📁 domain             # Capa de Dominio (Reglas del negocio, independiente de librerías)
│   ├── 📁 model          # Data classes limpias para usar en la UI
│   │   ├── Nivel.kt
│   │   ├── Ejercicio.kt
│   ├── 📁 repository     # Interfaces (contratos) de los repositorios
│   │   ├── AuthRepository.kt
│   │   ├── GameRepository.kt
│   ├── 📁 usecase        # Casos de uso (Acciones específicas únicas)
│       ├── GetNivelActualUseCase.kt
│       ├── SaveProgresoUseCase.kt
│
└── 📁 presentation       # Capa de Presentación (UI con Jetpack Compose)
    ├── 📁 theme          # Colores, Tipografía y Formas (Material Design 3)
    ├── 📁 navigation     # Grafo de navegación entre pantallas (NavHost)
    ├── 📁 components     # UI Reutilizable (Botones, Barras de XP, Tarjetas)
    ├── 📁 auth           # Pantalla de Login / Registro
    │   ├── AuthScreen.kt
    │   ├── AuthViewModel.kt
    ├── 📁 home           # Pantalla principal (Mapa de niveles tipo Duolingo)
    │   ├── HomeScreen.kt
    │   ├── HomeViewModel.kt
    └── 📁 game           # Pantalla del juego (Donde se resuelven los ejercicios)
        ├── GameScreen.kt
        ├── GameViewModel.kt
        └── 📁 views      # Sub-vistas polimórficas
            ├── OpcionMultipleView.kt
            ├── ArrastrarBloquesView.kt
            ├── CompletarEspacioView.kt

```

---

## 2. Patrones de Diseño a Implementar

Para que esta arquitectura funcione como un reloj, el desarrollador debe aplicar los siguientes patrones de diseño:

1. **Repository Pattern (Patrón Repositorio):**
Centraliza el acceso a los datos. El `ViewModel` no sabe si los niveles vienen de Firebase, de Supabase o de una base de datos local. Solo le pide datos al Repositorio, y este decide de dónde sacarlos.
2. **Dependency Injection (DI - Hilt o Koin):**
Se usa para no instanciar objetos manualmente. En lugar de hacer `val firebase = FirebaseClient()`, Hilt "inyecta" la conexión a Firebase en los repositorios que lo necesiten. Esto es vital para poder hacer pruebas unitarias (testing).
3. **Factory Pattern (Patrón Fábrica):**
**¡Crucial para este juego!** Como los ejercicios que vienen de Firebase son dinámicos (opción múltiple, completar, arrastrar), se usa un Factory para leer el campo `"tipo"` del JSON y construir la vista de Compose correspondiente en tiempo de ejecución.
4. **Observer Pattern (StateFlow / UDF):**
Se utiliza UDF (Unidirectional Data Flow). La UI (Compose) solo "observa" un `StateFlow` en el `ViewModel`. Si el jugador gana 10 XP, el ViewModel actualiza el estado y la UI se repinta sola (recomposición).

---

## 3. Elementos Faltantes para una App de Producción

Para que el juego pase de ser un "prototipo" a una "aplicación profesional" lista para la Play Store, debes considerar lo siguiente:

### A. Persistencia Offline (Caché local)

* **El problema:** Si el usuario entra al tren subterráneo y pierde internet, no podrá cargar el nivel desde Firebase, y la app podría fallar.
* **La solución:** Añadir **Room Database** (SQLite de Android) en la capa `data`. Cuando la app tiene internet, descarga los primeros 5 niveles de Firebase y los guarda en Room. El jugador juega con los datos de Room. Cuando vuelve el internet, la app sincroniza los puntos ganados a Supabase.

### B. Seguridad y Ofuscación

* **El problema:** Si pones las llaves de Supabase (API Keys) directamente en el código de Kotlin, alguien podría descompilar la app y robarlas.
* **La solución:** Guardar las URLs y API Keys en el archivo `local.properties` y usar la librería **Secrets Gradle Plugin** para inyectarlas de forma segura. Además, activar **R8/ProGuard** para ofuscar el código antes de compilar el APK/AAB.

### C. Assets de Gamificación (Multimedia)

Un juego educativo sin estímulos visuales/sonoros aburre rápido. Necesitas proporcionar:

* **Lottie Animations (JSON):** Animaciones muy ligeras para celebrar cuando se completa un nivel (confeti, estrellas).
* **Archivos de audio (MP3/OGG):** Sonidos de "Respuesta Correcta" (Ding!), "Respuesta Incorrecta" (Buzzer) y "Nivel Completado".
* **Haptic Feedback:** Vibraciones sutiles del teléfono al arrastrar un bloque de código o al fallar una pregunta.

### D. Analíticas y Monitoreo (Crashlytics)

* Debes pedirle al desarrollador que integre **Firebase Analytics** para saber en qué nivel de programación la gente abandona la app (ej. "el 80% de los usuarios se rinde en el nivel de ciclos For").
* **Firebase Crashlytics:** Para que si la app se cierra inesperadamente en algún celular antiguo, te llegue un reporte con la línea de código exacta que falló.