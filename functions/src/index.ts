import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { v2 as cloudinary } from "cloudinary";

admin.initializeApp();

cloudinary.config({
  cloud_name: "dsgx4conh",
  api_key: "764752389264726",
  api_secret: "3vugQqQEwlyAC1SNutohAkZGiU0"
});

interface DeleteImageData {
  public_id: string;
}

export const deleteImage = functions.https.onCall(async (data: DeleteImageData, context) => {
  const publicId = data.public_id;

  if (!publicId) {
    throw new functions.https.HttpsError("invalid-argument", "Thiếu public_id.");
  }

  try {
    const result = await cloudinary.uploader.destroy(publicId);
    return { success: true, result };
  } catch (error: any) {
    throw new functions.https.HttpsError("internal", error.message);
  }
});
