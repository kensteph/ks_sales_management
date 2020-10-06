package com.snack_bar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.util.Helper;

import asia.kanopi.fingerscan.Fingerprint;
import asia.kanopi.fingerscan.Status;

public class AddFingerPrintActivity extends AppCompatActivity {
    private int selectedEmployeeID=0;
    private ImageView leftFinger1,leftFinger2,rightFinger1,rightFinger2;
    private TextView tvStatus;
    private TextView tvError;
    private Button buttonAddFingerprint;
    private Fingerprint fingerprint;
    private DatabaseHelper db;
    private Helper helper;
    private ProgressDialog dialog;
    private String fingerSelected;
    private byte[] LF1,LF2,RF1,RF2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_finger_print);
        Intent dataFromActivity = getIntent();
        Employee employee = (Employee) dataFromActivity.getSerializableExtra("Employe");
        selectedEmployeeID=employee.getEmployee_id();
        String employeeInfo=employee.getEmployee_prenom()+" "+employee.getEmployee_nom()+" | "+employee.getEmployee_code()+" | "+selectedEmployeeID;
        getSupportActionBar().setTitle(employeeInfo);
        //HELPER
        helper = new Helper();
        //DATABASE
        db=new DatabaseHelper(this);
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
                int qty = 0;
                if(LF1!= null && LF2!= null && RF1!= null && RF2!= null && selectedEmployeeID!=0){
                    if(db.addFingerPrint(LF1,selectedEmployeeID,"LF1")){
                        qty++;
                    }
                    if(db.addFingerPrint(LF2,selectedEmployeeID,"LF2")){
                        qty++;
                    }
                    if(db.addFingerPrint(RF1,selectedEmployeeID,"RF1")){
                        qty++;
                    }
                    if(db.addFingerPrint(RF2,selectedEmployeeID,"RF2")){
                        qty++;
                    }
                    if(qty==4) {
                        Toast.makeText(getApplicationContext(), "Empreintes ajoute avec succès ", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Echec lors de la sauvegarde des emprentes...", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Empreinte invalide...",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("status", -1);
                setResult(RESULT_CANCELED,intent);
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
        Toast.makeText(getApplicationContext(),"FINGER : "+finger,Toast.LENGTH_LONG).show();
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
                        LF2=image;
                        break;
                    case "RF1":
                        rightFinger1.setImageBitmap(bm);
                        RF1=image;
                        break;
                    case "RF2":
                        rightFinger2.setImageBitmap(bm);
                        RF2=image;
                        break;
                }
            } else {
                errorMessage = msg.getData().getString("errorMessage");
                intent.putExtra("errorMessage", errorMessage);
                tvError.setText(errorMessage);
            }

        }
    };


    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(AddFingerPrintActivity.this);
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


}