package com.snack_bar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.model.FingerPrintTemp;
import com.snack_bar.util.Helper;

import java.util.ArrayList;
import java.util.List;

import asia.kanopi.fingerscan.Fingerprint;
import asia.kanopi.fingerscan.Status;

public class AddFingerPrintActivity extends AppCompatActivity {
    private int selectedEmployeeID = 0;
    private ImageView leftFinger1, leftFinger2, rightFinger1, rightFinger2;
    private TextView tvStatus;
    private TextView tvError;
    private Button buttonAddFingerprint;
    private Fingerprint fingerprint;
    private DatabaseHelper db;
    private Helper helper;
    private ProgressDialog dialog;
    private String fingerSelected;
    private byte[] LF1, LF2, RF1, RF2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_finger_print);
        Intent dataFromActivity = getIntent();
        Employee employee = (Employee) dataFromActivity.getSerializableExtra("Employe");
        selectedEmployeeID = employee.getEmployee_id();
        String employeeInfo = employee.getEmployee_prenom() + " " + employee.getEmployee_nom() + " | " + selectedEmployeeID;
        getSupportActionBar().setTitle(employeeInfo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //HELPER
        helper = new Helper();
        //DATABASE
        db = new DatabaseHelper(this);
        //FINGERPRINT INSTANCE FROM KANOPI
        fingerprint = new Fingerprint();
        //VIEWS
        buttonAddFingerprint = findViewById(R.id.buttonAddFingerprint);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvError = (TextView) findViewById(R.id.tvError);
        //FINGERS
        leftFinger1 = (ImageView) findViewById(R.id.finger_left1);
        leftFinger2 = (ImageView) findViewById(R.id.finger_left2);
        rightFinger1 = (ImageView) findViewById(R.id.finger_right1);
        rightFinger2 = (ImageView) findViewById(R.id.finger_right2);


        leftFinger1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan("LF1");
            }
        });
        leftFinger2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan("LF2");
            }
        });
        rightFinger1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan("RF1");
            }
        });
        rightFinger2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan("RF2");
            }
        });

        //ADD FINGERPRINTS IN LOCAL DB
        buttonAddFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SaveFingerPrintsInBackground().execute();
            }
        });

    }

    private void saveFingerPrintTo(String server) {
        if (server == "SERVER") {
            //saveFingerprintToServer();
        } else {
            saveFingerprintLocalDB();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("status", -1);
                setResult(RESULT_CANCELED, intent);
                fingerprint.turnOffReader();
                //setContentView(R.layout.cart_content);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startScan(String finger) {
        fingerSelected = finger;
        fingerprint.scan(this, printHandler, updateHandler);
        Toast.makeText(getApplicationContext(), "FINGER : " + finger, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        fingerprint.turnOffReader();
        super.onStop();
    }

    Handler updateHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int status = msg.getData().getInt("status");
            tvError.setText("");
            switch (status) {
                case Status.INITIALISED:
                    tvStatus.setText("Setting up reader");
                    break;
                case Status.SCANNER_POWERED_ON:
                    tvStatus.setText("Reader powered on");
                    break;
                case Status.READY_TO_SCAN:
                    tvStatus.setText("Ready to scan finger");
                    break;
                case Status.FINGER_DETECTED:
                    tvStatus.setText("Finger detected");
                    break;
                case Status.RECEIVING_IMAGE:
                    tvStatus.setText("Receiving image");
                    break;
                case Status.FINGER_LIFTED:
                    tvStatus.setText("Finger has been lifted off reader");
                    break;
                case Status.SCANNER_POWERED_OFF:
                    tvStatus.setText("Reader is off");
                    break;
                case Status.SUCCESS:
                    tvStatus.setText("Fingerprint successfully captured");
                    break;
                case Status.ERROR:
                    tvStatus.setText("Error");
                    tvError.setText(msg.getData().getString("errorMessage"));
                    break;
                default:
                    tvStatus.setText(String.valueOf(status));
                    tvError.setText(msg.getData().getString("errorMessage"));
                    break;

            }
        }
    };
    Handler printHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            byte[] image;
            String errorMessage = "empty";
            int status = msg.getData().getInt("status");
            Intent intent = new Intent();
            intent.putExtra("status", status);
            if (status == Status.SUCCESS) {
                image = msg.getData().getByteArray("img");
                intent.putExtra("img", image);
                //CONVERT IMAGE TO BITMAP
                Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
                switch (fingerSelected) {
                    case "LF1":
                        leftFinger1.setImageBitmap(bm);
                        LF1 = image;
                        break;
                    case "LF2":
                        leftFinger2.setImageBitmap(bm);
                        LF2 = image;
                        break;
                    case "RF1":
                        rightFinger1.setImageBitmap(bm);
                        RF1 = image;
                        break;
                    case "RF2":
                        rightFinger2.setImageBitmap(bm);
                        RF2 = image;
                        break;
                }
            } else {
                errorMessage = msg.getData().getString("errorMessage");
                intent.putExtra("errorMessage", errorMessage);
                tvError.setText(errorMessage);
            }

        }
    };

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(AddFingerPrintActivity.this);
            dialog.setMessage(msg);
            dialog.setCancelable(false);
        }

        if (show) {
            dialog.show();
        } else {
            dialog.dismiss();
        }
    }

    //Shows a message by using Snackbar
    private void showMessage(Boolean isSuccessful, String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        if (isSuccessful) {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(AddFingerPrintActivity.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(AddFingerPrintActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }

    //SAVE FINGERPRINT TO LOCAL DB
    private Boolean saveFingerprintLocalDB() {
        Boolean done = false;
        int qty = 0;
        byte[] serializeTemplate;
        String template, fingerImage;
        List<FingerPrintTemp> temporaryList = new ArrayList<>();

        if (LF1 != null && LF2 != null && RF1 != null && RF2 != null && selectedEmployeeID != 0) {
            //ADD THE FINGERPRINTS TO THE LIST
            //================== LF1 ============================
            FingerPrintTemp fingerPrintTempF1 = new FingerPrintTemp();
            //CREATE TEMPLATE
            serializeTemplate = helper.serializedTemplate(LF1);
            //CONVERT TO BASE64
            fingerImage = helper.byteArrayToBase64(LF1);
            template = helper.byteArrayToBase64(serializeTemplate);
            //SET THE INFO
            fingerPrintTempF1.setEmployeeId(selectedEmployeeID);
            fingerPrintTempF1.setFingerPrintImageBase64(fingerImage);
            fingerPrintTempF1.setFingerPrintTemplateBase64(template);
            fingerPrintTempF1.setFinger("LF1");
            temporaryList.add(fingerPrintTempF1);
            //================== LF2 ============================
            FingerPrintTemp fingerPrintTempF2 = new FingerPrintTemp();
            //CREATE TEMPLATE
            serializeTemplate = helper.serializedTemplate(LF2);
            //CONVERT TO BASE64
            fingerImage = helper.byteArrayToBase64(LF2);
            template = helper.byteArrayToBase64(serializeTemplate);
            //SET THE INFO
            fingerPrintTempF2.setEmployeeId(selectedEmployeeID);
            fingerPrintTempF2.setFingerPrintImageBase64(fingerImage);
            fingerPrintTempF2.setFingerPrintTemplateBase64(template);
            fingerPrintTempF2.setFinger("LF2");
            temporaryList.add(fingerPrintTempF2);
            //================== RF1 ============================
            FingerPrintTemp fingerPrintTempF3 = new FingerPrintTemp();
            //CREATE TEMPLATE
            serializeTemplate = helper.serializedTemplate(RF1);
            //CONVERT TO BASE64
            fingerImage = helper.byteArrayToBase64(RF1);
            template = helper.byteArrayToBase64(serializeTemplate);
            //SET THE INFO
            fingerPrintTempF3.setEmployeeId(selectedEmployeeID);
            fingerPrintTempF3.setFingerPrintImageBase64(fingerImage);
            fingerPrintTempF3.setFingerPrintTemplateBase64(template);
            fingerPrintTempF3.setFinger("RF1");
            temporaryList.add(fingerPrintTempF3);
            //================== RF2 ============================
            FingerPrintTemp fingerPrintTempF4 = new FingerPrintTemp();
            //CREATE TEMPLATE
            serializeTemplate = helper.serializedTemplate(RF2);
            //CONVERT TO BASE64
            fingerImage = helper.byteArrayToBase64(RF2);
            template = helper.byteArrayToBase64(serializeTemplate);
            //SET THE INFO
            fingerPrintTempF4.setEmployeeId(selectedEmployeeID);
            fingerPrintTempF4.setFingerPrintImageBase64(fingerImage);
            fingerPrintTempF4.setFingerPrintTemplateBase64(template);
            fingerPrintTempF4.setFinger("RF2");
            temporaryList.add(fingerPrintTempF4);

            //SAVE THE INFO TO DB
            if (db.addTemporaryFingerPrint(temporaryList)) {
                done = true;
                showMessage(true,"DONE");
                finish();
            }
        } else {
           showMessage(false,"Please provide all the Fingerprints");
        }

        return done;
    }

    //DIALOG SAVE FINGERPRINTS
    private void saveFingerPrints() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(AddFingerPrintActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(AddFingerPrintActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Add Fingerprints")
                .setMessage("Do you really want to SAVE THOSE FINGERPRINTS")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveFingerPrintTo("LOCAL");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        buttonAddFingerprint.setVisibility(View.VISIBLE);
                    }
                })
                .show();
    }
    //SAVE FINGERPRINTS IN RHE BACKGROUND
    public class SaveFingerPrintsInBackground extends AsyncTask<Void, Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return saveFingerprintLocalDB();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress("Saving FingerPrints....", true);

        }

        @Override
        protected void onPostExecute(Boolean done) {
            super.onPostExecute(done);
            showProgress("Saving FingerPrints....", false);
        }
    }

}