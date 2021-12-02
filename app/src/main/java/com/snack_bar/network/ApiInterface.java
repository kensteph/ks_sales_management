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

//===============================================LIVE API ==================================================================
    //LOGIN
    @FormUrlEncoded
    @POST("Login/Authenticate")
    Call<Object> login(@Field("Email") String email,@Field("Password") String password);

    //GET PRODUCTS FROM SERVER
    @FormUrlEncoded
    @POST("Products/All")
    Call<JsonObject> getAllProducts(@Field("Email") String email,@Field("Password") String password);


    //GET THE MAX USER ID FROM THE SERVER
    @FormUrlEncoded
    @POST("EmployeData/GetLastEmployee")
    Call<Integer> getMaxUserIdFromServer(@Field("Email") String email,@Field("Password") String password);

    //GET SINGLE EMPLOYEE'S FINGERPRINTS
    @FormUrlEncoded
    @POST("EmployeData/Get")
    Call<JsonObject> getEmployeeFingerPrints(@Field("id") int employeeRef,@Field("Email") String email,@Field("Password") String password);

    //GET MULTIPLE EMPLOYEES FINGERPRINTS
    @POST("EmployeData/GetEmployeesFingerPoint")
    Call<JsonObject> getLimitedFingerPrints(@Body JsonObject json);

    //GET ALL INFO ABOUT ALL EMPLOYEE
    @POST("EmployeData/GetAll")
    Call<JsonObject> getAllEmployee(@Body JsonObject json);

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

    //GET STUFFS TO RETURN FROM SERVER
    @FormUrlEncoded
    @POST("ReturnProducts/All")
    Call<JsonObject> getAllStuffs(@Field("Email") String email,@Field("Password") String password);

    //POST STUFF RETURN  TO SERVER
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("Sales/PostProductReturn")
    Call<JsonObject> postStuffReturn(@Body JsonObject json);






    @GET("employees/")
    Call<JsonObject> getAllEmployees(@Query("TypeEmployee") String TypeEmployee);

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
