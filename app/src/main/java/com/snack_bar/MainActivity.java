package com.snack_bar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;
import com.snack_bar.model.EmployeeFingerTemplate;
import com.snack_bar.model.Item;
import com.snack_bar.network.ApiClient;
import com.snack_bar.network.ApiInterface;
import com.snack_bar.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import asia.kanopi.fingerscan.Fingerprint;
import asia.kanopi.fingerscan.Status;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus,tvError;
    private Fingerprint fingerprint;
    private Button buttonRetry;
    private byte[] fingerCaptured;
    private DatabaseHelper db;
    private List<EmployeeFingerTemplate> listDbFingerPrints;
    private Helper helper;
    private ProgressDialog dialog;
    //Holds all Products
    private ArrayList<Item> productsList;
    //Holds all Employees
    private ArrayList<Employee> employeesList;
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("KSSM");
        //HELPER
        helper = new Helper();
        //DATABASE
        db=new DatabaseHelper(this);
        //FINGERPRINT INSTANCE FROM KANOPI
        fingerprint = new Fingerprint();
        //LIST OF FINGERPRINTS FROM DB
        listDbFingerPrints = new ArrayList<EmployeeFingerTemplate>();
        productsList = new ArrayList<Item>();
        employeesList = new ArrayList<Employee>();
        //LAUNCH LOGIN ACTIVITY
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Boolean isLogin = sp.getBoolean("isLogin", false);
        if(!isLogin){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        //LOAD COMPONENTS
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvError = (TextView) findViewById(R.id.tvError);
        buttonRetry = (Button) findViewById(R.id.btnRetry);
        buttonRetry.setVisibility(View.GONE);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // startScan();
                // FOR TEST 9A6060AF
                Intent intent = new Intent(MainActivity.this, ProductsList.class);
                intent.putExtra("EmployeeFullName","Ansderly RAMEAU | AR007-1");
                intent.putExtra("EmployeeId",1);
                startActivity(intent);
            }
        });
        //LOAD ALL FINGERPRINTS FROM DB
        new LoadFingerPrintsFromDB().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("status", -1);
                setResult(RESULT_CANCELED,intent);
                fingerprint.turnOffReader();
                //setContentView(R.layout.cart_content);
                finish();
                return true;
            case R.id.sync_products:
                synchronizeProducts();
                return true;
            case R.id.sync_employees:
                synchronizeEmployees();
                return true;
            case R.id.list_employees:
                startActivity(new Intent(MainActivity.this, EmployeeListActivity.class));
                return true;
            case R.id.list_sales:
                startActivity(new Intent(MainActivity.this, SalesListActivity.class));
                return true;
            case R.id.manage_finger:
                startActivity(new Intent(MainActivity.this, SyncFingerPrintToServer.class));
                return true;
            case R.id.LogOut:
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isLogin", false);
                editor.apply();
                intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        startScan();
        super.onStart();
    }

    private void startScan() {
        fingerprint.scan(this, printHandler, updateHandler);
    }

    @Override
    protected void onStop() {
        fingerprint.turnOffReader();
        super.onStop();
    }

    Handler updateHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int status = msg.getData().getInt("status");
            tvError.setText("");
            switch (status) {
                case Status.INITIALISED:
                    tvStatus.setText("Setting up reader");
                    buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.SCANNER_POWERED_ON:
                    tvStatus.setText("Reader powered on");
                    buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.READY_TO_SCAN:
                    tvStatus.setText("Ready to scan finger");
                    buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.FINGER_DETECTED:
                    tvStatus.setText("Finger detected");
                    buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.RECEIVING_IMAGE:
                    tvStatus.setText("Receiving image");
                    buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.FINGER_LIFTED:
                    tvStatus.setText("Finger has been lifted off reader");
                    buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.SCANNER_POWERED_OFF:
                    tvStatus.setText("Reader is off");
                    buttonRetry.setVisibility(View.VISIBLE);
                    break;
                case Status.SUCCESS:
                    tvStatus.setText("Fingerprint successfully captured");
                    break;
                case Status.ERROR:
                    tvStatus.setText("Error");
                    buttonRetry.setVisibility(View.VISIBLE);
                    tvError.setTextColor(Color.rgb(245,0,0));
                    tvError.setText(msg.getData().getString("errorMessage"));
                    break;
                default:
                    buttonRetry.setVisibility(View.GONE);
                    tvStatus.setText(String.valueOf(status));
                    tvError.setText(msg.getData().getString("errorMessage"));
                    break;

            }
        }
    };
    Handler printHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            byte[] image;
            String errorMessage = "empty";
            int status = msg.getData().getInt("status");
            Intent intent = new Intent(MainActivity.this, ProductsList.class);
            intent.putExtra("status", status);
            if (status == Status.SUCCESS) {
                image = msg.getData().getByteArray("img");
                //String str_img = msg.getData().getString("img");

               // intent.putExtra("img", image);
                fingerCaptured = image;
                int matchId =  verifyFingerPrints();
                if(matchId != 0){
                    buttonRetry.setVisibility(View.GONE);
                    Employee match = db.getEmployeeInfo(matchId);
                    //Toast.makeText(getApplicationContext(),"Vous etes "+match.getEmployeeId(),Toast.LENGTH_LONG).show();
                    // Launch new intent instead of loading fragment
                    intent.putExtra("EmployeeFullName",match.getFull_name()+" | "+match.getEmployee_code());
                    intent.putExtra("EmployeeId",match.getEmployee_id());
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Aucune correspondance...",Toast.LENGTH_LONG).show();
                    buttonRetry.setVisibility(View.VISIBLE);
                }
            } else {
                errorMessage = msg.getData().getString("errorMessage");
                intent.putExtra("errorMessage", errorMessage);
                tvError.setText(errorMessage);
            }
        }
    };

    //VERIFY FINGER
    private int verifyFingerPrints(){
        int employeeId=0 ;
        if(fingerCaptured!= null){
            FingerprintTemplate fingerprintCapturedTemplate = helper.createTemplate(fingerCaptured);
            employeeId = helper.verifyFingerPrint(fingerprintCapturedTemplate,listDbFingerPrints);
        }else{
            Toast.makeText(getApplicationContext(),"Empreinte invalide...",Toast.LENGTH_LONG).show();
        }
        return employeeId;
    }
    //LOAD ALL THE FINGERPRINTS FROM DB
    public class LoadFingerPrintsFromDB extends AsyncTask<String,String,List<EmployeeFingerTemplate>> {

        @Override
        protected List<EmployeeFingerTemplate> doInBackground(String... strings) {
            return db.getFingersTemplate();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress("Initialisation du système...",true);
        }

        @Override
        protected void onPostExecute(List<EmployeeFingerTemplate> fingerPrints) {
            super.onPostExecute(fingerPrints);
            listDbFingerPrints = fingerPrints;
            Log.d("FINGERPRINT2","FOUND : "+listDbFingerPrints.size());
            //tvStatus.setText("QTY FINGERPRINTS" +" : "+listDbFingerPrints.size());
            showProgress("Pret!",false);
        }
    }
    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(MainActivity.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
        } else
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
    //LOAD DATA FROM SERVER
    //GET ALL THE PRODUCTS
    private void getAllProducts(){
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<JsonObject> call = apiService.getAllProducts();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.d("SERVER",response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                     //GET ALL PRODUCTS
                    JSONArray arrayProducts = jsonObject.getJSONArray("Products");
                    //EMPTY THE PRODUCTS TABLE
                    db.emptyTable("products");
                    for (int i = 0; i < arrayProducts.length(); i++) {
                        JSONObject data = arrayProducts.getJSONObject(i);
                        Item product = new Item(data.getInt("id"),data.getInt("categoryId"),data.getInt("subCategoryId"),data.getString("name"),data.getDouble("unitPrice"),data.getString("url"));
                        productsList.add(product);
                        db.saveProducts(data.getInt("id"),data.getInt("subCategoryId"),data.getString("name"),data.getDouble("unitPrice"),data.getString("url"));
                        Log.d("SERVER 3",data.getString("name"));
                    }
                    showProgress("",false);
                    showMessage(true, "Synchronisation terminée...");
//                    Intent intent = getIntent();
//                    finish();
//                    startActivity(intent);
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
    //GET ALL THE EMPLOYEES
    private void getEmployees(){
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getAllEmployees("All");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.d("SERVER",response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //GET ALL EMPLOYEES
                    JSONArray array = jsonObject.getJSONArray("Employees");
                    //EMPTY THE EMPLOYEES TABLE
                    db.emptyTable("employes");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject data = array.getJSONObject(i);
                        Employee employee = new Employee(data.getInt("id"),data.getInt("entreprise_id"),data.getString("employe_code"),data.getString("employe_prenom"),data.getString("employe_nom"));
                        db.saveEmployees(employee);
                        Log.d("SERVER 1",data.getString("employe_prenom"));
                    }

                    //GET ALL FINGERPRINTS
                    JSONArray arraySub = jsonObject.getJSONArray("FingerPrints");
                    //EMPTY THE FINGERPRINTS TABLE
                    db.emptyTable("empreintes");
                    for (int i = 0; i < arraySub.length(); i++) {
                        JSONObject data = arraySub.getJSONObject(i);
                        int employeeId=data.getInt("employe_id");
                        byte[] fp =helper.base64ToByteArray(data.getString("finger_print"));
                        byte[] tp = helper.base64ToByteArray(data.getString("template"));
                        db.saveFingerPrintsFromServer(employeeId,fp,tp);
                        Log.d("SERVER 2","EMPLOYE ID : "+employeeId);
                    }

                    showProgress("",false);
                    showMessage(true, "Synchronisation terminée...");
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
    //SYNCHRONIZE THE PRODUCTS FROM SERVER
    private void synchronizeProducts() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Synchronisation des produits")
                .setMessage("Voulez-vous supprimer et remplacer les produits de la base de données locale?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        showProgress("Synchronisation des produits...",true);
                        getAllProducts();//Get Products fom server
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
    // SYNCHRONIZE THE PRODUCTS FROM SERVER
    private void synchronizeEmployees() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Synchronisation des employés")
                .setMessage("Voulez-vous supprimer et remplacer les employés de la base de données locale?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        showProgress("Synchronisation des employés...",true);
                        getEmployees();//Get Products fom server
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