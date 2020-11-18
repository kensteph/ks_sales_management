 package com.snack_bar;

 import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;

 public class ScannerActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener{
     BarcodeReader barcodeReader;
     String emailInfo="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        // get the barcode reader instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
        emailInfo = getIntent().getStringExtra("email");
    }

     @Override
     public void onScanned(Barcode barcode) {
         // playing barcode reader beep sound
         barcodeReader.playBeep();
         // ticket details activity by passing barcode
         Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
         intent.putExtra("code", barcode.displayValue);
         intent.putExtra("email",emailInfo);
         startActivity(intent);
     }

     @Override
     public void onScannedMultiple(List<Barcode> barcodes) {

     }

     @Override
     public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

     }

     @Override
     public void onScanError(String errorMessage) {

     }

     @Override
     public void onCameraPermissionDenied() {

     }
 }