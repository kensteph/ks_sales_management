package com.snack_bar.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.R;
import com.snack_bar.adapter.EmployeeAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.model.FingerPrint;
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

public class EmployeeListActivity extends AppCompatActivity implements EmployeeAdapter.IEmployeeAdapterCallback{
    private List<Employee> employeesList;
    private List<FingerPrint> listDbFingerPrints;
    private TextView tv_nb_employees;
    RecyclerView recyclerView;
    EditText editTextSearch;
    EmployeeAdapter adapter;
    DatabaseHelper db;
    private Helper helper;
    private ProgressDialog dialog;
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    private SharedPreferences sp;
    private String Email;
    private String Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Fingerprints Taking List");

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email", "");
        Password = sp.getString("password", "");

        //LIST OF EMPLOYEES FROM DB
        employeesList = new ArrayList<Employee>();
        helper = new Helper();
        //LIST OF FINGERPRINTS FROM DB
        listDbFingerPrints = new ArrayList<FingerPrint>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        tv_nb_employees = (TextView) findViewById(R.id.tv_nb_employees);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeeAdapter(this, employeesList,EmployeeListActivity.this);
        recyclerView.setAdapter(adapter);

        db = new DatabaseHelper(this);
        //LOAD EMPLOYEES
        //FROM SERVER
        //getEmployees();
        //FROM DB
        new LoadEmployeesFromDB().execute();

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "WELCOME BACK", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.sync_finger_print, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
//            case R.id.sync_finger_server:
//                synchronizeFingerPrints();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        List<Employee> filteredNames = new ArrayList<>();
        //looping through existing elements
        for (Employee employee : employeesList) {
            String employeeInfo = employee.getEmployee_prenom() + " " + employee.getEmployee_nom() + " " + employee.getEmployee_code() + " " + employee.getEmployee_id();
            //if the existing elements contains the search input
            if (employeeInfo.toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filteredNames.add(employee);
            }
        }

        adapter = new EmployeeAdapter(this, filteredNames,EmployeeListActivity.this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //IMPORT SINGLE EMPLOYEE 'S FP IN LOCAL DB
    private void ImportFingerPrints(int employeeID){
        showProgress("Fingerprints Importation starts...", true);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Log.d("CREDENTIALS", Email + " | " + Password);
        Call<JsonObject> call = apiService.getEmployeeFingerPrints(employeeID,Email, Password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("SERVER", response.message());
                Log.e("SERVER", response.body().toString());
                try {
                    JSONObject data = new JSONObject(new Gson().toJson(response.body()));
                    String fullName=data.getString("Nom")+" "+data.getString("Prenom");
                    //GET ALL FINGERPRINTS
                    JSONArray allFingers = data.getJSONArray("Fingers");
                    int nbFingers = allFingers.length();
                    if(nbFingers>0){
                        //IF EMPLOYEE HAS FINGERPRINTS ALREADY
                        db.deleteFingerPrints(employeeID);
                        Log.e("FINGERPRINTS","THERE ARE "+nbFingers+" FINGERPRINTS FOR "+fullName);
                        for(int i=0;i<nbFingers;i++){
                            JSONObject finger = allFingers.getJSONObject(i);
                            String fingerR = finger.getString("Finger");
                            Log.e("FINGERPRINTS","FINGER :  "+fingerR);
                            byte[] fp = helper.base64ToByteArray(finger.getString("FingerPrint"));
                            byte[] tp = helper.base64ToByteArray(finger.getString("Template"));
                            db.saveFingerPrintsFromServer(employeeID, fingerR, fp, tp);
                        }
                        showProgress("Fingerprints Importation starts...", false);
                        showMessage(true, "Fingerprints Importation done.");
                        editTextSearch.setText("");
                    }else{
                        editTextSearch.setText("");
                        showProgress("Fingerprints Importation starts...", false);
                        showMessage(false, "THERE ARE NO FINGERPRINTS FOR "+fullName);
                        Log.e("FINGERPRINTS","THERE ARE NO FINGERPRINTS FOR "+fullName);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("", false);
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                //Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("SERVER", t.getMessage());
            }
        });
    }

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(EmployeeListActivity.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(EmployeeListActivity.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(EmployeeListActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
    //DIALOG SYNCHRONIZE THE SALES FROM SERVER
    private void synchronizeFingerPrints(Employee employee) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(EmployeeListActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(EmployeeListActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Import Fingerprints")
                .setMessage("Do you really want to import the "+employee.getFull_name()+"'s fingerprints ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ImportFingerPrints(employee.getEmployee_id());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }


    @Override
    public void onImportFingerPrints(Employee employee) {
        synchronizeFingerPrints(employee);
    }

    //LOAD EMPLOYEES FROM DB
    public class LoadEmployeesFromDB extends AsyncTask<Void, Void, List<Employee>> {

        @Override
        protected List<Employee> doInBackground(Void... voids) {
            List<Employee> tmpList = db.getAllEmployeesFromDB();
            return tmpList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Employee> employees) {
            super.onPostExecute(employees);
            JSONArray array = new JSONArray();
            for (Employee employee : employees) {
                employeesList.add(employee);
                String json = helper.toJSON(employee);
                //Log.d("SALES JSON",""+json);
            }
            String json = helper.toJSON(employeesList);
            Log.d("SALES JSON ARRAY", "" + json);
            adapter.notifyDataSetChanged();
            int nbEmp = employeesList.size();
            Log.d("EMPLOYEE DATA", "NB : " +nbEmp );
            tv_nb_employees.setText(nbEmp+" Employees");
        }
    }

}