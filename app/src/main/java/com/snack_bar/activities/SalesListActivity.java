package com.snack_bar.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.snack_bar.R;
import com.snack_bar.adapter.SaleListAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.SaleItemListModel;
import com.snack_bar.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class SalesListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<SaleItemListModel> salesList;
    SaleListAdapter saleListAdapter ;
    DatabaseHelper databaseHelper;
    private ProgressDialog dialog;
    private TextView tv_nb_sales;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseHelper=new DatabaseHelper(this);
        helper = new Helper();
        salesList = new ArrayList<>();
        saleListAdapter = new SaleListAdapter(salesList,getBaseContext());
        recyclerView.setAdapter(saleListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tv_nb_sales = (TextView) findViewById(R.id.tv_nb_sales);
        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email","");
        Password = sp.getString("password","");
        Boolean isLogin = sp.getBoolean("isLogin", false);
        initData();
        //GET NUMBER OF LINES IN SALES_DETAILS
        numberOfSalesDetails = databaseHelper.getSalesDetailsCount();
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
            tv_nb_sales.setText(nbSales+" Sales");
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

}