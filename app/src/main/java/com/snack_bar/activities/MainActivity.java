package com.snack_bar.activities;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.snack_bar.R;
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

    private TextView tvStatus, tvError;
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
    //SHARED PREFERENCES
    private static final String SHARED_PREF_NAME = "MY_SHARED_PREFERENCES";
    SharedPreferences sp;
    private String Email;
    private String Password;

    //DRAWER MENU
    DrawerLayout drawer;
    NavigationView navigationView;
    FrameLayout frameLayout;
    ActionBarDrawerToggle toggle;
    ImageView imageView;
    Toolbar toolbar;
    View header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //DRAWER MENU
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        frameLayout = (FrameLayout) findViewById(R.id.frame);


        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //HELPER
        helper = new Helper();
        //DATABASE
        db = new DatabaseHelper(this);
        //FINGERPRINT INSTANCE FROM KANOPI
        fingerprint = new Fingerprint();
        //LIST OF FINGERPRINTS FROM DB
        listDbFingerPrints = new ArrayList<EmployeeFingerTemplate>();
        productsList = new ArrayList<Item>();
        employeesList = new ArrayList<Employee>();

        //GET INFO FROM SHARED PREFERENCES
        sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Email = sp.getString("email", "");
        Password = sp.getString("password", "");
        Boolean isLogin = sp.getBoolean("isLogin", false);

        //LAUNCH LOGIN ACTIVITY
        if (!isLogin) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        //LOAD COMPONENTS
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvError = (TextView) findViewById(R.id.tvError);
        buttonRetry = (Button) findViewById(R.id.btnRetry);

        //buttonRetry.setVisibility(View.GONE);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        //LOAD ALL FINGERPRINTS FROM DB
        new LoadFingerPrintsFromDB().execute();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                setResult(RESULT_CANCELED, intent);
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
            case R.id.manual_sales:
                startActivity(new Intent(MainActivity.this, ManualSales.class));
                return true;
            case R.id.list_sales:
                startActivity(new Intent(MainActivity.this, SalesListActivity.class));
                return true;
            case R.id.sync_sales:
                startActivity(new Intent(MainActivity.this, SyncSales.class));
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

    //SCANNER
    Handler updateHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int status = msg.getData().getInt("status");
            tvError.setText("");
            switch (status) {
                case Status.INITIALISED:
                    tvStatus.setText("Setting up reader");
                    //buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.SCANNER_POWERED_ON:
                    tvStatus.setText("Reader powered on");
                    //buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.READY_TO_SCAN:
                    tvStatus.setText("Ready to scan finger");
                    // buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.FINGER_DETECTED:
                    tvStatus.setText("Finger detected");
                    //buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.RECEIVING_IMAGE:
                    tvStatus.setText("Receiving image");
                    //buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.FINGER_LIFTED:
                    tvStatus.setText("Finger has been lifted off reader");
                    //buttonRetry.setVisibility(View.GONE);
                    break;
                case Status.SCANNER_POWERED_OFF:
                    tvStatus.setText("Reader is off");
                    //buttonRetry.setVisibility(View.VISIBLE);
                    break;
                case Status.SUCCESS:
                    tvStatus.setText("Fingerprint successfully captured");
                    break;
                case Status.ERROR:
                    tvStatus.setText("Error");
                    buttonRetry.setVisibility(View.VISIBLE);
                    tvError.setTextColor(Color.rgb(245, 0, 0));
                    tvError.setText(msg.getData().getString("errorMessage"));
                    break;
                default:
                    //buttonRetry.setVisibility(View.GONE);
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
                int matchId = verifyFingerPrints();
                if (matchId != 0) {
                    buttonRetry.setVisibility(View.GONE);
                    Employee match = db.getEmployeeInfo(matchId);
                    Log.d("MATCH EMP", match.toString());
                    //Toast.makeText(getApplicationContext(),"Vous etes "+match.getEmployeeId(),Toast.LENGTH_LONG).show();
                    // Launch new intent instead of loading fragment
                    intent.putExtra("EmployeeFullName", match.getEmployee_code() + " | " + match.getFull_name());
                    intent.putExtra("EmployeeId", match.getEmployee_id());
                    intent.putExtra("SaleType",0);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Aucune correspondance...", Toast.LENGTH_LONG).show();
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
    private int verifyFingerPrints() {
        int employeeId = 0;
        if (fingerCaptured != null) {
            FingerprintTemplate fingerprintCapturedTemplate = helper.createTemplate(fingerCaptured);
            employeeId = helper.verifyFingerPrint(fingerprintCapturedTemplate, listDbFingerPrints);
        } else {
            Toast.makeText(getApplicationContext(), "Empreinte invalide...", Toast.LENGTH_LONG).show();
        }
        return employeeId;
    }

    //LOAD ALL THE FINGERPRINTS FROM DB
    public class LoadFingerPrintsFromDB extends AsyncTask<String, String, List<EmployeeFingerTemplate>> {

        @Override
        protected List<EmployeeFingerTemplate> doInBackground(String... strings) {
            return db.getFingersTemplate();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress("Please wait....", true);
        }

        @Override
        protected void onPostExecute(List<EmployeeFingerTemplate> fingerPrints) {
            super.onPostExecute(fingerPrints);
            listDbFingerPrints = fingerPrints;
            Log.d("FINGERPRINT2", "FOUND : " + listDbFingerPrints.size());
            //tvStatus.setText("QTY FINGERPRINTS" +" : "+listDbFingerPrints.size());
            showProgress("Ready!", false);
        }
    }

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(MainActivity.this);
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
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }

    //LOAD DATA FROM SERVER
    //GET ALL THE PRODUCTS
    private void getAllProducts() {
        showProgress("Products Synchronization starts...", true);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Log.d("CREDENTIALS", Email + " | " + Password);
        Call<JsonObject> call = apiService.getAllProducts(Email, Password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("SERVER", response.message());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //GET ALL PRODUCTS
                    JSONArray arrayProducts = jsonObject.getJSONArray("Products");
                    Log.d("SERVER OBJ", arrayProducts.toString());
                    //EMPTY THE PRODUCTS TABLE
                    db.emptyTable("products");
                    for (int i = 0; i < arrayProducts.length(); i++) {
                        JSONObject data = arrayProducts.getJSONObject(i);
                        int productID = data.getInt("ProduitId");
                        int categoryID = data.getInt("CategoryId");
                        String productDESC = data.getString("Description");
                        double productPrice = data.getDouble("Prix");
                        String productIMG = data.getString("IconeUrl") + ".jpg";
                        Item product = new Item(productID, categoryID, categoryID, productDESC, productPrice, productIMG);
                        productsList.add(product);
                        db.saveProducts(productID, categoryID, productDESC, productPrice, productIMG);
                        Log.d("SERVER 3", productDESC);
                    }
                    showProgress("", false);
                    showMessage(true, "Synchronization complete...");
//                    Intent intent = getIntent();
//                    finish();
//                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress("", false);
                    showMessage(false, "" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("", false);
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                //Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("SERVER", t.getMessage());
            }
        });
    }

    //GET ALL THE EMPLOYEES REFERENCES
    private void getEmployeesReferences() {
        showProgress("Employee Synchronization starts...", true);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getEmployeesReferences(Email, Password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Log.d("SERVER",response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    //GET ALL EMPLOYEES
                    JSONArray array = jsonObject.getJSONArray("References");
                    //EMPTY THE EMPLOYEES TABLE
                    db.emptyTable("employes");
                    //EMPTY THE FINGERPRINTS TABLE
                    db.emptyTable("empreintes");
                    Log.d("SERVER REP", "REFERENCES COUNT : " + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        int employeeID = array.getInt(i);
                        getEmployeeInfo(employeeID, array.length(), i + 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress("", false);
                    showMessage(true, e.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("", false);
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                //Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("SERVER", t.getMessage());
            }
        });
    }

    //GET EMPLOYEES INFO
    private void getEmployeeInfo(int employeeRef, int employeeCount, int position) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getEmployee(employeeRef, Email, Password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("SERVER EMP", response.body().toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    String employee_FirstName = jsonObject.getString("Prenom");
                    String employee_LastName = jsonObject.getString("Nom");
                    String employee_CIN = jsonObject.getString("CIN").trim();
                    int employee_ID = jsonObject.getInt("EmployeId");
                    int employee_Enterprise = 1;
                    //SAVE EMPLOYEE IN LOCAL DATABASE
                    Employee employee = new Employee(employee_ID, employee_Enterprise, employee_CIN, employee_FirstName, employee_LastName);
                    db.saveEmployees(employee);
                    Log.d("SERVER 1", employee_FirstName);
                    Log.d("EMPLOYEE", "EMPLOYEE : " + employee_FirstName);

                    //GET FINGER PRINTS
                    JSONArray array = jsonObject.getJSONArray("Fingers");
                    Log.d("FINGER PRINTS", "FINGER PRINTS : " + array.length());

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject data = array.getJSONObject(i);
                        String finger = data.getString("Finger");
                        byte[] fp = helper.base64ToByteArray(data.getString("FingerPrint"));
                        byte[] tp = helper.base64ToByteArray(data.getString("Template"));
                        db.saveFingerPrintsFromServer(employee_ID, finger, fp, tp);
                        Log.d("SERVER 2", "EMPLOYE ID : " + employee_ID);
                        Log.d("POSITION " + position, "CURRENT POS : " + employeeCount);
                        if (position == employeeCount) {
                            showProgress("", false);
                            showMessage(true, "Synchronization complete...");
                            //RELOAD THE MAIN SCREEN
                            finish();
                            startActivity(getIntent());
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("SERVER ERR", e.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress("", false);
                showMessage(false, "PLEASE VERIFY YOUR CREDENTIALS OR  NETWORK CONNECTION...");
                //Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("SERVER", t.getMessage());
            }
        });
    }


    //SYNCHRONIZE THE PRODUCTS FROM SERVER
    private void synchronizeProducts() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Sync Products")
                .setMessage("Do you really want to SYNC PRODUCTS?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getAllProducts();//Get Products fom server
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    // SYNCHRONIZE THE PRODUCTS FROM SERVER
    private void synchronizeEmployees() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setCancelable(false);
        builder.setTitle("Sync Employees")
                .setMessage("Do you really want to SYNC EMPLOYEES?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getEmployeesReferences();
                        //getEmployees();//Get Products fom server
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