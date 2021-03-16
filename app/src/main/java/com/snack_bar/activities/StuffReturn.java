package com.snack_bar.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.util.Helper;

import java.util.ArrayList;

public class StuffReturn extends AppCompatActivity {
    private Button mPickStuff, btn_save_stuff;
    private ProgressDialog dialog;
    private TextView mStuffReturn;
    private String[] listStuff;
    private boolean[] checkedStuff;
    private ArrayList<Integer> stuffReturnByUser = new ArrayList<>();
    private DatabaseHelper db;
    private Helper helper;
    String plate, spoon, bottle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stuff_return);
        ActionBar actionBar = getSupportActionBar();
        //GET THE INFO FROM THE ACTIVITY
        String EmployeeFullName = getIntent().getStringExtra("EmployeeFullName");
        int employeeSelectedID = getIntent().getIntExtra("EmployeeId", 0);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(EmployeeFullName);

        helper = new Helper();
        db = new DatabaseHelper(this);

        //INIT COMPONENT
        mPickStuff = (Button) findViewById(R.id.btn_pick_stuff);
        btn_save_stuff = (Button) findViewById(R.id.btn_save_stuff);
        mStuffReturn = (TextView) findViewById(R.id.tv_stuff_return);

        btn_save_stuff.setVisibility(View.GONE);

        listStuff = getResources().getStringArray(R.array.stuff);
        checkedStuff = new boolean[listStuff.length];

        mPickStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(StuffReturn.this);
                mBuilder.setTitle("Stuff List");
                mBuilder.setMultiChoiceItems(listStuff, checkedStuff, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        Log.e("BEFORE", "ITEMS :" + stuffReturnByUser.toString() + " POS : " + position + "  IS CHECKED : " + isChecked);

                            if (isChecked) {
                                if (!stuffReturnByUser.contains(position)) {
                                    stuffReturnByUser.add(position);
                                }
                            }else{
                                if (stuffReturnByUser.size()==1) {
                                    stuffReturnByUser.clear();
                                }else {
                                    stuffReturnByUser.remove(position);
                                }
                            }
                        Log.e("AFTER", "ITEMS :" + stuffReturnByUser.toString() + " POS : " + position + "  IS CHECKED : " + isChecked);
                    }
                });
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String stuffSelected = "";
                        bottle = "0";
                        plate = "0";
                        spoon = "0";
                        for (int i = 0; i < stuffReturnByUser.size(); i++) {
                            String stuff = listStuff[stuffReturnByUser.get(i)];
                            stuffSelected = stuffSelected + stuff;
                            if (i != stuffReturnByUser.size() - 1) {
                                stuffSelected = "\n" + stuffSelected + ", ";
                            }
                            if (stuff.contains("Bottle")) {
                                bottle = "1";
                            }
                            if (stuff.contains("Plate")) {
                                plate = "1";
                            }
                            if (stuff.contains("Spoon")) {
                                spoon = "1";
                            }
                        }
                        mStuffReturn.setText(stuffSelected);
                        mPickStuff.setVisibility(View.GONE);
                        btn_save_stuff.setVisibility(View.VISIBLE);
                    }
                });
                mBuilder.setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                mBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedStuff.length; i++) {
                            checkedStuff[i] = false;
                            stuffReturnByUser.clear();
                            mStuffReturn.setText("");
                        }
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

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

}