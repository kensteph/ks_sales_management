package com.snack_bar.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.FingerPrintTemp;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;
import com.snack_bar.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncFingerPrintToServer extends AppCompatActivity {
    private DatabaseHelper db;
    private int nbFingerPrintsToSync = 0;
    private TextView nbFp;
    private ProgressDialog dialog;
    private Button btnSynchronizeFingerPrints;
    private List<FingerPrintTemp> temporaryFingerPrints;
    private Helper helper;
    //SHARED PREFERENCES
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    SharedPreferences sp;
    private String Email;
    private String Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_finger_print_to_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        helper = new Helper();
        db = new DatabaseHelper(this);

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email", "");
        Password = sp.getString("password", "");

        nbFingerPrintsToSync = db.getFingerCount();
        temporaryFingerPrints = new ArrayList<>();
        temporaryFingerPrints = db.getTemporaryFingers();
        nbFp = (TextView) findViewById(R.id.nbFpSync);
        nbFp.setText(nbFingerPrintsToSync + " Fingerprints to sync");

        btnSynchronizeFingerPrints = findViewById(R.id.btnSynchronizeFingerPrints);
        if (nbFingerPrintsToSync == 0) {
            btnSynchronizeFingerPrints.setEnabled(false);
        }
        btnSynchronizeFingerPrints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synchronizeFingerPrints();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(SyncFingerPrintToServer.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SyncFingerPrintToServer.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SyncFingerPrintToServer.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }

    //UPLOAD FINGERPRINTS TO SERVER
    private void postFingerPrints(String data) {
        showProgress("Synchronisation des ventes.....", true);
        List<Integer> salesSucceedID;
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.UploadFingerPrintsToServer(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("SERVER REP", response.body().toString());
                int nbSuccess = 0;
                //Get the response
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //Log.d(" responce => ", jsonObject.getJSONObject("body").toString());
//                    JSONObject Response  = jsonObject.getJSONObject("response");
//                    nbSuccess = Response.getInt("TotalSuccess");
//                    JSONArray list = Response.getJSONArray("SuccessId");
//                    for (int i = 0; i < list.length(); i++) {
//                        int saleId = list.getInt(i);
//                        databaseHelper.deleteSale(saleId);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(" responce => ", e.toString());
                }
                if (nbSuccess == 0) {
                    Toast.makeText(getApplicationContext(), " Aucune vente n'a été enregistrée...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), nbSuccess + " Ventes ont été enregistrées avec succès...", Toast.LENGTH_LONG).show();
                    temporaryFingerPrints.clear();
                    btnSynchronizeFingerPrints.setText("Aucune vente à synchroniser");
                }

                //Log.d("SERVER",jsonObject.toString());
                showProgress("Synchronisation des ventes terminée.....", false);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("Synchronisation des ventes terminée.....", false);
                Toast.makeText(getApplicationContext(), t.toString() + " | " + call.toString(), Toast.LENGTH_LONG).show();
                Log.d("SERVER", t.toString());
            }

        });
    }

    //PREPARE DATA TO SENT
    private void saveFingerPrintsToServer(List<FingerPrintTemp> listFingerPrints) {
        int pos = 1;
        for (FingerPrintTemp fpT : listFingerPrints) {
            testSendFingerPrintToServer(fpT, pos);
            pos++;
        }
    }

    //SERVER SIDE
    private void testSendFingerPrintToServer(FingerPrintTemp fpT, int pos) {
        JsonObject login = new JsonObject();
        JsonObject obj = new JsonObject();
        login.addProperty("Email", Email);
        login.addProperty("Password", Password);
        obj.addProperty("EmployeId", fpT.getEmployeeId());
        obj.addProperty("Finger", fpT.getFinger());
        obj.addProperty("FingerPrint", fpT.getFingerPrintImageBase64());
        obj.addProperty("Template", fpT.getFingerPrintTemplateBase64());
        obj.add("Login", login);
        String data = obj.toString();
        Log.d("SERVER", "JSON : " + data);
        postDataToServer(obj, pos, fpT.getEmployeeId());
    }

    private void postDataToServer(JsonObject obj, int pos, int employeeID) {
        // Using the Retrofit
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.postFingerPrint(obj);
        showProgress("Fingerprints Synchronization starts.. ", true);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    if (jsonObject.has("Accepted")) { //REQUEST SUCCESSFUL
                        Log.e("response-success", jsonObject.getString("Accepted"));
                        //REMOVE THIS
                        db.deleteTemporaryFingerPrints(employeeID);
                    } else {
                        Log.e("response-failed", "SALE DETAILS DON'T SAVE");
                        //showMessage(false,"SALE DETAILS DON'T SAVE");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (pos == temporaryFingerPrints.size()) {
                    emptyFingerPrintsTable();
                    showProgress("Fingerprints Synchronization complete.", false);
                    showMessage(true, "Fingerprints Synchronization complete");
                    nbFp.setText("");
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                Log.e("response-failure", call.toString());
                showProgress("Fingerprints Synchronization complete.", false);
                nbFp.setText("Verify your internet connection and retry....");
            }

        });
    }

    //DIALOG SYNCHRONIZE THE SALES FROM SERVER
    private void synchronizeFingerPrints() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(SyncFingerPrintToServer.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(SyncFingerPrintToServer.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Sync Fingerprints")
                .setMessage("Do you really want to SYNC THOSE FINGERPRINTS")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveFingerPrintsToServer(temporaryFingerPrints);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void emptyFingerPrintsTable() {
        //GET NUMBER OF FINGERPRINTS TO SYNC
        nbFingerPrintsToSync = db.getFingerCount();
        Log.d("SERVER", "FINGERPRINTS TO SEND COUNT : " + nbFingerPrintsToSync);
        if (nbFingerPrintsToSync == 0) {
            //EMPTY THE SALES TABLE
            db.emptyTable("empreintes_tmp");
            btnSynchronizeFingerPrints.setText("No Fingerprint to sync");
            btnSynchronizeFingerPrints.setEnabled(false);
            showMessage(true, "Synchronization complete...");
        }
    }


}