 package com.snack_bar;

 import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

 public class ScannerActivity extends AppCompatActivity{
     private CodeScanner mCodeScanner;
     String emailInfo="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        if(getIntent().hasExtra("email")){
            emailInfo = getIntent().getStringExtra("email");
        }
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.password.setText( result.getText());
                        LoginActivity.email.setText(emailInfo);
                        Toast.makeText(ScannerActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
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
         mCodeScanner.startPreview();
     }

     @Override
     protected void onPause() {
         mCodeScanner.releaseResources();
         super.onPause();
     }

 }