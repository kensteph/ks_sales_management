package com.snack_bar;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
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
//SHARED PREFERENCES
private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
SharedPreferences sp;
private String Email;
private String Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_finger_print_to_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        helper = new Helper();
        db = new DatabaseHelper(this);

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email","");
        Password = sp.getString("password","");

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
        Call<JsonObject> call = apiService.UploadFingerPrintsToServer (data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("SERVER REP",response.body().toString());
                int nbSuccess=0;
                //Get the response
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //Log.d(" responce => ", jsonObject.getJSONObject("body").toString());
//                    JSONObject Response  = jsonObject.getJSONObject("response");
//                    nbSuccess = Response.getInt("TotalSuccess");
//                    JSONArray list = Response.getJSONArray("SuccessId");
//                    for (int i = 0; i < list.length(); i++) {
//                        int saleId = list.getInt(i);
//                        databaseHelper.deleteSale(saleId);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(" responce => ", e.toString());
                }
                if(nbSuccess==0) {
                    Toast.makeText(getApplicationContext()," Aucune vente n'a été enregistrée...", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), nbSuccess+" Ventes ont été enregistrées avec succès...", Toast.LENGTH_LONG).show();
                    temporaryFingerPrints.clear();
                    btnSynchronizeFingerPrints.setText("Aucune vente à synchroniser");
                }

                //Log.d("SERVER",jsonObject.toString());
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
        int pos=1;
        for (FingerPrintTemp fpT : listFingerPrints)
        {
            testSendFingerPrintToServer(fpT,pos);
            pos++;
        }
    }

    //TEST SERVER
    private void testSendFingerPrintToServer(FingerPrintTemp fpT,int pos){
        JsonObject login = new JsonObject();
        JsonObject obj = new JsonObject();
            login.addProperty ("Email",Email);
            login.addProperty("Password",Password);
            obj.addProperty("EmployeId", 3);
            obj.addProperty("Finger", "RTF");
            obj.addProperty("FingerPrint",fpT.getFingerPrintImageBase64());
            obj.addProperty("Template", fpT.getFingerPrintTemplateBase64());
            obj.add("Login",login);
            String data = obj.toString();
            //Log.d("SERVER", "JSON : " + data);
           postDataToServer(obj,pos);


    }

    private void postDataToServer(JsonObject obj,int pos){
        // Using the Retrofit
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.postFingerPrint (obj);
        showProgress("Synchronisation des empreintes ",true);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try{
                    Log.e("response-success", response.body().toString());
                    if(pos==temporaryFingerPrints.size()){
                        showProgress("Synchronisation des empreintes terminée.",false);
                        nbFp.setText("Synchronisation des empreintes terminée");
                        btnSynchronizeFingerPrints.setVisibility(View.INVISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("response-failure", call.toString());
                showProgress("Synchronisation des empreintes terminée.",false);
                nbFp.setText("Une erreur est survenue.Reessayez");
            }

        });
    }


}