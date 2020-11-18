package com.snack_bar.network;

import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface ApiInterface {
    //LOGIN
    @FormUrlEncoded
    @POST("Login/Autenticate")
    Call<Object> login(@Field("Email") String email,@Field("Password") String password);

    //GET PRODUCTS FROM SERVER
    @FormUrlEncoded
    @POST("Products/All")
    Call<JsonObject> getAllProducts(@Field("Email") String email,@Field("Password") String password);

    //GET REFERENCES OF EMPLOYEES
    @FormUrlEncoded
    @POST("EmployeReferences/Get")
    Call<JsonObject> getEmployeesReferences(@Field("Email") String email,@Field("Password") String password);

    //GET ALL INFO ABOUT AN EMPLOYEE
    @FormUrlEncoded
    @POST("EmployeData/Get")
    Call<JsonObject> getEmployee(@Field("id") int employeeRef,@Field("Email") String email,@Field("Password") String password);

    //POST FINGER PRINT TO SERVER
    @FormUrlEncoded
    @POST("Biometrics/Post")
    Call<JsonObject> UploadFingerPrintsToServer(@Field("data") String json);

    //POST FINGERPRINTS TO SERVER
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("Biometrics/Post")
    Call<JsonObject> postFingerPrint(@Body JsonObject json);

    //POST SALES  TO SERVER
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("Sales/Post")
    Call<JsonObject> postSales(@Body JsonObject json);








    @GET("employees/")
    Call<JsonObject> getAllEmployees(@Query("TypeEmployee") String TypeEmployee);
    @FormUrlEncoded
    @POST("sales/")
    Call<JsonObject> UploadSaleToServer(@Field("data") String json);

    @Multipart
    @POST("finger_prints/")
    Call<RequestBody> uploadImage(@Part MultipartBody.Part part, @Part("somedata") RequestBody requestBody);
    @Multipart
    @POST("finger_prints/")
    Call<ServerResponse> upload(
            @Header("Authorization") String authorization,
            @PartMap Map<String, RequestBody> map
    );

    @FormUrlEncoded
    @POST("employees/finger_prints/")
    Call<String> UploadFingerPrints(@Field("data") String json);

}
