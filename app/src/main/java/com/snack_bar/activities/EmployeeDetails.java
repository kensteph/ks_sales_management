package com.snack_bar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.snack_bar.R;

public class EmployeeDetails extends AppCompatActivity {
private Button btnGoToSale;
private TextView tv_first_name,tv_last_name,tv_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);
        //GET THE INFO FROM THE ACTIVITY
        String EmployeeFullName = getIntent().getStringExtra("EmployeeFullName");
        String first_name = getIntent().getStringExtra("FirstName");
        String last_name = getIntent().getStringExtra("LastName");
        String employeeCode = getIntent().getStringExtra("EmployeeCode");
        int employeeSelectedID = getIntent().getIntExtra("EmployeeId",0);

        tv_first_name = (TextView) findViewById(R.id.tv_first_name);
        tv_last_name = (TextView) findViewById(R.id.tv_last_name);
        tv_code = (TextView) findViewById(R.id.tv_code);
        //SET VALUES
        tv_first_name.setText(first_name);
        tv_last_name.setText(last_name);
        tv_code.setText(employeeCode);

        btnGoToSale = (Button) findViewById(R.id.btnGoToSale);
        btnGoToSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDetails.this, ProductsList.class);
                intent.putExtra("EmployeeFullName",EmployeeFullName);
                intent.putExtra("EmployeeId",employeeSelectedID);
                intent.putExtra("SaleType",1);
                startActivity(intent);
                finish();
            }
        });
    }
}