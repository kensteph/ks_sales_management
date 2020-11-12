package com.snack_bar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

public class EmployeeListActivity extends AppCompatActivity {
    private List<Employee> employeesList;
    private List<FingerPrint> listDbFingerPrints;
    RecyclerView recyclerView;
    EditText editTextSearch;
    EmployeeAdapter adapter;
    DatabaseHelper db ;
    private Helper helper;
    private ProgressDialog dialog;
    private Button btnSynchronizeSales;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //LIST OF EMPLOYEES FROM DB
        employeesList = new ArrayList<Employee>();
        helper = new Helper();
        //LIST OF FINGERPRINTS FROM DB
        listDbFingerPrints = new ArrayList<FingerPrint>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeeAdapter(this,employeesList);
        recyclerView.setAdapter(adapter);

        db=new DatabaseHelper(this);
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
            String employeeInfo = employee.getEmployee_prenom()+" "+employee.getEmployee_nom()+" "+employee.getEmployee_code()+" "+employee.getEmployee_id();
            //if the existing elements contains the search input
            if (employeeInfo.toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filteredNames.add(employee);
            }
        }

        adapter = new EmployeeAdapter(this,filteredNames);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(EmployeeListActivity.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(EmployeeListActivity.this, R.color.colorAccent));
        } else
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(EmployeeListActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }

    //LOAD EMPLOYEE WITHOUT FINGERPRINTS FROM SERVER
    private void getEmployees(){
        showProgress("Récupération des données du serveur....",false);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getAllEmployees("WFP");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.d("SERVER",response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //GET ALL EMPLOYEES
                    JSONArray array = jsonObject.getJSONArray("Employees");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject data = array.getJSONObject(i);
                        Employee employee = new Employee(data.getInt("id"),data.getInt("entreprise_id"),data.getString("employe_code"),data.getString("employe_prenom"),data.getString("employe_nom"));
                        //db.saveEmployees(employee);
                        employeesList.add(employee);
                        Log.d("SERVER 1",data.getString("employe_prenom"));
                    }
                    adapter.notifyDataSetChanged();
                    showProgress("",false);
                    showMessage(true, "Récupération terminée !!!");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("",false);
                showMessage(false, t.getMessage());
                //Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("SERVER",t.getMessage());
            }
        });
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
            for(Employee employee : employees){
                employeesList.add(employee);
                String json = helper.toJSON(employee);
                //Log.d("SALES JSON",""+json);
            }
            String json = helper.toJSON(employeesList);
            Log.d("SALES JSON ARRAY",""+json);
            adapter.notifyDataSetChanged();
            Log.d("EMPLOYEE DATA","NB : "+employeesList.size());
        }
    }

}