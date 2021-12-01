package com.snack_bar.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.snack_bar.R;
import com.snack_bar.adapter.StuffAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Stuff;
import com.snack_bar.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class StuffReturn extends AppCompatActivity implements StuffAdapter.StuffCallBack {
    private Button mPickStuff, btn_save_stuff;
    private ProgressDialog dialog;
    private TextView mStuffReturn;
    private String[] listStuff;
    private boolean[] checkedStuff;
    private ArrayList<Integer> stuffReturnByUser = new ArrayList<>();
    private DatabaseHelper db;
    private Helper helper;
    String plate="0", spoon="0", bottle="0";
    private List<Stuff> stuffList;
    private List<Stuff> stuffListSelected;
    private RecyclerView recyclerView;
    private StuffAdapter stuffAdapter;
    private int employeeSelectedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stuff_return);
        ActionBar actionBar = getSupportActionBar();
        //GET THE INFO FROM THE ACTIVITY
        String EmployeeFullName = getIntent().getStringExtra("EmployeeFullName");
        employeeSelectedID = getIntent().getIntExtra("EmployeeId", 0);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(EmployeeFullName);

        helper = new Helper();
        db = new DatabaseHelper(this);
        stuffList = new ArrayList<>();
        stuffListSelected = new ArrayList<>();
        //GET STUffS
        stuffList = db.getAllStuffsFromDB();
        //INIT COMPONENT
        recyclerView = (RecyclerView) findViewById(R.id.rv_stuff_to_return);
        btn_save_stuff = (Button) findViewById(R.id.btn_save_stuff);


        stuffAdapter = new StuffAdapter(getBaseContext(), this, stuffList);
        recyclerView.setAdapter(stuffAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        btn_save_stuff.setVisibility(View.GONE);

        btn_save_stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nbToSave=0; int saved=0;
                for (int i=0;i<stuffList.size();i++){
                    Stuff stuffSelected = stuffList.get(i);
                    String stuffName = stuffSelected.getStuffName();
                    int qty = stuffSelected.getQty();
                    int stuff_id = stuffSelected.getStuffId();
                    boolean ifSelected = stuffSelected.isSelected();
                    //SAVE TO DB ONLY THE SELECTED STUFF
                    if(ifSelected){
                        nbToSave++;
                        long res = db.saveStuffReturn(helper.getCurrentDate(),employeeSelectedID,stuff_id,qty);
                        if(res != -1){
                            saved++;
                        }
                        Log.e("STUFF","STUFF RETURN: "+stuffName+" QTY RETURN : "+qty);
                    }
                }
                Log.e("STUFF OP","STUFF RETURN OP RESULT : "+saved+" / "+nbToSave);
        if(saved==nbToSave){
            finish();
            Toast.makeText(getApplicationContext(),"Stuff return successfully..", Toast.LENGTH_LONG).show();
        }

            }
        });
    }


    //Shows a message by using Snackbar
    private void showMessage(Boolean isSuccessful, String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        if (isSuccessful) {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(StuffReturn.this, R.color.colorAccent));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(StuffReturn.this, R.color.design_default_color_error));
        }
        snackbar.show();
    }

    @Override
    public void onSelectStuff(Stuff stuff) {
        int nbSelected=0;
        for (int i=0;i<stuffList.size();i++){
            Stuff stuffSelected = stuffList.get(i);
            String stuffName = stuffSelected.getStuffName();
            int qty = stuffSelected.getQty();
            boolean ifSelected = stuffSelected.isSelected();
            //ADD ONLY THE SELECTED STUFF
            if(ifSelected){
                nbSelected++;
                Log.e("STUFF","STUFF : "+stuffName+" QTY RETURN : "+qty);
            }
        }
       //DISPLAY THE BUTTON
        if(nbSelected!=0){
            btn_save_stuff.setVisibility(View.VISIBLE);
        }else{
            btn_save_stuff.setVisibility(View.GONE);
        }
    }

    @Override
    public void onIncreaseOrDecrease(Stuff stuff, String action) {

    }
}