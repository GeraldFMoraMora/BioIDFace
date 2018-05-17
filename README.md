# BioIDFace
Proyecto basado en algoritmos Biometricos para la detección de rostros y su verificación. Desarrollado en C# y Android, utilizando la tecnologia disponible de Microsoft Cognitive.
## Requisitos del sistema cliente (Hardware & Software)

-   Sistema Operativo  **Android**  version minima 6.0 (Marshmellow, API level 23) o hasta la version maxima 8.1 (Oreo, API level 27).
-   Memoria RAM 1.5GB o superior.
-   Espacio de almacenamiento 300MB.
-   Pantalla de 4.5" o superior.
-   Acceso a Internet.
-   Camara digital.

## Requisitos del sistema servidor (Hardware & Software)
- Sistema Operativo Windows (versiones 8.1 ó 10) o Linux last version.
- Memoria RAM (recomendada) 8GB o superior.
- Android Studio.
- Visual Studio 2015 (recomendado).
- Acceso a Internet.
## Preparar Visual Studio para Microsoft Cognitive

-   El desarrollador debe crear un nuevo proyecto desde **Visual Studio**, este debe ser tipo `Console Aplication`.
- Dirigirse al panel derecho **Explorador de soluciones**.
- Seleccionar el proyecto creado.
- Pulsar el boton derecho del mouse en Referencias y seleccionar **Administrar paquetes NuGet**.
- En el boton de busqueda escribir `Microsoft.Cognitive` y presionar la pestaña **Browser**.
- Seleccionar el paquete **Microsoff.ProjectOxford.Face** y posteriormente selccionar **Install**.
## Conectar la aplicación con Microsoft Cognitive

-   El desarrollador debe crear una aplicación vacía en **Android Studio**.
- Se debe agregar la dependencia para Microsoft Cognitive al archivo **build.gradle** (Module: app): `implementation 'com.microsoft.projectoxford:face:1.4.3'`
>**Nota**: Esta versión de Microsoft Oxford Project puede cambiar con el tiempo.
- No olvidar agregar el permiso de acceso a internet al archivo  **Manifest.xml**:  `<uses-permission android:name="android.permission.INTERNET"/>`

