package com.snack_bar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.model.FingerPrintTemp;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;
import com.snack_bar.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportFingerPrints extends AppCompatActivity {
    private DatabaseHelper db;
    private int nbFingerPrintsToImport = 0;
    private TextView nbFp;
    private ProgressDialog dialog;
    private Button btnImportFingerPrints;
    private List<Employee> noFingerPrintsList;
    private Helper helper;
    //SHARED PREFERENCES
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    private SharedPreferences sp;
    private String Email;
    private String Password;
    private JsonObject login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_finger_prints);
        getSupportActionBar().setTitle("Import Fingerprints");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nbFp = (TextView) findViewById(R.id.nbFpToImport);
        btnImportFingerPrints = findViewById(R.id.btnImportFingerPrints);

        //DB
        helper = new Helper();
        db = new DatabaseHelper(this);

        //CHECK THE EMPLOYEE'S LIST WITH NO FINGERPRINTS
        checkListNoFingerPrints();

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email", "");
        Password = sp.getString("password", "");

        login = new JsonObject();
        login.addProperty ("Email",Email);
        login.addProperty("Password",Password);

        if (nbFingerPrintsToImport == 0) {
            btnImportFingerPrints.setEnabled(false);
        }
        btnImportFingerPrints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importFingerPrintsDialog();
            }
        });
    }

    private void checkListNoFingerPrints() {
        noFingerPrintsList = new ArrayList<Employee>(); // EMPLOYEES NOT YET HAVE FINGERPRINTS
        noFingerPrintsList = db.getEmployeesWithNoFingerPrintsFromDB();
        nbFingerPrintsToImport = noFingerPrintsList.size();
        nbFp.setText(nbFingerPrintsToImport + " Fingerprints to import");
        Log.e("FINGERPRINTS","FINGERPRINTS TO IMPORT : "+nbFingerPrintsToImport);
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

    //GET THE FP FOR AN EMPLOYEE FROM THE SERVER
    private void ImportEmployeesFingerPrints(){
        int nbEmployeesRequested = 50;
        //PARAMS
        JsonObject params = new JsonObject();
        JsonArray IDS = new JsonArray();
        //RANDOMIZE THE LIST
        Collections.shuffle(noFingerPrintsList);
        for(int pos=0;pos<nbEmployeesRequested;pos++){
            Employee employee = noFingerPrintsList.get(pos);
            int empId=employee.getEmployee_id();
            IDS.add(empId);
           Log.e("EMPLOYEE", pos+"-"+empId+" | "+employee.getFull_name());
        }
        params.add("EmployeesId",IDS);
        params.add("Login",login);
        Log.e("PARAMS", params.toString());
        // Using the Retrofit
        ApiInterface apiService =ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getLimitedFingerPrints(params);
        showProgress("Fingerprints Importation starts...", true);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("SERVER_FINGERPRINTS", response.message());
                Log.e("SERVER_FINGERPRINTS", response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //GET FINGERPRINTS
                    JSONArray fingerPrints = jsonObject.getJSONArray("Fingerprints");
                    int nbObj = fingerPrints.length();
                    Log.d("SERVER OBJ", fingerPrints.toString());
                    Log.d("SERVER OBJ COUNT",""+nbObj);
                    //EMPTY THE PRODUCTS TABLE
                    //db.emptyTable("products");
                    int count=0;
                    for (int i = 0; i < fingerPrints.length(); i++) {
                        JSONObject singleEmployee = fingerPrints.getJSONObject(i);
                        //GET FINGER PRINTS
                        int employeeID = singleEmployee.getInt("EmployeId");
                        JSONArray array = singleEmployee.getJSONArray("Fingers");
                        Log.d("FINGER PRINTS", "FINGER PRINTS : " + array.length());

                        for (int j = 0; j < array.length(); j++) {
                            JSONObject data = array.getJSONObject(j);
                            String finger = data.getString("Finger");
                            byte[] tp = helper.base64ToByteArray(data.getString("Template"));
                            db.saveFingerPrintsFromServer(employeeID, finger,null, tp);
                        }
                        count++;
                    }
                    checkListNoFingerPrints();
                    showProgress("", false);
                    if(count>0){
                        showMessage(true, count+" NEW FINGERPRINTS IMPORTED...");
                    }else{
                        showMessage(false, "NO FINGERPRINTS FOUND ON THE SERVER FOR THIS GROUP.RETRY!");
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                    checkListNoFingerPrints();
                    showProgress("", false);
                    showMessage(false, "" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                checkListNoFingerPrints();
                Log.e("response-failure", call.toString());
                showProgress("Authentication",false);
                showMessage(false,t.toString());
                Log.e("response-failure", t.toString());
            }

        });
    }

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(ImportFingerPrints.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(ImportFingerPrints.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(ImportFingerPrints.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }

    // IMPORT THE EMPLOYEES FINGERPRINTS FROM SERVER
    private void importFingerPrintsDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ImportFingerPrints.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ImportFingerPrints.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Import Fingerprints")
                .setMessage("Do you really want to Import FINGERPRINTS from the Server ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //IMPORT
                        ImportEmployeesFingerPrints();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

}