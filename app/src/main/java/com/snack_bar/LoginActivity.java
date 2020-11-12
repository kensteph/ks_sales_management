package com.snack_bar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.Result;
import com.gpfreetech.awesomescanner.ui.GpCodeScanner;
import com.gpfreetech.awesomescanner.ui.ScannerView;
import com.gpfreetech.awesomescanner.util.DecodeCallback;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    ImageView launchScan;
    Button login;
    TextView register;
    boolean isEmailValid, isPasswordValid;
    TextInputLayout emailError, passError;
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    private GpCodeScanner mCodeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        launchScan = (ImageView) findViewById(R.id.launchScan);
        login = (Button) findViewById(R.id.login);
//        register = (TextView) findViewById(R.id.register);
//        emailError = (TextInputLayout) findViewById(R.id.emailError);
//        passError = (TextInputLayout) findViewById(R.id.passError);

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
                mCodeScanner.startPreview();
            }
        });

        ScannerView scannerView = findViewById(R.id.scanner_view);

        mCodeScanner = new GpCodeScanner(this, scannerView);

        mCodeScanner.setDecodeCallback(new DecodeCallback() {

            @Override
            public void onDecoded(@NonNull Result result) {
                Toast.makeText(getApplicationContext(), result.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCodeScanner!=null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if(mCodeScanner!=null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
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
            SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isLogin", true);
            editor.apply();
            Toast.makeText(getApplicationContext(), "Successfully", Toast.LENGTH_SHORT).show();
            // redirect to RegisterActivity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

    }
}