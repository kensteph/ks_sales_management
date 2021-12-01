package com.snack_bar.network;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class BackgroundService extends IntentService {
    public  BackgroundService(){
        super("BackgroundService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Using the Retrofit
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        JsonObject login=null;
        Call<JsonObject> call = apiService.getLimitedFingerPrints(login);

        Response<JsonObject> response = null;
        JsonObject object = null;
        try {
            Log.e("SERVER REQUEST","");
            response = call.execute();
            object = response.body();
            Log.e("SERVER OBJ COUNT",""+object);
            Log.e("SERVER OBJ COUNT","ADD IT TO DB");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
