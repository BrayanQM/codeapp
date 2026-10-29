
```text
codeApp/
├── .gradle/                       # Caché de Gradle
├── .idea/                         # Configuración de Android Studio / IntelliJ
├── .gitignore
├── build.gradle.kts               # Configuración global de Gradle
├── gradle/
│   └── wrapper/                   # Wrapper de Gradle
├── gradle.properties              # Propiedades globales de Gradle
├── gradlew                        # Script para ejecutar Gradle en Linux/macOS
├── gradlew.bat                    # Script para Windows
├── local.properties               # Configuración local del entorno (SDK, etc.)
├── settings.gradle.kts            # Incluye el módulo :app
├── app/
│   ├── build.gradle.kts           # Configuración del módulo app
│   ├── src/
│   │   ├── androidTest/
│   │   │   └── java/com/primerexample/codeapp/
│   │   │       └── ExampleInstrumentedTest.kt
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/
│   │   │   │   └── lessons.json
│   │   │   ├── java/com/primerexample/codeapp/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── LessonAssetDataSource.kt
│   │   │   │   │   │   └── ProgressLocalDataSource.kt
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── Lesson.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── CodeAppRepository.kt
│   │   │   │   ├── domain/
│   │   │   │   │   └── usecase/
│   │   │   │   │       └── LoadLessonsUseCase.kt
│   │   │   │   └── ui/
│   │   │   │       ├── CodeApp.kt
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt
│   │   │   │           ├── Theme.kt
│   │   │   │           └── Type.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap-anydpi/
│   │   │   │   ├── mipmap-hdpi/
│   │   │   │   ├── mipmap-mdpi/
│   │   │   │   ├── mipmap-xhdpi/
│   │   │   │   ├── mipmap-xxhdpi/
│   │   │   │   ├── mipmap-xxxhdpi/
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   └── keepRules/
│   │   │       └── rules.keep
│   │   └── test/
│   │       └── java/com/primerexample/codeapp/
│   │           └── ExampleUnitTest.kt
└──


Qué significa:
- app: módulo principal de la app Android
- data: acceso a datos y modelos
- domain: casos de uso
- ui: interfaz de usuario con Compose
- res: recursos visuales y de strings
- assets/lessons.json: datos de lecciones

Si querés, te lo puedo convertir en un README.md más “bonito” con descripción de cada carpeta y un resumen del flujo de la app.
