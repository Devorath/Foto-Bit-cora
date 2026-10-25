# Foto Bitácora

Aplicación académica para Android desarrollada con **Kotlin, vistas XML y SQLite**. Permite registrar usuarios, iniciar sesión y crear una bitácora personal de fotografías con ubicación y comentarios.

Los datos se almacenan localmente en el dispositivo. La aplicación no utiliza un servidor ni sincronización en la nube.

## Funciones

- Registro de nombre, apellidos, correo electrónico y contraseña.
- Validación de campos, formato del correo, contraseña de al menos ocho caracteres y confirmación de contraseña.
- Prevención de cuentas con el mismo correo.
- Inicio de sesión mediante correo y contraseña contra SQLite.
- Sesión local y cierre de sesión.
- Login con imagen, fondo degradado, textura y control para mostrar u ocultar la contraseña.
- Captura mediante una aplicación de cámara del dispositivo.
- Vista previa de la fotografía en la pantalla principal.
- Guardado del archivo en una carpeta específica de la app.
- Registro de coordenadas, precisión estimada, comentario y fecha.
- Galería personal y detalle de cada fotografía.

## Correspondencia con la consigna

| Requisito | Implementación |
| --- | --- |
| Registrar nombre, apellidos, correo y contraseña en una base local | `RegisterActivity.kt`, `DatabaseHelper.kt` y `PasswordUtils.kt` |
| Comparar las credenciales con la base local | `LoginActivity.kt` y `DatabaseHelper.kt` |
| Mostrar las opciones Tomar foto y Ver fotos | `MainActivity.kt` y `activity_main.xml` |
| Capturar, mostrar y guardar la foto con GPS y comentario | `MainActivity.kt`, `PhotoStorage.kt`, `GpsLocator.kt` y `DatabaseHelper.kt` |
| Consultar fotografías y mostrar comentario y ubicación | `GalleryActivity.kt`, `PhotoAdapter.kt` y `PhotoDetailActivity.kt` |

Esta tabla describe la implementación del código; la comprobación funcional se documenta en la lista de pruebas de este README.

## Configuración del proyecto

| Componente | Configuración |
| --- | --- |
| Identificador de la aplicación | `com.example.fotobitacora` |
| Android Gradle Plugin | `9.2.1` |
| Gradle configurado | `9.4.1` |
| SDK de compilación | API `37` |
| SDK objetivo | API `37` |
| SDK mínimo | API `30` — Android 11 |
| Compatibilidad del código Java | Java `17` |
| Base de datos | SQLite, mediante `SQLiteOpenHelper` |
| Interfaz | XML, AppCompat y RecyclerView |

Utiliza una versión de Android Studio compatible con AGP 9.2.1 y un JDK compatible con la versión de Gradle configurada. AGP 9 incorpora soporte para Kotlin; este proyecto no aplica por separado el plugin `org.jetbrains.kotlin.android`.

## Abrir y ejecutar

1. Clona este repositorio o descarga y descomprime su ZIP.
2. En Android Studio, selecciona **Open** y abre la carpeta que contiene `settings.gradle.kts` y `app/`.
3. Espera la sincronización de Gradle. La primera sincronización necesita conexión para descargar Gradle y las dependencias.
4. Instala Android SDK Platform 37 y los componentes que solicite Android Studio.
5. Selecciona un emulador o dispositivo con Android 11 o superior, cámara disponible y ubicación habilitada.
6. Ejecuta la configuración **app**.
7. Registra una cuenta antes de iniciar sesión por primera vez.

La entrega debe incluir estos cuatro archivos del Wrapper:

