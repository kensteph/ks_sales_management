package com.snack_bar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.EmployeeFingerTemplate;
import com.snack_bar.model.FingerPrint;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public  class Helper {
private DatabaseHelper dbh;
private Bitmap image;
private Context context;
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";

    public Helper( Context context) {
        this.context = context;
    }

    public Helper() {
    }
    //CONVERT OBJECT TO JSON
    public String toJSON(Object object){
        Gson gson = new Gson();
        return gson.toJson(object);
    }

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
    public int verifyFingerPrint(FingerprintTemplate probe, List<EmployeeFingerTemplate> candidates){
        FingerprintMatcher matcher = new FingerprintMatcher().index(probe);
        double high = 0;
        int found = 0;
        for (EmployeeFingerTemplate candidate : candidates) {
            FingerprintTemplate fingerprintDB = new FingerprintTemplate(candidate.getFingerTemplate());
            double score = matcher.match(fingerprintDB);
            if (score > high) {
                high = score;
                //Get INFO ABOUT THE EMPLOYEE
                found = candidate.getEmployeeId();
                //break;
            }
        }
        double threshold = 40;
        return high >= threshold ? found : 0;
    }
    //FIND A FINGERPRINT AMONG ALL FINGERPRINTS
    public boolean verifySingleFingerPrint(FingerprintTemplate probe,FingerprintTemplate candidate){
        FingerprintMatcher matcher = new FingerprintMatcher().index(probe);
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

    public Bitmap getImageFromServer(String url){
        new AsyncTaskLoadImage(url).execute(url);
     return image;
    }

    public String byteArrayToBase64(byte[] arrayBytes){
        String imageString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            imageString = Base64.getEncoder().encodeToString(arrayBytes);
        }
        return  imageString;
    }

    public byte[] base64ToByteArray(String imageString ){
        byte[] arrayBytes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try{
                arrayBytes = Base64.getDecoder().decode(imageString);
            }catch (IllegalArgumentException ex){

            }
        }
        return  arrayBytes;
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
            image=bitmap;
            Log.d("LOADIMAGE",bitmapToBase64(image));
        }
    }

    public int getRandomId(int min, int max) {

        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public void uploadImage(String imageName,byte[] image) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), image);
        MultipartBody.Part parts = MultipartBody.Part.createFormData("newimage", imageName, requestBody);
        RequestBody someData = RequestBody.create(MediaType.parse("text/plain"), "This is a new Image");

        ApiInterface apiService=ApiClient.getClient().create(ApiInterface.class);
        Call call = apiService.uploadImage(parts, someData);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("upload",response.body().toString());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("uploadError",t.toString());

            }
        });
    }

    public String getCurrentDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String res = formatter.format(date);
        Log.e("DATE", "getCurrentDate: "+res);
        return res;
    }

}
