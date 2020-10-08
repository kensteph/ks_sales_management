package com.snack_bar.network;
import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

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
    Call<JsonObject> UploadFingerPrintsToServer(@Field("data") String json);
    @Multipart
    @POST("finger_prints/")
    Call<RequestBody> uploadImage(@Part MultipartBody.Part part, @Part("somedata") RequestBody requestBody);
    @Multipart
    @POST("finger_prints/")
    Call<ServerResponse> upload(
            @Header("Authorization") String authorization,
            @PartMap Map<String, RequestBody> map
    );

}
