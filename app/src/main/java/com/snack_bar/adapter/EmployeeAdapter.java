package com.snack_bar.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.snack_bar.AddFingerPrintActivity;
import com.snack_bar.R;
import com.snack_bar.model.Employee;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder >  {
    private List<Employee> employees;
    private Context context;
    String employeeInfo;
    public EmployeeAdapter(Context context,List<Employee> employees) {
        this.employees = employees;
        this.context=context;
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
        employeeInfo = employee.getEmployee_prenom()+" "+employee.getEmployee_nom()+" | "+employee.getEmployee_id();
//        if(employeeInfo.length()>25){
//            employeeInfo = employeeInfo.substring(0,25)+"...";
//        }
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
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageButton addFingerPrint;
        ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            addFingerPrint = (ImageButton) itemView.findViewById(R.id.btnAddFinger);
        }
    }
}
