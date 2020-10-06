package com.snack_bar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.adapter.SaleListAdapter;
import com.snack_bar.database.DatabaseHelper;
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

public class SalesListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<SaleItemListModel> salesList;
    SaleListAdapter saleListAdapter ;
    DatabaseHelper databaseHelper;
    private ProgressDialog dialog;
    private Button btnSynchronizeSales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_list);
        recyclerView = findViewById(R.id.rvSalesList);
        btnSynchronizeSales = findViewById(R.id.btnSynchronizeSales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseHelper=new DatabaseHelper(this);
        salesList = new ArrayList<>();
        saleListAdapter = new SaleListAdapter(salesList,getBaseContext());
        recyclerView.setAdapter(saleListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initData();
        btnSynchronizeSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveSalesToServer();
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
                Log.d("SALES",""+slm.isExpandable());
            }
            saleListAdapter.notifyDataSetChanged();
            int nbSales =salesList.size();
            if(nbSales>0){
                btnSynchronizeSales.setText("Synchroniser les "+nbSales+" ventes");
            }else{
                btnSynchronizeSales.setText("Aucune vente à synchroniser");
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
    //UPLOAD SALES
    private void postSale(String data) {
        showProgress("Synchronisation des ventes.....",true);
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
                    for (int i = 0; i < list.length(); i++) {
                        int saleId = list.getInt(i);
                        databaseHelper.deleteSale(saleId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(nbSuccess==0) {
                    Toast.makeText(getApplicationContext()," Aucune vente n'a été enregistrée...", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), nbSuccess+" Ventes ont été enregistrées avec succès...", Toast.LENGTH_LONG).show();
                    salesList.clear();
                    saleListAdapter.notifyDataSetChanged();
                    btnSynchronizeSales.setText("Aucune vente à synchroniser");
                }

                Log.d("SERVER",jsonObject.toString());
                showProgress("Synchronisation des ventes terminée.....",false);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("Synchronisation des ventes terminée.....",false);
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
                Log.d("SERVER",t.toString());
            }

        });
    }

    private void SaveSalesToServer(){
        JSONArray array = new JSONArray();
        for (SaleItemListModel sale : salesList)
        {
            JSONObject obj = new JSONObject();
            try {
                obj.put("SaleId", sale.getSaleId());
                obj.put("SaleDate", sale.getSaleDate());
                obj.put("EmployeeId", sale.getEmployee());
                obj.put("SellerId", sale.getCashier());
                obj.put("Total", sale.getTotal());
                obj.put("MaterialId", sale.getMaterialId());
                //DETAILS
                List<Order> listItems=sale.getItem();
                JSONObject Orderdetails = new JSONObject();
                JSONArray arrayDetails = new JSONArray();
                for(Order line : listItems) {
                    double unitPrice=line.item.unitPrice;
                    int productId=line.item.id;
                    Orderdetails.put("VenteId",sale.getSaleId());
                    Orderdetails.put("MaterielId",sale.getMaterialId());
                    Orderdetails.put("EmployeId",sale.getEmployee());
                    Orderdetails.put("VendeurId",sale.getCashier());
                    Orderdetails.put("ProduitId",productId);
                    Orderdetails.put("Quantite",line.quantity);
                    Orderdetails.put("PrixUnitaire",unitPrice);
                    Orderdetails.put("DateVente",sale.getSaleDate());
                    arrayDetails.put(Orderdetails);
                }
                obj.put("Details",arrayDetails);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        String data = array.toString();
        Log.d("SERVER","JSON : "+data);
        postSale(data);
    }
}