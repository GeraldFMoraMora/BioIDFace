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

## Codificación
### Aplicación en Visual Studio
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
#### Conexión con el servidor de Microsoft
```c#
FaceServiceClient faceServiceClient = new FaceServiceClient("e3275864a134451abf93c69eb5e372de", "https://westcentralus.api.cognitive.microsoft.com/face/v1.0");
```
> **Nota**: 
> - El código "e3275864a134451abf93c69eb5e372de" en el primer parámetro se refiere a la clave de subscriptor, esta tiene una validez de 7 días a partir de 16/05/2018.
> - El segundo parámetro se refiere a la localización mas cercana para la API.
#### Creación de grupo de personas
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
>**Nota**:  
>- Se especifica un identificador que sea sencillo de recordar, este servirá para hacer las llamadas desde este mismo programa o inclusive para que el grupo pueda ser identificado desde Android. 
>- El nombre es simplemente representativo para este ejemplo.
##### Ejemplo:
```c#
new Program().CreatePersonGroup("hollywoodstar","Hollywood Star");
```
#### Llamada a adherir persona al grupo
```c#
new Program().AddPersonToGroup("<Nombre de grupo>", "<Nombre de persona>", @"C:\Users\<User>\Desktop\<Carpeta donde se encuentra>\.");
```
>**Nota**: 
>- Se especifica el directorio donde se encuentran las imágenes que corresponden a una persona. Este directorio puede contener varias imágenes e inclusive una, pero no puede estar vació. Esto favorece a que el sistema pueda entrenar de una mejor manera.
##### Ejemplo:
```c#
new Program().AddPersonToGroup("hollywoodstar", "Eminem", @"C:\Users\Gerald PC\Desktop\training\eminem\");
```
#### Llamada a entrenar el sistema
```c#
new Program().TrainingAI("<Identificador de grupo>");
```
>**Nota**: 
>- A este método solo se le pasa el identificador del grupo, se ejecuta una vez se hayan agregado las personas al grupo, inclusive siempre que se quiera agregar una nueva se puede volver a llamar.
##### Ejemplo:
```c#
new Program().TrainingAI("hollywoodstar");
```
#### Llamada a reconocimiento de rostro
```c#
new Program().RecognitionFace("<Identificador de grupo>", @"C:\Users\<User>\Desktop\<Carpeta donde se encuentra>\<imagen>.jpg");
```
>**Nota**: 
>- Este llama a un método para realizar pruebas, el cual se encarga de tomar la imagen, detectar el rostro y luego realizar la comparación en el grupo de persona para determinar con cual de todas obtiene un mejor parentezco. 
> - Como todo sistema biometrico, este esta expuesto a errores de determinación, en esto influye la calidad de la foto, la luz y otros factores que dificultan que se de un match preciso.
##### Ejemplo:
```c#
new Program().RecognitionFace("hollywoodstar", @"C:\Users\Gerald PC\Desktop\training\gerald.jpg");
```
## Aplicación en Android
### Librerías a importar
```java
import  android.app.ProgressDialog;  
import android.content.Intent;  
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.net.Uri;  
import android.os.AsyncTask;  
import android.os.Bundle;  
import android.provider.MediaStore;  
import android.support.v7.app.AppCompatActivity;  
import android.view.View;  
import android.widget.Button;  
import android.widget.ImageView;  
import android.widget.Toast;  
  
import com.microsoft.projectoxford.face.FaceServiceClient;  
import com.microsoft.projectoxford.face.FaceServiceRestClient;  
import com.microsoft.projectoxford.face.contract.Face;  
import com.microsoft.projectoxford.face.contract.IdentifyResult;  
import com.microsoft.projectoxford.face.contract.Person;  
import com.microsoft.projectoxford.face.contract.TrainingStatus;  
  
import java.io.ByteArrayInputStream;  
import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;  
import java.util.UUID;
```
### Variables a utilizar
```java
private FaceServiceClient mFaceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "e3275864a134451abf93c69eb5e372de");  
private final String mPersonGroupId = "hollywoodstar";  
  
private final int PICK_IMAGE = 1;  
  
public ImageView mImageView;  
public Bitmap mBitmap;  
public Face[] mFacesDetected;
```
> **Nota**: 
> - El código "e3275864a134451abf93c69eb5e372de" en el segundo parámetro se refiere a la clave de subscriptor, esta tiene una validez de 7 días a partir de 16/05/2018.
> - El primer parámetro se refiere a la localización mas cercana para la API.

