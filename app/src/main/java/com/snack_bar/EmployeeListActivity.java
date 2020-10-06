package com.snack_bar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.adapter.EmployeeAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.model.FingerPrint;
import com.snack_bar.model.Order;
import com.snack_bar.model.SaleItemListModel;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;

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
    private ProgressDialog dialog;
    private Button btnSynchronizeSales;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        //LIST OF EMPLOYEES FROM DB
        employeesList = new ArrayList<Employee>();
        //LIST OF FINGERPRINTS FROM DB
        listDbFingerPrints = new ArrayList<FingerPrint>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeeAdapter(this,employeesList);
        recyclerView.setAdapter(adapter);

        db=new DatabaseHelper(this);
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
            for(Employee employee : employees){
                employeesList.add(employee);
            }
            adapter.notifyDataSetChanged();
            Log.d("EMPLOYEE DATA","NB : "+employeesList.size());
        }
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
    //UPLOAD FINGER PRINTS TO SERVER
    //LOAD ALL THE FINGERPRINTS FROM DB
    public class LoadFingerPrintsFromDB extends AsyncTask<String,String,List<FingerPrint>> {

        @Override
        protected List<FingerPrint> doInBackground(String... strings) {
            // Temporary list
            List<FingerPrint> tempFingerPrints = new ArrayList<>();
            tempFingerPrints =db.getAllFingersPrintsFromDB();
            return tempFingerPrints;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress("Chargement des empreintes...",true);
        }

        @Override
        protected void onPostExecute(List<FingerPrint> fingerPrints) {
            super.onPostExecute(fingerPrints);
            listDbFingerPrints = fingerPrints;
            Log.d("FINGERPRINT2","FOUND : "+listDbFingerPrints.size());
            showProgress("Chargement des empreintes...",false);
        }
    }
    private void postFingerPrints(String data) {
        showProgress("Synchronisation des empreintes.....",true);
        List<Integer> salesSucceedID;
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.UploadSaleToServer(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                int nbSuccess=0;
                //Get the response
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    JSONObject Response  = jsonObject.getJSONObject("response");
                    nbSuccess = Response.getInt("TotalSuccess");
                    JSONArray list = Response.getJSONArray("SuccessId");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(nbSuccess==0) {
                    Toast.makeText(getApplicationContext()," Aucune empreinte n'a été synchronisée...", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), nbSuccess+" empreintes ont été synchronisées avec succès...", Toast.LENGTH_LONG).show();
                }

                Log.d("SERVER",jsonObject.toString());
                showProgress("Synchronisation des empreintes terminée.....",false);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("Synchronisation des ventes terminée.....",false);
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
                Log.d("SERVER",t.toString());
            }

        });
    }
    private void SaveFingerPrintsToServer(){
        JSONArray array = new JSONArray();
        for (FingerPrint fingerPrint : listDbFingerPrints)
        {
            JSONObject obj = new JSONObject();
            try {
                //employe_id	doigt	empreinte template
                obj.put("EmployeeId", fingerPrint.getEmployeeId());
                obj.put("Finger", "");
                obj.put("FingerPrint", fingerPrint.getFingerPrintByteArray());
                obj.put("FingerPrintTemplate", fingerPrint.getFingerPrintTemplate());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        String data = array.toString();
        Log.d("SERVER","JSON : "+data);
        postFingerPrints(data);
    }
}