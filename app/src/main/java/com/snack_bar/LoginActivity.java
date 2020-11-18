package com.snack_bar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    ImageView launchScan;
    Button login;
    boolean isEmailValid, isPasswordValid;
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        launchScan = (ImageView) findViewById(R.id.launchScan);
        login = (Button) findViewById(R.id.login);
         String barcode = getIntent().getStringExtra("code");
         password.setText(barcode);
         String emailInfo = getIntent().getStringExtra("email");
          email.setText(emailInfo);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetValidation();
            }
        });
        launchScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Scan Launch.....", Toast.LENGTH_SHORT).show();
                // redirect to RegisterActivity
                Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
                intent.putExtra("email",email.getText().toString());
                startActivity(intent);
            }
        });


    }

    //AUTHENTICATION
    private void login(){
        JsonObject login = new JsonObject();
        login.addProperty ("Email",email.getText().toString());
        login.addProperty("Password",password.getText().toString());
        // Using the Retrofit
        ApiInterface apiService =ApiClient.getClient().create(ApiInterface.class);
        Call<Object> call = apiService.login (email.getText().toString(),password.getText().toString());
        showProgress("Authentification ",true);
        call.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                try{
                    showProgress("Authentification.",false);
//                    Log.e("response-success", response.body().toString());
                    Log.d("SERVER",response.message());
//                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
//                    Log.d("LOGIN","SUCCEED...");
                    SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isLogin", true);
                    editor.putString("email", email.getText().toString());
                    editor.putString("password", password.getText().toString());
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Successfully", Toast.LENGTH_SHORT).show();
                    //redirect to RegisterActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("email",email.getText().toString());
                    startActivity(intent);
                    finish();

                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("SERVER ERROR",e.toString());
                    showProgress("Synchronisation des empreintes terminée.",false);
                }
            }


            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e("response-failure", call.toString());
                showProgress("Synchronisation des empreintes terminée.",false);
            }

        });
    }


    public void SetValidation() {
        // Check for a valid email address.
        if (email.getText().toString().isEmpty()) {
            //emailError.setError(getResources().getString(R.string.email_error));
            isEmailValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            //emailError.setError(getResources().getString(R.string.error_invalid_email));
            isEmailValid = false;
        } else  {
            isEmailValid = true;
            //emailError.setErrorEnabled(false);
        }

        // Check for a valid password.
        if (password.getText().toString().isEmpty()) {
            //passError.setError(getResources().getString(R.string.password_error));
            isPasswordValid = false;
        } else if (password.getText().length() < 6) {
            //passError.setError(getResources().getString(R.string.error_invalid_password));
            isPasswordValid = false;
        } else  {
            isPasswordValid = true;
           // passError.setErrorEnabled(false);
        }

        if (isEmailValid && isPasswordValid) {
            login();
//            SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
//            SharedPreferences.Editor editor = sp.edit();
//            editor.putBoolean("isLogin", true);
//            editor.apply();
//            Toast.makeText(getApplicationContext(), "Successfully", Toast.LENGTH_SHORT).show();
//            // redirect to RegisterActivity
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            intent.putExtra("email",email.getText().toString());
//            startActivity(intent);
        }

    }

    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage(msg);
            dialog.setCancelable(false);
        }

        if (show)
        {
            dialog.show();
        } else
        {
            dialog.dismiss();
        }
    }
    //Shows a message by using Snackbar
    private void showMessage(Boolean isSuccessful, String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        if (isSuccessful)
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.colorAccent));
        } else
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
}