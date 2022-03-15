package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.snack_bar.activities.AddFingerPrintActivity;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Employee;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder >  {
    private List<Employee> employees;
    private Context context;
    String employeeInfo;
    private Activity activity; //The activity we want to communicate with
    private IEmployeeAdapterCallback employeeCallback;

    //MY INTERFACE TO INSURE THE COMMUNICATION
    public interface IEmployeeAdapterCallback
    {
        void onImportFingerPrints(Employee employee);
    }
    public EmployeeAdapter(Context context,List<Employee> employees,Activity activity) {
        this.employees = employees;
        this.context=context;
        this.activity=activity;
        employeeCallback = (IEmployeeAdapterCallback) activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_employee_layout, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Employee employee = employees.get(position);
        employeeInfo = employee.getEmployee_code()+" | "+employee.getEmployee_prenom()+" "+employee.getEmployee_nom();
        int nbFingerPrint =  employee.getNbFingerPrints();
        if(employeeInfo.length()>50){
            employeeInfo = employeeInfo.substring(0,50)+"... | "+nbFingerPrint+" FP";
        }else{
            employeeInfo = employeeInfo+" | "+nbFingerPrint+" FP";
        }
        //Log.e("FP","COUNT FP : "+nbFingerPrint);
        holder.textViewName.setText(employeeInfo.toUpperCase());
        if(nbFingerPrint==0){
            holder.r_layout.setBackgroundColor(Color.rgb(245, 130, 167));
        }else {
            holder.r_layout.setBackgroundColor(Color.rgb(193, 248, 207));
        }
        holder.addFingerPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(),"FINGERPRINTS FOR "+employeeInfo,Toast.LENGTH_LONG).show();
                Intent intent =new Intent(context, AddFingerPrintActivity.class);
                intent.putExtra("Employe", employee);
                context.startActivity(intent);
            }
        });
        holder.btnImportFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CALL THE FUNCTION IN THE ACTIVITY
                employeeCallback.onImportFingerPrints(employee);
            }
        });
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageButton addFingerPrint,btnImportFinger;
        RelativeLayout r_layout ;
        ViewHolder(View itemView) {
            super(itemView);
            r_layout = (RelativeLayout) itemView.findViewById(R.id.r_layout);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            addFingerPrint = (ImageButton) itemView.findViewById(R.id.btnAddFinger);
            btnImportFinger = (ImageButton) itemView.findViewById(R.id.btnImportFinger);
        }
    }
}
