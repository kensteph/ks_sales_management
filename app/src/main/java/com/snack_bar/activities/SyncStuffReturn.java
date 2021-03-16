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
import com.google.gson.JsonObject;
import com.snack_bar.R;
import com.snack_bar.adapter.StuffReturnAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.StuffReturnModel;
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
                synchronizeSales();
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

    //POST SALES TO SERVER
    private void synchronizeSalesToServer(List<StuffReturnModel> stuffReturnList) {
            showProgress("Stuff return Synchronization start....", true);
            Log.d("SERVER", "SALES COUNT : " + stuffReturnList.size());
            Log.d("SERVER", "SALES DETAILS TO SEND COUNT : " + numberOfSalesDetails);
            int position = 1;
            for (StuffReturnModel stuff : stuffReturnList) {
                Log.d("STUFF SYNC", "STUFF RETURN ID : " + stuff.getReturnId());
                //STUFF RETURN DETAILS
                int nbLines = stuffReturnList.size();
                prepareJSON(stuff, position, position, nbLines);
                Log.d("SERVER", "STUFF RETURN PROCESSING ID  : " + stuff.getReturnId());
                position++;
            }

    }

    private void prepareJSON(StuffReturnModel stuffReturn, int position, int nbLines, int posDetails) {
        JsonObject login = new JsonObject();
        JsonObject obj = new JsonObject();
        login.addProperty("Email", Email);
        login.addProperty("Password", Password);

        //RETURN DATA INFO
        int returnId = stuffReturn.getReturnId();
        String plate = stuffReturn.getPlateReturn();
        if (Integer.parseInt(plate.trim()) != 0) {
            plate = "True";
        }else{
            plate = "False";
        }
        String spoon = stuffReturn.getSpoonReturn();
        if (Integer.parseInt(spoon.trim()) != 0) {
            spoon = "True";
        }else{
            spoon = "False";
        }
        String bottle = stuffReturn.getBottleReturn();
        if (Integer.parseInt(bottle.trim()) != 0) {
            bottle = "True";
        }else{
            bottle = "False";
        }
        obj.addProperty("EmployeId", stuffReturn.getEmployeeId());
        obj.addProperty("Plate", plate);
        obj.addProperty("Spoon", spoon);
        obj.addProperty("Bottle", bottle);
        obj.addProperty("Date", stuffReturn.getDateReturn());
        obj.add("Login", login);

        String data = obj.toString();
        Log.d("SERVER", "JSON : " + data);
        postDataToServer(obj, returnId, position, nbLines);

    }

    private void postDataToServer(JsonObject obj, int returnId, int position, int nbLines) {
        // Using the Retrofit
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.postStuffReturn(obj);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
               // Log.e("SYNC STUFF","SERVER RESPONSE : "+ response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    if (jsonObject.has("Accepted")) { //REQUEST SUCCESSFUL
                        Log.e("response-success", jsonObject.getString("Accepted"));
                        //REMOVE THIS DETAILS FROM SALES DETAILS
                        databaseHelper.deleteStuffReturn(returnId);
                    } else {
                        Log.e("response-failed", "SALE DETAILS DON'T SAVE"+response.code());
                        //showMessage(false,"SALE DETAILS DON'T SAVE");
                    }
                    if (position == stuffReturnList.size()) {
                        showProgress("Stuff Return Synchronization start....", false);
                        //EMPTY THE SALES TABLE
                        emptySalesTable(true);
                        Log.e("DONE", "Stuff Return SYNC DONE....");
                        stuffReturnList.clear();
                        stuffReturnAdapter.notifyDataSetChanged();
                        btnSynchronizeSales.setText("No more stuff to sync");
                        tv_summary_report.setText("");
                        btnSynchronizeSales.setEnabled(false);
                        showMessage(true, "Synchronization complete...");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    stuffReturnAdapter.notifyDataSetChanged();
                    Log.e("response-failed", "STUFF RETURN DETAILS DON'T SAVE" + e.getMessage());
                    showProgress("Stuff Return Synchronization start....", false);
                    showMessage(false, "STUFF RETURN ID " + returnId + " CANNOT BE SYNCED");
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

    private void emptySalesTable(boolean showMessage) {
        //GET NUMBER OF LINES IN SALES_DETAILS
        numberOfSalesDetails = stuffReturnList.size();
        Log.d("SERVER", "SALES DETAILS TO SEND COUNT : " + numberOfSalesDetails);
        if (numberOfSalesDetails == 0) {
            //EMPTY THE SALES TABLE
            databaseHelper.emptyTable("stuff_return");
            stuffReturnList.clear();
            recyclerView.setAdapter(stuffReturnAdapter);
            stuffReturnAdapter.notifyDataSetChanged();
            btnSynchronizeSales.setText("No more stuff to sync");
            tv_summary_report.setText("");
            if (showMessage) {
                showMessage(true, "Synchronization complete...");
            }
            btnSynchronizeSales.setEnabled(false);
        }
    }

    //DIALOG SYNCHRONIZE THE SALES FROM SERVER
    private void synchronizeSales() {
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
                        synchronizeSalesToServer(stuffReturnList);
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