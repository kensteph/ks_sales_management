package com.snack_bar.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Employee employee = employees.get(position);
        employeeInfo = employee.getEmployee_code()+" | "+employee.getEmployee_prenom()+" "+employee.getEmployee_nom();
        if(employeeInfo.length()>50){
            employeeInfo = employeeInfo.substring(0,50)+"...";
        }
        holder.textViewName.setText(employeeInfo.toUpperCase());
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
        ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            addFingerPrint = (ImageButton) itemView.findViewById(R.id.btnAddFinger);
            btnImportFinger = (ImageButton) itemView.findViewById(R.id.btnImportFinger);
        }
    }
}
