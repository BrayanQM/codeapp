Para conectar tu app de Android hecha con **Jetpack Compose** a Firebase, la arquitectura correcta utiliza **`google-services.json`** en el cliente (APK) para la identificación pública, junto con las librerías Kotlin KTX / BoM de Firebase.

---

1. **Colocar google-services.json en la app:** Solo identifica tu app ante el proyecto Firebase.
Descarga el archivo `google-services.json` desde la consola de Firebase y muévelo dentro de la carpeta `app/` de tu proyecto.

*Estructura de archivos:* `TuProyecto/app/google-services.json`


2. **Configurar dependencias Gradle (Kotlin DSL / .kts):**
En proyectos con Compose es común usar archivos `.kts`. Agrega los plugins y las librerías oficiales de Firebase.

**1. En `build.gradle.kts` (Project):**

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.1" apply false
}

```

**2. En `build.gradle.kts` (Module :app):**

```kotlin
plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Aplica el plugin
}

dependencies {
    // Firebase BoM (gestiona las versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Librería de Firestore para Kotlin
    implementation("com.google.firebase:firebase-firestore-ktx")
}

```


3. **Conectar Firestore en Jetpack Compose:**
En Jetpack Compose, gestionas el estado con un `ViewModel` para evitar que las llamadas a la base de datos bloqueen o recarguen la interfaz gráfica.

**1. Crear la ViewModel (`MainViewModel.kt`):**

```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var estadoGuardado = mutableStateOf("")

    fun guardarUsuario(nombre: String, correo: String) {
        val usuario = mapOf(
            "nombre" to nombre,
            "correo" to correo
        )

        db.collection("usuarios")
            .add(usuario)
            .addOnSuccessListener {
                estadoGuardado.value = "¡Guardado con éxito!"
            }
            .addOnFailureListener { e ->
                estadoGuardado.value = "Error: ${e.message}"
            }
    }
}

```

**2. Crear la pantalla en Compose (`HomeScreen.kt`):**

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(viewModel: MainViewModel = viewModel()) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.guardarUsuario(nombre, correo) }) {
            Text("Guardar en Firebase")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = viewModel.estadoGuardado.value)
    }
}

```


---