### onCreate
```java
@Override  
protected void onCreate(Bundle savedInstanceState) {  
    super.onCreate(savedInstanceState);  
  setContentView(R.layout.activity_main);  
  
 this.mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face);  
 this.mImageView = (ImageView) findViewById(R.id.imageView);  
 this.mImageView.setImageBitmap(this.mBitmap);  
  Button btnDetect = (Button) findViewById(R.id.btnDetectFace);  
  Button btnIdentify = (Button) findViewById(R.id.btnIdentify);  
  btnIdentify.setVisibility(View.GONE);  
  
  btnDetect.setOnClickListener(new View.OnClickListener() {  
        @Override  
  public void onClick(View v) {  
            Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);  
  gallIntent.setType("image/*");  
  startActivityForResult(Intent.createChooser(gallIntent, "Seleccionar foto"), PICK_IMAGE);  
  }  
    });  
  btnIdentify.setOnClickListener(new View.OnClickListener() {  
        @Override  
  public void onClick(View v) {  
            final UUID[] faceIds = new UUID[mFacesDetected.length];  
 for (int i = 0; i < mFacesDetected.length; i++) {  
                faceIds[i] = mFacesDetected[i].faceId;  
  }  
            new IdentificationTask(mPersonGroupId).execute(faceIds);  
  }  
    });  
}
```
### onActivityResult
```java
@Override  
protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
    super.onActivityResult(requestCode, resultCode, data);  
  
 if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {  
        Uri uri = data.getData();  
 try {  
            //Se obtiene la imagen como mapa de bits  
  mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);  
  ImageView imageView = (ImageView) findViewById(R.id.imageView);  
  imageView.setImageBitmap(mBitmap);  
  
  //Convertir imagen a stream  
  ByteArrayOutputStream outputStream = new ByteArrayOutputStream();  
  mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);  
  ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());  
  
  Button btnIdentify = (Button) findViewById(R.id.btnIdentify);  
  btnIdentify.setVisibility(View.VISIBLE);  
  
  // Detectar y agregar.  
  new detectTask().execute(inputStream);  
  
  } catch (IOException e) {  
            e.printStackTrace();  
  }  
    }  
}
```
### Clase detectTask
```java
class detectTask extends AsyncTask<InputStream, String, Face[]> {  
    private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);  
  
  @Override  
  protected Face[] doInBackground(InputStream... params) {  
        try {  
            publishProgress("Detectando rostro...");  
  Face[] results = mFaceServiceClient.detect(params[0], true, false, null);  
 if (results == null) {  
                publishProgress("Detección finalizada. No hay ningun rostro en la imagen");  
 return null;  } else {  
                publishProgress(String.format("Detección finalizada. %d face(s) detectada", results.length));  
 return results;  
  }  
        } catch (Exception ex) {  
            return null;  
  }  
    }  
  
    @Override  
  protected void onPreExecute() {  
        mDialog.show();  
  }  
  
    @Override  
  protected void onPostExecute(Face[] faces) {  
        mDialog.dismiss();  
  mFacesDetected = faces;  
  }  
  
    @Override  
  protected void onProgressUpdate(String... values) {  
        mDialog.setMessage(values[0]);  
  }  
}
```
### Clase IdentificationTask
```java
private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {  
    String personGroupId;  
 private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);  
  
 public IdentificationTask(String personGroupId) {  
        this.personGroupId = personGroupId;  
  }  
  
    @Override  
  protected IdentifyResult[] doInBackground(UUID... params) {  
        try {  
            publishProgress("Obteniendo status del grupo de personas...");  
  TrainingStatus trainingStatus = mFaceServiceClient.getPersonGroupTrainingStatus(this.personGroupId);  
 if (trainingStatus.status != TrainingStatus.Status.Succeeded) {  
                publishProgress("El estado de entrenamiento del grupo de personas es: " + trainingStatus.status);  
 return null;  }  
            publishProgress("Identificando...");  
  
  IdentifyResult[] results = mFaceServiceClient.identity(personGroupId, // person group id  
  params // face ids  
  , 1); // maximo numero de cantidatos que se requiere retorne  
  
  return results;  
  
  } catch (Exception e) {  
            return null;  
  }  
    }  
  
    @Override  
  protected void onPreExecute() {  
        mDialog.show();  
  }  
  
    @Override  
  protected void onPostExecute(IdentifyResult[] identifyResults) {  
        mDialog.dismiss();  
 for (IdentifyResult identifyResult : identifyResults) {  
            new PersonDetectionTask(personGroupId).execute(identifyResult.candidates.get(0).personId);  
  }  
    }  
  
    @Override  
  protected void onProgressUpdate(String... values) {  
        mDialog.setMessage(values[0]);  
  }  
}
```
### Clase PersonDetectionTask
```java
private class PersonDetectionTask extends AsyncTask<UUID, String, Person> {  
    private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);  
 private String personGroupId;  
  
 public PersonDetectionTask(String personGroupId) {  
        this.personGroupId = personGroupId;  
  }  
  
    @Override  
  protected Person doInBackground(UUID... params) {  
        try {  
            publishProgress("Obteniendo status del grupo de personas...");  
  
 return mFaceServiceClient.getPerson(personGroupId, params[0]);  
  } catch (Exception e) {  
            return null;  
  }  
    }  
  
    @Override  
  protected void onPreExecute() {  
        mDialog.show();  
  }  
  
    @Override  
  protected void onPostExecute(Person person) {  
        mDialog.dismiss();  
  
  Toast.makeText(getApplicationContext(), person.name, Toast.LENGTH_SHORT).show();  
  }  
  
    @Override  
  protected void onProgressUpdate(String... values) {  
        mDialog.setMessage(values[0]);  
  }  
}
```
### Activity a utilizar
```xml
<LinearLayout  
  android:layout_width="match_parent"  
  android:layout_height="match_parent"  
  android:orientation="vertical">  
  
 <LinearLayout  android:id="@+id/groupButton"  
  android:layout_width="match_parent"  
  android:layout_height="wrap_content"  
  android:layout_alignParentBottom="true"  
  android:background="@color/colorPrimaryDark"  
  android:weightSum="2">  
  
 <Button  android:id="@+id/btnDetectFace"  
  android:layout_width="0dp"  
  android:layout_height="wrap_content"  
  android:layout_weight="1"  
  android:background="@android:color/holo_blue_dark"  
  android:text="Cargar rostro"  
  android:textColor="@android:color/background_light" />  
  
 <Button  android:id="@+id/btnIdentify"  
  android:layout_width="0dp"  
  android:layout_height="wrap_content"  
  android:layout_weight="1"  
  android:background="@android:color/holo_green_dark"  
  android:text="Identificar rostro"  
  android:textColor="@android:color/background_light" />  
  
 </LinearLayout>  
 <ImageView  android:id="@+id/imageView"  
  android:layout_width="match_parent"  
  android:layout_height="match_parent"  
  android:background="@color/colorPrimaryDark" />  
  
</LinearLayout>
```
