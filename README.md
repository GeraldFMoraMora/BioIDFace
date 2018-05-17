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

## Codificar un ejemplo
### Aplicacion en Visual Studio
#### Librerías necesarias
```c#
using Microsoft.ProjectOxford.Face;
using Microsoft.ProjectOxford.Face.Contract;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
```
#### Conexion con el servidor de Microsoft
```c#
FaceServiceClient faceServiceClient = new FaceServiceClient("e3275864a134451abf93c69eb5e372de", "https://westcentralus.api.cognitive.microsoft.com/face/v1.0");
```
> **Nota**: El código "e3275864a134451abf93c69eb5e372de" en el primer parámetro se refiere a la clave de subscriptor, esta tiene una validez de 7 días a partir de 16/05/2018.
> El segundo parámetro se refiere a la localización mas cercana para la API.
#### Creacion de grupo de personas
```c#
public async void CreatePersonGroup(string personGroupId, string personGroupName)
        {
            try
            {
                await faceServiceClient.CreatePersonGroupAsync(personGroupId, personGroupName);
            }catch (Exception ex)
            {
                Console.WriteLine("Error al crear person group\n" + ex.Message);
            }
        }
```
#### Agregar una persona a un grupo de personas
```c#
public async void AddPersonToGroup(string personGroupId, string name, string pathImage)
        {
            try
            {
                await faceServiceClient.GetPersonGroupAsync(personGroupId);
                CreatePersonResult person = await faceServiceClient.CreatePersonAsync(personGroupId, name);
                DetectFaceAndRegister(personGroupId, person, pathImage);
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error al agregar persona al grupo\n" + ex.Message);
            }
        }
```
#### Detectar un rostro y agregarlo
```c#
private async void DetectFaceAndRegister(string personGroupId, CreatePersonResult person, string pathImage)
        {
            foreach (var imgPath in Directory.GetFiles(pathImage,"*.jpg"))
            {
                using (Stream s = File.OpenRead(imgPath))
                {
                    await faceServiceClient.AddPersonFaceAsync(personGroupId, person.PersonId, s);
                }
            }
        }
```
#### Entrenar la inteligencia artificial del sistema Cognitive de Microsoft
```c#
public async void TrainingAI(string personGrooupID)
        {
            await faceServiceClient.TrainPersonGroupAsync(personGrooupID);
            TrainingStatus trainingStatus = null;
            while (true)
            {
                trainingStatus = await faceServiceClient.GetPersonGroupTrainingStatusAsync(personGrooupID);
                if (trainingStatus.Status != Status.Running)
                    break;
                await Task.Delay(15000);
            }
            Console.WriteLine("Training complete");
        }
```
#### Verificación de rostro
```c#
public async void RecognitionFace(string personGroupId, string imgPath)
        {

            using (Stream s = File.OpenRead(imgPath))
            {
                var faces = await faceServiceClient.DetectAsync(s);
                var faceIds = faces.Select(face => face.FaceId).ToArray();

                try
                {
                    var results = await faceServiceClient.IdentifyAsync(personGroupId, faceIds);
                    foreach(var identifyResult in results)
                    {
                        Console.WriteLine($"Result of face: { identifyResult.FaceId}");
                        if (identifyResult.Candidates.Length == 0)
                            Console.WriteLine("No se identifico a nadie");
                        else
                        {
                            //Candidato mas aproximado a lo esperado
                            var candidateId = identifyResult.Candidates[0].PersonId;
                            var person = await faceServiceClient.GetPersonAsync(personGroupId, candidateId);
                            Console.WriteLine($"Indentificado como: {person.Name}");
                        }
                    }

                }
                catch(Exception ex)
                {
                    Console.WriteLine("Error con el proceso de indentificacion "+ ex.Message);
                }
            }
        }
```
### Ejecución
#### Llamada a creación de grupo
```c#
new Program().CreatePersonGroup("<Identificador de grupo>","Nombre de grupo");
```
#### Llamada a adherir persona al grupo
```c#
new Program().AddPersonToGroup("<Nombre de grupo>", "<Nombre de persona>", @"C:\Users\<User>\Desktop\<Carpeta donde se encuentra>\.");
```
#### Llamada a entrenar el sistema
```c#
new Program().TrainingAI("<Identificador de grupo>");
```
#### Llamada a reconocimiento de rostro
```c#
new Program().RecognitionFace("<Identificador de grupo>", @"C:\Users\<User>\Desktop\<Carpeta donde se encuentra>\<imagen>.jpg");
```
