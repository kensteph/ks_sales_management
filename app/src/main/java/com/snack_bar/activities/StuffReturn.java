package com.snack_bar.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
        stuffList.add(new Stuff(1, "Plate (Assiette)", R.drawable.plate, false));
        stuffList.add(new Stuff(3, "Spoon (Cuillère)", R.drawable.couvert, false));
        stuffList.add(new Stuff(2, "Bottle (Bouteille)", R.drawable.bottle, false));

        //INIT COMPONENT
        recyclerView = (RecyclerView) findViewById(R.id.rv_stuff_to_return);
        btn_save_stuff = (Button) findViewById(R.id.btn_save_stuff);


        stuffAdapter = new StuffAdapter(getBaseContext(), this, stuffList);
        recyclerView.setAdapter(stuffAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btn_save_stuff.setVisibility(View.GONE);

        btn_save_stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long res = db.saveStuffReturn(helper.getCurrentDate(), employeeSelectedID, plate, spoon, bottle);
                boolean type = false;
                String msg = "Error...";
                if (res != -1) {
                    type = true;
                    msg = "Done..";
                }
                showMessage(type, msg);
                finish();
            }
        });
    }

    public void updateUi() {
        long res = db.saveStuffReturn(helper.getCurrentDate(), employeeSelectedID, plate, spoon, bottle);
        boolean type = false;
        String msg = "Error...";
        if (res != -1) {
            type = true;
            msg = "Done..";
        }
        showMessage(type, msg);
        finish();
    }

    private void showProgress(String msg, boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(StuffReturn.this);
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
            if(stuffSelected.isSelected()){
                nbSelected++;
                if (stuffName.contains("Bottle")) {
                    bottle = "1";
                }
                if (stuffName.contains("Plate")) {
                    plate = "1";
                }
                if (stuffName.contains("Spoon")) {
                    spoon = "1";
                }
            }else{
                if (stuffName.contains("Bottle")) {
                    bottle = "0";
                }
                if (stuffName.contains("Plate")) {
                    plate = "0";
                }
                if (stuffName.contains("Spoon")) {
                    spoon = "0";
                }
            }
            Log.e("STUFF","STUFF : "+stuffSelected.getStuffName()+" IS_SELECTED : "+stuffSelected.isSelected());
        }
       //DISPLAY THE BUTTON
        if(nbSelected!=0){
            btn_save_stuff.setVisibility(View.VISIBLE);
        }else{
            btn_save_stuff.setVisibility(View.GONE);
        }
        //SAVE THE DATA
        Log.e("STUFF","PLATE : "+plate+" BOTTLE : "+bottle+"  SPOON : "+spoon);
    }
}