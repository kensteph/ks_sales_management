package com.snack_bar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.FingerPrintTemp;
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

public class SyncFingerPrintToServer extends AppCompatActivity {
private DatabaseHelper db ;
private int nbFingerPrintsToSync=0;
private TextView nbFp;
private ProgressDialog dialog;
private Button btnSynchronizeFingerPrints;
private List<FingerPrintTemp> temporaryFingerPrints;
private Helper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_finger_print_to_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        helper = new Helper();
        db = new DatabaseHelper(this);
        nbFingerPrintsToSync = db.getFingerCount();
        temporaryFingerPrints = new ArrayList<>();
        temporaryFingerPrints = db.getTemporaryFingers();
        nbFp = (TextView) findViewById(R.id.nbFpSync);
        nbFp.setText(nbFingerPrintsToSync+" Empreintes à synchroniser");
        btnSynchronizeFingerPrints = findViewById(R.id.btnSynchronizeFingerPrints);
        if(nbFingerPrintsToSync==0){
            btnSynchronizeFingerPrints.setEnabled(false);
        }
        btnSynchronizeFingerPrints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFingerPrintsToServer(temporaryFingerPrints);
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

    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(SyncFingerPrintToServer.this);
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

    //UPLOAD FINGERPRINTS TO SERVER
    private void postFingerPrints(String data) {
        showProgress("Synchronisation des ventes.....",true);
        List<Integer> salesSucceedID;
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.UploadFingerPrintsToServer(data);
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
//                    JSONArray list = Response.getJSONArray("SuccessId");
//                    for (int i = 0; i < list.length(); i++) {
//                        int saleId = list.getInt(i);
//                        databaseHelper.deleteSale(saleId);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(nbSuccess==0) {
                    Toast.makeText(getApplicationContext()," Aucune vente n'a été enregistrée...", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), nbSuccess+" Ventes ont été enregistrées avec succès...", Toast.LENGTH_LONG).show();
                    temporaryFingerPrints.clear();
                    btnSynchronizeFingerPrints.setText("Aucune vente à synchroniser");
                }

                Log.d("SERVER",jsonObject.toString());
                showProgress("Synchronisation des ventes terminée.....",false);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("Synchronisation des ventes terminée.....",false);
                Toast.makeText(getApplicationContext(), t.toString()+" | "+call.toString(), Toast.LENGTH_LONG).show();
                Log.d("SERVER",t.toString());
            }

        });
    }

    //PREPARE DATA TO SENT
    private void saveFingerPrintsToServer(List<FingerPrintTemp> listFingerPrints){
        JSONArray array = new JSONArray();
        for (FingerPrintTemp fpT : listFingerPrints)
        {
            JSONObject obj = new JSONObject();
            try {
                obj.put("EmployeeId", fpT.getEmployeeId());
                obj.put("Finger","");
                obj.put("FingerPrint", fpT.getFingerPrintImageBase64());
                obj.put("FingerPrintTemplate", fpT.getFingerPrintTemplateBase64());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        String data = array.toString();
        Log.d("SERVERDATA","JSON : "+data);
        postFingerPrints(data);
    }

}