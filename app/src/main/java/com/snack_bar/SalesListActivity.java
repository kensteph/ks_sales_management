package com.snack_bar;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
    private Button btnSynchronizeSales;
    private Helper helper;
    //SHARED PREFERENCES
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    SharedPreferences sp;
    private String Email;
    private String Password;


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

        btnSynchronizeSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synchronizeSalesToServer(salesList);
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
            return databaseHelper.getAllSales();
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
            }

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
    //UPLOAD SALES
//    private void postSale(JsonObject data) {
//        showProgress("Synchronisation des ventes.....",true);
//        List<Integer> salesSucceedID;
//       ApiInterface apiService =
//                ApiClient.getClient().create(ApiInterface.class);
//        Call<JsonObject> call = apiService.postSales(data);
//        call.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                int nbSuccess=0;
//                //Get the response
//                JSONObject jsonObject = null;
//                try {
//                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
//                    JSONObject Response  = jsonObject.getJSONObject("response");
//                    nbSuccess = Response.getInt("TotalSuccess");
//                    JSONArray list = Response.getJSONArray("SuccessId");
//                    for (int i = 0; i < list.length(); i++) {
//                        int saleId = list.getInt(i);
//                        databaseHelper.deleteSale(saleId);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                if(nbSuccess==0) {
//                    Toast.makeText(getApplicationContext()," Aucune vente n'a été enregistrée...", Toast.LENGTH_LONG).show();
//                }else{
//                    Toast.makeText(getApplicationContext(), nbSuccess+" Ventes ont été enregistrées avec succès...", Toast.LENGTH_LONG).show();
//                    salesList.clear();
//                    saleListAdapter.notifyDataSetChanged();
//                    btnSynchronizeSales.setText("Aucune vente à synchroniser");
//                }
//
//                Log.d("SERVER",jsonObject.toString());
//                showProgress("Synchronisation des ventes terminée.....",false);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                showProgress("Synchronisation des ventes terminée.....",false);
//                Toast.makeText(getApplicationContext(), t.toString()+" | "+call.toString(), Toast.LENGTH_LONG).show();
//                Log.d("SERVER",t.toString());
//            }
//
//        });
//    }
//
//    private void SaveSalesToServer(){
//        JSONArray array = new JSONArray();
//        for (SaleItemListModel sale : salesList)
//        {
//            JSONObject obj = new JSONObject();
//            try {
//                int saleID = sale.getSaleId();
//                obj.put("SaleId", saleID);
//                obj.put("SaleDate", sale.getSaleDate());
//                obj.put("EmployeeId", sale.getEmployee());
//                obj.put("SellerId", sale.getCashier());
//                obj.put("Total", sale.getTotal());
//                obj.put("MaterialId", sale.getMaterialId());
//                //DETAILS
//                List<Order> listItems=sale.getItem();
//                JSONArray arrayDetails = new JSONArray();
//
//                for(Order line : listItems) {
//                    JSONObject Orderdetails = new JSONObject();
//                    double unitPrice=line.item.unitPrice;
//                    int productId=line.item.id;
//                    //Log.d("SERVER","SALE ID : "+sale.getSaleId()+" PRODUCT ID : "+productId);
//                    Orderdetails.put("VenteId",sale.getSaleId());
//                    Orderdetails.put("MaterielId",sale.getMaterialId());
//                    Orderdetails.put("EmployeId",sale.getEmployee());
//                    Orderdetails.put("VendeurId",sale.getCashier());
//                    Orderdetails.put("ProduitId",productId);
//                    Orderdetails.put("Quantite",line.quantity);
//                    Orderdetails.put("PrixUnitaire",unitPrice);
//                    Orderdetails.put("DateVente",sale.getSaleDate());
//                    arrayDetails.put(Orderdetails);
//                }
//                Log.d("SERVER","SALE ID : "+saleID +" | "+arrayDetails);
//                obj.put("Details",arrayDetails);
//                array.put(obj);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//        String data = array.toString();
//        Log.d("SERVER","JSON : "+data);
//        postSale(data);
//    }
    //POST SALES TO SERVER

    private void synchronizeSalesToServer(List<SaleItemListModel> salesList){
        Log.d("SERVER", "SALES COUNT : " + salesList.size());
        for (SaleItemListModel sale : salesList)
        {
                //SALE DETAILS
            List<Order> saleDetails= new ArrayList<>();
            saleDetails=sale.getItem();
            for (Order sd : saleDetails) {
                prepareJSON(sale.getEmployee(),sd,sale.getSaleDate());
            }
        }
    }
    private void prepareJSON(int employeeID,Order order,String date){
        JsonObject login = new JsonObject();
        JsonObject obj = new JsonObject();
        login.addProperty ("Email",Email);
        login.addProperty("Password",Password);
        //PRODUCT INFO
        Item item = order.item;
        obj.addProperty("EmployeId", employeeID);
        obj.addProperty("ProduitId", item.id);
        obj.addProperty("Quantite",order.quantity);
        obj.addProperty("Prix", item.unitPrice);
        obj.addProperty("Date", date);
        obj.add("Login",login);
        String data = obj.toString();
        Log.d("SERVER", "JSON : " + data);
        postDataToServer(obj);

    }
    private void postDataToServer(JsonObject obj){
        // Using the Retrofit
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.postSales (obj);
        showProgress("Sales Synchronization start....",true);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try{
//                      Log.e("response-success", response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                   if(jsonObject.has("Accepted")){ //REQUEST SUCCESSFUL
                       Log.e("response-success", jsonObject.getString("Accepted"));
                   }else{
                       Log.e("response-failed","SALE DETAILS DON'T SAVE");
                       showMessage(false,"SALE DETAILS DON'T SAVE");
                   }


//                    nbSuccess = Response.getInt("TotalSuccess");
//                    JSONArray list = Response.getJSONArray("SuccessId");
//                    for (int i = 0; i < list.length(); i++) {
//                        int saleId = list.getInt(i);
//                        databaseHelper.deleteSale(saleId);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                        showProgress("Synchronisation des empreintes terminée.",false);
//                        nbFp.setText("Synchronisation des empreintes terminée");
//                        btnSynchronizeFingerPrints.setVisibility(View.INVISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showMessage(false,"PLEASE VERIFY YOUR NETWORK CONNECTION...");
                Log.e("response-failure", call.toString());
                showProgress("Sales Synchronization complete.",false);
                //nbFp.setText("Une erreur est survenue.Reessayez");
            }

        });
    }
}