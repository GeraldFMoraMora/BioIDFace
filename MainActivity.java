package edmt.dev.cognitiveidentifyface;

import android.app.ProgressDialog;
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

public class MainActivity extends AppCompatActivity {

    private FaceServiceClient mFaceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "e3275864a134451abf93c69eb5e372de");
    private final String mPersonGroupId = "hollywoodstar";

    private final int PICK_IMAGE = 1;

    public ImageView mImageView;
    public Bitmap mBitmap;
    public Face[] mFacesDetected;

    /**
     * Metodo autogenerado que cargara todos los elementos al activity apenas corra la aplicacion.
     *
     * @param savedInstanceState
     */
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

    /**
     * Metodo autogenerado que estara en escucha de la acciones con los objetos como los botones
     * con cada accion ejecutara una actividad distinta.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * La tarea de deteccion de rostros se va a ejecutar como una clase distinta. Esta clase se
     * encarga de detectar un rostro y de este recibe un ID unico.
     */
    class detectTask extends AsyncTask<InputStream, String, Face[]> {
        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected Face[] doInBackground(InputStream... params) {
            try {
                publishProgress("Detectando rostro...");
                Face[] results = mFaceServiceClient.detect(params[0], true, false, null);
                if (results == null) {
                    publishProgress("Detección finalizada. No hay ningun rostro en la imagen");
                    return null;
                } else {
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

    /**
     * Se crea una clase para la tarea de
     */
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
                    return null;
                }
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

    /**
     *
     */
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
}
