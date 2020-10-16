package com.snack_bar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;
import com.snack_bar.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import asia.kanopi.fingerscan.Fingerprint;
import asia.kanopi.fingerscan.Status;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //HELPER
        helper = new Helper();
        //DATABASE
        db=new DatabaseHelper(this);
        Bitmap imgFromServer = helper.getImageFromServer("https://saudeezagency.com/MyImages/coca-cola.jpg");
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
                leftFinger1.setImageBitmap(imgFromServer);
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
                buttonAddFingerprint.setVisibility(View.GONE);
                saveFingerPrintTo("SERVER");
            }
        });

    }

    private void saveFingerPrintTo(String server) {
        if(server=="SERVER"){
            saveFingerprintToServer();
        }else{
            saveFingerprintLocalDB();
        }
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
    //Shows a message by using Snackbar
    private void showMessage(Boolean isSuccessful, String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        if (isSuccessful)
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(AddFingerPrintActivity.this, R.color.colorAccent));
        } else
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(AddFingerPrintActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
    //SEND THE FINGERPRINTS TO SERVER
    private void saveFingerprintToServer(){
        prepareFingerPrintsToSend();
    }
    private void prepareFingerPrintsToSend() {
        if(LF1!= null && LF2!= null && RF1!= null && RF2!= null && selectedEmployeeID!=0) {
            List<byte[]> employeeFingers = new ArrayList<>();
            employeeFingers.add(LF1);
            employeeFingers.add(LF2);
            employeeFingers.add(RF1);
            employeeFingers.add(RF2);

            JSONArray array = new JSONArray();
            for (byte[] fp : employeeFingers) {
                String encodedTemplate ="";
                String encodedFinger ="";
                try {
                    //LEFT FINGER 1
                    JSONObject obj = new JSONObject();
                    byte[] serializeTemplate = helper.serializedTemplate(fp);
                    //CONVERT THE TEMPLATE TO BASE64
                    encodedTemplate = helper.byteArrayToBase64(serializeTemplate);
                    encodedFinger = helper.byteArrayToBase64(fp);
                    obj.put("EmployeeId", selectedEmployeeID);
                    obj.put("Finger", "");
                    obj.put("FingerPrint", encodedFinger);
                    obj.put("FingerPrintTemplate", encodedTemplate);
                    array.put(obj);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String data = array.toString();
            Log.d("SERVER", "JSON : " + data);
            postFingerPrints(data);
        }else{
            Toast.makeText(getApplicationContext(),"Veuillez prendre toutes les Empreintes...",Toast.LENGTH_LONG).show();
        }
    }
    private void postFingerPrints(String data) {
        showProgress("Envoie  des empreintes vers le serveur.....",true);
        List<Integer> salesSucceedID;
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.UploadFingerPrintsToServer(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String error="";
                String success="";
                //Get the response
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    JSONObject Response  = jsonObject.getJSONObject("response");
                    success = Response.getString("success");
                    error = Response.getString("error");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(success=="") {
                    //Toast.makeText(getApplicationContext()," Aucune empreinte n'a été synchronisée...", Toast.LENGTH_LONG).show();
                    showMessage(false, " Aucune empreinte n'a été synchronisée...");
                }else{
                    Toast.makeText(getApplicationContext(), "Les empreintes ont été synchronisées avec succès...", Toast.LENGTH_LONG).show();
                    finish();
                }

                Log.d("SERVER",jsonObject.toString());
                showProgress("Envoie des empreintes terminée.....",false);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("Envoie  des empreintes terminée.....",false);
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
                Log.d("SERVER",t.toString());
            }

        });
    }
    //SEND FINGERPRINT TO LOCAL DB
    private void saveFingerprintLocalDB(){
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
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Echec lors de la sauvegarde des emprentes...", Toast.LENGTH_LONG).show();
                buttonAddFingerprint.setVisibility(View.VISIBLE);
            }
        }else{
            Toast.makeText(getApplicationContext(),"Veuillez prendre toutes les Empreintes...",Toast.LENGTH_LONG).show();
            buttonAddFingerprint.setVisibility(View.VISIBLE);
        }
    }

}