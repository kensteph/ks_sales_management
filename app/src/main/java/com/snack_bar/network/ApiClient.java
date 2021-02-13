package com.snack_bar.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    //public static final String BASE_URL = "https://saudeezagency.com/sales/";
      public static final String BASE_URL = "http://107.180.77.51:8080/PointOfSale/api/";
    //public static final String BASE_URL = "http://192.169.153.227/TAGO/api/";
    //Login: { 'Email': 'gillesw2000@hotmail.com', 'Password': 'R3NLO3XZA4MC3TCD' }

    private static Retrofit retrofit = null;
    public static Retrofit getClient() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

         retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit;
    }
}
