using Microsoft.ProjectOxford.Face;
using Microsoft.ProjectOxford.Face.Contract;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ApplicationAITraining
{
    class Program
    {
        FaceServiceClient faceServiceClient = new FaceServiceClient("e3275864a134451abf93c69eb5e372de", "https://westcentralus.api.cognitive.microsoft.com/face/v1.0");

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
        static void Main(string[] args)
        {
            //new Program().CreatePersonGroup("hollywoodstar","Hollywood Star");

            //new Program().AddPersonToGroup("hollywoodstar", "Tom Cruise", @"C:\Users\Gerald PC\Desktop\training\tom");
            //new Program().AddPersonToGroup("hollywoodstar", "Gerald Mora", @"C:\Users\Gerald PC\Desktop\training\gerald\");
            //new Program().AddPersonToGroup("hollywoodstar", "Jim Carrey", @"C:\Users\Gerald PC\Desktop\training\jim\");
            //new Program().AddPersonToGroup("hollywoodstar", "Adam Sandler", @"C:\Users\Gerald PC\Desktop\training\adam\");
            //new Program().AddPersonToGroup("hollywoodstar", "Carlos Alvarado", @"C:\Users\Gerald PC\Desktop\training\carlos\");
            //new Program().AddPersonToGroup("hollywoodstar", "Eminem", @"C:\Users\Gerald PC\Desktop\training\eminem\");

            //new Program().TrainingAI("hollywoodstar");

            //new Program().RecognitionFace("hollywoodstar", @"C:\Users\Gerald PC\Desktop\training\gerald.jpg");

            Console.ReadLine();
        }
    }
}
