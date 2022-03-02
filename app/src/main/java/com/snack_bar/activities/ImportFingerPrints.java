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
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.snack_bar.R;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportFingerPrints extends AppCompatActivity {
    private DatabaseHelper db;
    private int nbFingerPrintsToImport = 0;
    private int nbEmployeesWithFP = 0;
    private TextView nbFp;
    private ProgressDialog dialog;
    private Button btnImportFingerPrints;
    private Button btnRetry;
    private List<Employee> noFingerPrintsList;
    private List<Employee> withFingerPrintsList;
    private JSONArray employeeIdsToSync;
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

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email", "");
        Password = sp.getString("password", "");

        login = new JsonObject();
        login.addProperty ("Email",Email);
        login.addProperty("Password",Password);
        nbFp = (TextView) findViewById(R.id.nbFpToImport);
        btnImportFingerPrints = findViewById(R.id.btnImportFingerPrints);
        btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setVisibility(View.INVISIBLE);

        //DB
        helper = new Helper();
        db = new DatabaseHelper(this);

        //CHECK THE EMPLOYEE'S LIST WITH NO FINGERPRINTS
        checkListNoFingerPrints();

        btnImportFingerPrints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importFingerPrintsDialog();
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmployeesIdWithFpMissingInLocal();
            }
        });
    }

    private void checkListNoFingerPrints() {
        noFingerPrintsList = new ArrayList<Employee>(); // EMPLOYEES NOT YET HAVE FINGERPRINTS
        withFingerPrintsList = new ArrayList<Employee>(); // EMPLOYEES WITH FINGERPRINTS
        noFingerPrintsList = db.getEmployeesWithNoFingerPrintsFromDB();
        withFingerPrintsList = db.getEmployeesWithFingerPrintsFromDB();
        nbEmployeesWithFP = withFingerPrintsList.size();
        int total_employee = db.getEmployeeCount();
        nbFingerPrintsToImport =  total_employee-nbEmployeesWithFP;
        if (nbFingerPrintsToImport == 0) {
            btnImportFingerPrints.setVisibility(View.INVISIBLE);
        }
        //GET THE MISSING EMPLOYEE LIST WITH FP FROM SERVER
        getEmployeesIdWithFpMissingInLocal();

        nbFp.setText(nbFingerPrintsToImport + " Fingerprints to import");
        Log.e("FINGERPRINTS","EMPLOYEES WITH FP : "+nbEmployeesWithFP);
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
//GET EMPLOYEE'S ID WITH FINGERPRINT FROM THE SERVER NOT IN LOCAL DB
    private void getEmployeesIdWithFpMissingInLocal() {
        showProgress("Please wait...", true);
        btnImportFingerPrints.setVisibility(View.INVISIBLE);
        //PARAMS
        JsonObject params = new JsonObject();
        JsonArray IDS = new JsonArray();

        for(int pos=0;pos<nbEmployeesWithFP;pos++){
            Employee employee = withFingerPrintsList.get(pos);
            int empId=employee.getEmployee_id();
            IDS.add(empId);
            //Log.e("EMPLOYEE", pos+"-"+empId+" | "+employee.getFull_name());
        }
        if(nbEmployeesWithFP==0) {
            IDS.add(0);
        }
        params.add("EmployeesId",IDS);
        params.add("Login",login);
        Log.e("PARAMS_ID_SEND", params.toString());


        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Log.d("CREDENTIALS", Email + " | " + Password);
        Call<JsonObject> call = apiService.getEmployeeIdMissingFpInLocalDB(params);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("SERVER EMP", response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //GET ALL EMPLOYEES
                    JSONArray AllEmployees = jsonObject.getJSONArray("EmployeeIds");
                    employeeIdsToSync = AllEmployees;
                    int nbEmployee = AllEmployees.length();
                    Log.e("FP_TOTAL_FOUND", ""+nbEmployee);
                    Log.e("FP_LIST", AllEmployees.toString());
                    showProgress("", false);
                    Toast.makeText(getApplicationContext(), "Ready to Sync!!..", Toast.LENGTH_LONG).show();
                    btnImportFingerPrints.setVisibility(View.VISIBLE);
                    btnRetry.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress("", false);
                    showMessage(false, "" + e.toString());
                }
                catch (OutOfMemoryError om){
                    showMessage(false, "" + om.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("", false);
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                btnRetry.setVisibility(View.VISIBLE);
                btnImportFingerPrints.setVisibility(View.INVISIBLE);
                Log.e("SERVER", t.getMessage());
            }
        });
    }
    //GET THE FP FOR AN EMPLOYEE FROM THE SERVER
    private void ImportEmployeesFingerPrints(){
        Log.e("SYNC","============================================================");
        Log.e("ID_TO_SYNC", employeeIdsToSync.toString());
        int nbEmployeesRequested = 50;
        //PARAMS
        JsonObject params = new JsonObject();
        JsonArray IDS = new JsonArray();

        for (int i = 0; i < employeeIdsToSync.length(); i++) {
            int pos=i+1;
            try {
                int employeeID= (int) employeeIdsToSync.get(i);
                IDS.add(employeeID);
                //Log.e(pos+"-EMP-ID",""+employeeID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        params.add("EmployeesId",IDS);
        params.add("Login",login);
        Log.e("PARAMS_SENT_SERV", params.toString());
        // Using the Retrofit
        ApiInterface apiService =ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getLimitedFingerPrints(params);
        showProgress("Fingerprints Importation starts...", true);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.e("SERVER_FINGERPRINTS", response.message());
                //Log.e("SERVER_FINGERPRINTS", response.body().toString());
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

                    getEmployeesIdWithFpMissingInLocal();

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