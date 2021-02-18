package com.snack_bar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;

public class ManualSales extends AppCompatActivity {
    private Button btnSearch;
    private EditText searchCode;
    private DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_sales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //DATABASE
        db = new DatabaseHelper(this);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        searchCode = (EditText) findViewById(R.id.et_employeeCode);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GET THE USER IN THE DB
                String codeToSearch = searchCode.getText().toString().trim().toUpperCase();
                Log.d("EMPLOYEE", "SEARCHING FOR ... :  " + codeToSearch);
                if (codeToSearch != null && codeToSearch.length() > 0) {
                    Employee employee = db.getEmployeeInfoByCode(codeToSearch);
                    if(employee !=null) {
                        searchCode.setText("");
                        String first_name = employee.getEmployee_prenom();
                        String last_name = employee.getEmployee_nom();
                        String fn = employee.getEmployee_code() + " | " + employee.getFull_name();
                        String codeEmp = employee.getEmployee_code().trim();
                        int idEmp = employee.getEmployee_id();

                        Log.d("EMPLOYEE", "FULL NAME :  " + fn + " ID : " + idEmp);
                        Intent intent = new Intent(ManualSales.this, EmployeeDetails.class);
                        intent.putExtra("FirstName", first_name);
                        intent.putExtra("LastName", last_name);
                        intent.putExtra("EmployeeFullName", fn);
                        intent.putExtra("EmployeeId", idEmp);
                        intent.putExtra("EmployeeCode", codeEmp);

                        startActivity(intent);
                        finish();
                    }else{
                        String msg = "Employee Code Incorrect...";
                        showMessage(false, msg);
                        Log.e("EMPLOYE", msg);
                    }
                } else {
                    String msg = "Employee Code Incorrect...";
                    showMessage(false, msg);
                    Log.e("EMPLOYE", msg);
                }
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

    //Shows a message by using Snackbar
    private void showMessage(Boolean isSuccessful, String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        if (isSuccessful) {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(ManualSales.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(ManualSales.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
}