```text
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

### Compilación desde la terminal

En macOS o Linux, desde la raíz del proyecto:

```bash
chmod +x gradlew
./gradlew --version
./gradlew clean assembleDebug
```

En Windows, desde PowerShell:

```powershell
.\gradlew.bat --version
.\gradlew.bat clean assembleDebug
```

El mensaje `BUILD SUCCESSFUL` confirma la compilación, pero no sustituye las pruebas de cámara, ubicación y persistencia.

El archivo `local.properties` contiene la ruta local del SDK. Cada equipo debe utilizar su propia configuración; este archivo no debe publicarse en el repositorio.

## Uso de la aplicación

### 1. Crear una cuenta e ingresar

Selecciona **Crear una cuenta**, completa los datos y confirma la contraseña. Después utiliza el correo registrado y la contraseña para ingresar. El ojo permite mostrar u ocultar lo escrito. Si existe una sesión guardada, la aplicación abre directamente la pantalla principal.

### 2. Tomar y registrar una fotografía

1. Pulsa **Tomar foto** y concede el permiso de ubicación.
2. Realiza la captura en la cámara y confírmala.
3. La fotografía se muestra en la pantalla principal.
4. La aplicación obtiene la ubicación y abre el diálogo de registro.
5. Escribe un comentario y pulsa **Guardar**.

Si no se obtiene ubicación, puedes reintentar o descartar la captura. La foto no se registra en la base de datos hasta guardar sus coordenadas y comentario. Descartar elimina el archivo pendiente.

### 3. Consultar las fotografías

Pulsa **Ver fotos**. La lista muestra los registros del usuario autenticado. Selecciona uno para visualizar su imagen, comentario, latitud, longitud, precisión estimada y fecha.

## Almacenamiento y permisos

- **Usuarios y metadatos:** base SQLite `fotobitacora.db`.
- **Imágenes:** carpeta `Pictures` del almacenamiento específico de la app; si no está disponible, utiliza `filesDir/Pictures`.
- **Sesión:** identificador del usuario en `SharedPreferences`; no almacena allí la contraseña.
- **Ubicación:** permisos `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`, solicitados en tiempo de ejecución.
- **Cámara:** utiliza una aplicación externa con `ActivityResultContracts.TakePicture` y comparte el destino mediante `FileProvider`.

Las imágenes se guardan como archivos JPG. SQLite almacena su ruta, no el contenido binario. Borrar el almacenamiento de la app o desinstalarla puede eliminar los datos y archivos específicos de la aplicación.

Si se concede ubicación aproximada, las coordenadas tendrán menor precisión. La ubicación puede provenir de GPS o de un proveedor de red; se muestra su precisión estimada.

## Modelo de datos

| Tabla | Datos principales |
| --- | --- |
| `users` | Identificador, nombre, apellidos, correo único, sal, hash de contraseña y fecha de registro |
| `photos` | Identificador, usuario, ruta del archivo, latitud, longitud, precisión, comentario y fecha |

La relación es **un usuario a muchas fotografías**. `photos.user_id` referencia a `users.id`. Las consultas de galería y detalle filtran por el usuario autenticado.

Las contraseñas se verifican con PBKDF2-HMAC-SHA256 y sal aleatoria. Las consultas de credenciales utilizan parámetros SQL.

## Organización del código

Dentro de `app/src/main/java/com/example/fotobitacora/`:

| Paquete | Responsabilidad |
| --- | --- |
| `ui/login` | Registro e inicio de sesión |
| `ui/main` | Pantalla principal, captura y registro de fotografías |
| `data` | Base de datos y creación de archivos |
| `model` | Modelo `Photo` |
| `gallery` | Lista, adaptador y detalle |
| `utils` | Ubicación, sesión, contraseñas y funciones de interfaz |

Los diseños están en `app/src/main/res/layout/`; las imágenes y fondos, en `res/drawable/`.

## Bocetos

Consulta [BOCETOS.md](BOCETOS.md) para ver los bocetos textuales de las pantallas. Son documentación de baja fidelidad; no constituyen un archivo editable de Balsamiq.

## Pruebas funcionales

Marca cada casilla únicamente después de comprobarla en la versión que se entregará. Una casilla vacía significa que no hay resultado documentado en esta lista.

- [ ] El proyecto sincroniza y compila desde una copia limpia.
- [ ] Registra una cuenta y permite ingresar con ella.
- [ ] Rechaza campos vacíos, correo inválido, contraseña corta y confirmación diferente durante el registro.
- [ ] Impide registrar dos cuentas con el mismo correo.
- [ ] Rechaza credenciales incorrectas sin cerrar la aplicación.
- [ ] El ojo muestra y oculta la contraseña sin cambiar su contenido.
- [ ] Captura y muestra una fotografía en la pantalla principal.
- [ ] Cancelar la cámara permite intentar otra captura.
- [ ] Sin ubicación disponible, permite reintentar o descartar.
- [ ] Guarda fotografía, coordenadas y comentario.
- [ ] La galería y el detalle muestran los datos guardados.
- [ ] Al cerrar y abrir la aplicación, conserva los registros y las imágenes.
- [ ] Un segundo usuario no ve las fotografías del primero.
- [ ] Se comprueba el comportamiento al girar la pantalla durante el registro de la fotografía.

En el emulador, utiliza **Extended Controls → Location** para enviar una posición de prueba y habilita la ubicación del dispositivo virtual.

## Limitaciones conocidas

- No incluye recuperación de contraseña, sincronización ni respaldo en la nube.
- La obtención de ubicación puede necesitar reintentos, especialmente en interiores.
- Las operaciones de base de datos y verificación de contraseña se ejecutan actualmente en el hilo principal; pueden producir una pausa breve.
- La recuperación del diálogo de comentario tras girar la pantalla está pendiente de validar y ajustar. La ruta pendiente se conserva, pero el flujo puede quedar incompleto.
- La visualización de fotografías no aplica una corrección explícita de orientación EXIF; conviene comprobar capturas verticales y horizontales.

## Entrega académica

El instructor debe recibir el enlace al repositorio **público**, con código, recursos, configuración y Wrapper completo. Comprueba que el enlace sea accesible sin iniciar sesión.

No publiques `local.properties`, cachés, carpetas `build/`, claves de firma ni datos personales utilizados durante las pruebas. Las capturas de las pantallas y una demostración breve pueden añadirse como evidencia complementaria.
