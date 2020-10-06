package com.snack_bar.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.snack_bar.model.FingerPrint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public  class Helper {

    //CREATE TEMPLATE FOR COMPARISON
    public FingerprintTemplate createTemplate(byte[] byteImage){
    FingerprintTemplate template = new FingerprintTemplate(
            new FingerprintImage()
                    .dpi(500)
                    .decode(byteImage));
    return template;
    }

    //CREATE TEMPLATE AND SAVE IT IN DB FOR COMPARISON
    public byte[] serializedTemplate(byte[] byteImage){
        FingerprintTemplate template = new FingerprintTemplate(
                new FingerprintImage()
                        .dpi(500)
                        .decode(byteImage));
        byte[] serialized = template.toByteArray();
        return serialized;
    }

    //VERIFY 2 BYTES ARRAY
    public FingerPrint verifyBytesArray(byte[] probe, List<FingerPrint> candidates){
        FingerPrint found = null;
        for (FingerPrint candidate : candidates) {
         if(Arrays.equals(probe,candidate.getFingerPrintByteArray())){
             found = candidate;
         }
        }
        return found;
    }
    //FIND A FINGERPRINT AMONG ALL FINGERPRINTS
    public FingerPrint verifyFingerPrint(FingerprintTemplate probe, List<FingerPrint> candidates){

        FingerprintMatcher matcher = new FingerprintMatcher()
                .index(probe);
        double high = 0;
        FingerPrint found = null;
        for (FingerPrint candidate : candidates) {
            //FingerprintTemplate fingerprintDB = createTemplate(candidate.getFingerPrintByteArray());
            FingerprintTemplate fingerprintDB = new FingerprintTemplate(candidate.getFingerPrintTemplate());
            double score = matcher.match(fingerprintDB);
            if (score > high) {
                high = score;
                found = candidate;
            }
        }
        double threshold = 40;
        return high >= threshold ? found : null;
    }

    //FIND A FINGERPRINT AMONG ALL FINGERPRINTS
    public boolean verifySingleFingerPrint(FingerprintTemplate probe,FingerprintTemplate candidate){
        FingerprintMatcher matcher = new FingerprintMatcher()
                .index(probe);
        double high = 0;
        boolean found = false;
            double score = matcher.match(candidate);
            if (score > high) {
                high = score;
                found = true;
            }
        double threshold = 40;
        return high >= threshold ? true : false;
    }

    public Bitmap base64ToBitmap(String base64){
        byte[] decodedString = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decodedString = Base64.getDecoder().decode(new String(base64));
        }
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return  decodedByte;
    }

    public String bitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            imageString = Base64.getEncoder().encodeToString(imageBytes);
        }
        return  imageString;
    }

    public byte[] bitmapToByteArray(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();
        return  byteArray;
    }

    public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
        private final static String TAG = "AsyncTaskLoadImage";
        private String URL;
        public AsyncTaskLoadImage(String URL) {
            this.URL = URL;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {

        }
    }

    public int getRandomId(int min, int max) {

        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

}
