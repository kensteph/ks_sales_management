package com.snack_bar;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.adapter.SaleListAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Item;
import com.snack_bar.model.Order;
import com.snack_bar.model.SaleItemListModel;
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

public class SalesListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<SaleItemListModel> salesList;
    SaleListAdapter saleListAdapter ;
    DatabaseHelper databaseHelper;
    private ProgressDialog dialog;
    public static   Button btnSynchronizeSales;
    private Helper helper;
    //SHARED PREFERENCES
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    SharedPreferences sp;
    private String Email;
    private String Password;
    private int numberOfSalesDetails =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_list);
        recyclerView = findViewById(R.id.rvSalesList);
        btnSynchronizeSales = findViewById(R.id.btnSynchronizeSales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseHelper=new DatabaseHelper(this);
        helper = new Helper();
        salesList = new ArrayList<>();
        saleListAdapter = new SaleListAdapter(salesList,getBaseContext());
        recyclerView.setAdapter(saleListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email","");
        Password = sp.getString("password","");
        Boolean isLogin = sp.getBoolean("isLogin", false);

        initData();
        //GET NUMBER OF LINES IN SALES_DETAILS
        numberOfSalesDetails = databaseHelper.getSalesDetailsCount();

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
        new LoadSalesFromDb().execute();
    }

    public  class LoadSalesFromDb extends AsyncTask<Void, Void, List<SaleItemListModel>> {

        @Override
        protected List<SaleItemListModel> doInBackground(Void... voids) {
            List<SaleItemListModel> ls = null;
            try{
                ls = databaseHelper.getAllSales();
            }catch (IllegalStateException ex){
                Log.d("SALES-LIST", "doInBackground: "+ex);
            }
            return ls;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<SaleItemListModel> saleItemListModels) {
            super.onPostExecute(saleItemListModels);
            //salesList =saleItemListModels;
            for(SaleItemListModel slm : saleItemListModels){
                salesList.add(slm);
            }
            saleListAdapter.notifyDataSetChanged();
            int nbSales =salesList.size();
            if(nbSales>0){
                btnSynchronizeSales.setText(nbSales+" sales to sync");
            }else{
                btnSynchronizeSales.setText("No sale to sync");
                btnSynchronizeSales.setEnabled(false);
            }
            emptySalesTable(false);

        }
    }

    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(SalesListActivity.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SalesListActivity.this, R.color.colorAccent));
        } else
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SalesListActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }

    //POST SALES TO SERVER
    private void synchronizeSalesToServer(List<SaleItemListModel> salesList){
        if(numberOfSalesDetails > 0){
            showProgress("Sales Synchronization start....",true);
            Log.d("SERVER", "SALES COUNT : " + salesList.size());
            Log.d("SERVER", "SALES DETAILS TO SEND COUNT : " + numberOfSalesDetails);
            int position =1;
            for (SaleItemListModel sale : salesList)
            {
                Log.d("SERVER", "SALES ID : " + sale.getSaleId());
                //SALE DETAILS
                List<Order> saleDetails= new ArrayList<>();
                saleDetails=sale.getItem();
                int nbLines = saleDetails.size();
                int posDetails = 1;
                for (Order sd : saleDetails) {
                    prepareJSON(sale.getEmployee(),sale.getSaleId(),sd,sale.getSaleDate(),position,nbLines,posDetails);
                    posDetails++;
                }
                Log.d("SERVER", "SALES PROCESSING ID  : " + sale.getSaleId());
                position++;
            }
        }else{
            showProgress("Sales Synchronization start....",false);
            //EMPTY THE SALES TABLE
            emptySalesTable(true);
        }

    }
    private void prepareJSON(int employeeID,int saleID,Order order,String date,int position,int nbLines,int posDetails){
        JsonObject login = new JsonObject();
        JsonObject obj = new JsonObject();
        login.addProperty ("Email",Email);
        login.addProperty("Password",Password);
        //PRODUCT INFO
        Item item = order.item;
        int productId = item.id;
        obj.addProperty("EmployeId", employeeID);
        obj.addProperty("ProduitId", productId);
        obj.addProperty("Quantite",order.quantity);
        obj.addProperty("Prix", item.unitPrice);
        obj.addProperty("Date", date);
        obj.add("Login",login);
        String data = obj.toString();
        Log.d("SERVER", "JSON : " + data);
        postDataToServer(obj,saleID,productId,position,nbLines,posDetails);

    }
    private void postDataToServer(JsonObject obj,int saleID,int productId,int position,int nbLines,int posDetails){
        // Using the Retrofit
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.postSales (obj);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.e("response-success", response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                   if(jsonObject.has("Accepted")){ //REQUEST SUCCESSFUL
                       Log.d("SYNC SALES", "POS DETAIL/NB LINES: "+posDetails+"/"+nbLines);
                       Log.e("response-success", jsonObject.getString("Accepted"));
                       //REMOVE THIS DETAILS FROM SALES DETAILS
                        databaseHelper.deleteSaleDetails(saleID,productId);
                       //DELETE THIS SALE
                       if(posDetails == nbLines){ //If the last line
                           databaseHelper.deleteSale(saleID);
                       }
                      // emptySalesTable(true);
                   }else{
                       Log.e("response-failed","SALE DETAILS DON'T SAVE");
                       //showMessage(false,"SALE DETAILS DON'T SAVE");
                   }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(position == salesList.size()){
                    showProgress("Sales Synchronization start....",false);
                    //EMPTY THE SALES TABLE
                    emptySalesTable(true);
                    Log.e("DONE","SALE SYNC DONE....");
                    salesList.clear();
                    saleListAdapter.notifyDataSetChanged();
                    btnSynchronizeSales.setText("No sale to sync");
                    btnSynchronizeSales.setEnabled(false);
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showMessage(false,"PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                Log.e("response-failure", call.toString());
                showProgress("Sales Synchronization complete.",false);
            }

        });
    }
    private void emptySalesTable(boolean showMessage) {
        //GET NUMBER OF LINES IN SALES_DETAILS
        numberOfSalesDetails = databaseHelper.getSalesDetailsCount();
        Log.d("SERVER", "SALES DETAILS TO SEND COUNT : " + numberOfSalesDetails);
        if(numberOfSalesDetails ==0) {
            //EMPTY THE SALES TABLE
            databaseHelper.emptyTable("sales");
            salesList.clear();
            saleListAdapter.notifyDataSetChanged();
            btnSynchronizeSales.setText("No sale to sync");
            if(showMessage){
                showMessage(true,"Synchronization complete...");
            }
            btnSynchronizeSales.setEnabled(false);
        }
    }

    //DIALOG SYNCHRONIZE THE SALES FROM SERVER
    private void synchronizeSales() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(SalesListActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(SalesListActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Sync Sales")
                .setMessage("Do you really want to SYNC THOSE SALES ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        synchronizeSalesToServer(salesList);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing
                    }
                })
                .show();
    }

}