package com.snack_bar.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.snack_bar.R;
import com.snack_bar.adapter.StuffReturnAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.StuffReturnModel;
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

public class SyncStuffReturn extends AppCompatActivity {
    RecyclerView recyclerView;
    List<StuffReturnModel> stuffReturnList;
    StuffReturnAdapter stuffReturnAdapter;
    DatabaseHelper databaseHelper;
    private ProgressDialog dialog;
    public static Button btnSynchronizeSales;
    private TextView tv_summary_report;
    private Helper helper;
    //SHARED PREFERENCES
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    SharedPreferences sp;
    private String Email;
    private String Password;
    private int numberOfSalesDetails = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_stuff_return);

        recyclerView = findViewById(R.id.rvSalesList);
        btnSynchronizeSales = findViewById(R.id.btnSynchronizeSales);
        tv_summary_report = (TextView) findViewById(R.id.tv_summary_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseHelper = new DatabaseHelper(this);
        helper = new Helper();
        helper.getCurrentDate();
        stuffReturnList = new ArrayList<>();
        stuffReturnAdapter = new StuffReturnAdapter(stuffReturnList, getBaseContext());
        recyclerView.setAdapter(stuffReturnAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email", "");
        Password = sp.getString("password", "");
        Boolean isLogin = sp.getBoolean("isLogin", false);

        initData();
        btnSynchronizeSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synchronizeStuffs();
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

    private void initData() {
        new LoadStuffReturn().execute();
    }

    public class LoadStuffReturn extends AsyncTask<Void, Void, List<StuffReturnModel>> {

        @Override
        protected List<StuffReturnModel> doInBackground(Void... voids) {
            List<StuffReturnModel> ls = null;
            try {
                //GET ALL RETURNS
                ls = databaseHelper.getStuffReturn();
            } catch (IllegalStateException ex) {
                Log.d("SALES-LIST", "doInBackground: " + ex);
            }
            return ls;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<StuffReturnModel> stuffList) {
            super.onPostExecute(stuffList);
            for (StuffReturnModel stuff : stuffList) {
                stuffReturnList.add(stuff);
            }
            stuffReturnAdapter.notifyDataSetChanged();
            int nbSales = stuffReturnList.size();
            if (nbSales > 0) {
                btnSynchronizeSales.setText("Tap to sync " + nbSales + " returns");
            } else {
                btnSynchronizeSales.setEnabled(false);
            }

        }
    }

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(SyncStuffReturn.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SyncStuffReturn.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SyncStuffReturn.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
    private void synchronizeStuffsToServer() {
        showProgress("Stuff Return Synchronization start....", true);
        JsonArray arrayStuffs=new JsonArray();
        for(int i=0;i<stuffReturnList.size();i++){
            //STUFF info
            StuffReturnModel stuffReturn = stuffReturnList.get(i);
            JsonObject stuffs = new JsonObject();
            stuffs.addProperty("EmployeId", stuffReturn.getEmployeeId());
            stuffs.addProperty("ProduitRetourId", stuffReturn.getStuffReturnId());
            stuffs.addProperty("Quantite", stuffReturn.getStuffQty());
            stuffs.addProperty("Date", stuffReturn.getDateReturn());
            //ADD SINGLE OBJECT IN ARRAY OBJECT
            arrayStuffs.add(stuffs);
        }

        //LOGIN OBJECT
        JsonObject login = new JsonObject();
        login.addProperty("Email", Email);
        login.addProperty("Password", Password);

        //FINAL OBJECT
        JsonObject obj = new JsonObject();
        obj.add("SaleProductReturnDataList", arrayStuffs);
        obj.add("Login", login);

        String data = obj.toString();
        Log.e("SERVER", "JSON : " + data);
        serverSync(obj);
    }
    private void serverSync(JsonObject obj) {
        // Using the Retrofit
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.postStuffReturn(obj);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.e("SYNC STUFF","SERVER RESPONSE : "+ response.body().toString());
                JsonObject jsonObject = null;
                //jsonObject = new JsonObject(new Gson().toJson(response.body()));
                jsonObject = response.body();
                //if (jsonObject.has("Accepted")) { //REQUEST SUCCESSFUL
                if(response.isSuccessful()){
                    Log.e("response-success", jsonObject.toString());
                    emptyStuffReturnTable(true,"Synchronization with Server Done...");
                } else {
                    showProgress("Stuff Return Synchronization start....", false);
                    Log.e("response-failed", "STUFFS DON'T SYNC"+response.toString());
                    emptyStuffReturnTable(false,"No sales match these articles...");
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                Log.e("response-failure", call.toString());
                showProgress("Stuff Return Synchronization complete.", false);
            }

        });
    }
    private void emptyStuffReturnTable(boolean result,String msg) {
        //EMPTY THE TABLE STUFFS RETURN IN LOCAL DB
        databaseHelper.emptyTable("stuff_return");
        stuffReturnList.clear();
        stuffReturnAdapter.notifyDataSetChanged();
        btnSynchronizeSales.setText("No more stuff to sync");
        tv_summary_report.setText("");
        btnSynchronizeSales.setEnabled(false);
        showProgress("Stuff Return Synchronization start....", false);
        showMessage(result, msg);
    }
    //DIALOG SYNCHRONIZE THE SALES FROM SERVER
    private void synchronizeStuffs() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(SyncStuffReturn.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(SyncStuffReturn.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Sync Stuff Return")
                .setMessage("Do you really want to SYNC THOSE Returns ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        synchronizeStuffsToServer();
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