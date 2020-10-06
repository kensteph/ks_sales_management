package com.snack_bar.network;
import com.google.gson.JsonObject;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {
    @GET("products/")
    Call<JsonObject> getAllProducts();
    @GET("employees/")
    Call<JsonObject> getAllEmployees();
    @FormUrlEncoded
    @POST("sales/")
    Call<JsonObject> UploadSaleToServer(@Field("data") String json);
    @FormUrlEncoded
    @POST("employees/finger_prints/")
    Call<JsonObject> UploadEmployeeToServer(@Field("data") String json);

}
