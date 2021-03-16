package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.StuffReturnModel;

import java.util.List;


public class StuffReturnAdapter extends RecyclerView.Adapter<StuffReturnAdapter.SalesVH> {
    List<StuffReturnModel> salesList;
    Context context;
    private DatabaseHelper db;
    public StuffReturnAdapter(List<StuffReturnModel> salesList, Context context) {
        this.salesList = salesList;
        this.context = context;
    }

    @Override
    public SalesVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sync_stuff_return_row,parent,false);
        return new SalesVH(view);
    }

    @Override
    public void onBindViewHolder(SalesVH holder, @SuppressLint("RecyclerView") final int position) {
        StuffReturnModel stuffReturn = salesList.get(position);

//        Glide.with(context)
//                .load(productReport.getProductImage())
//                .into(holder.productImage);
        holder.employee.setText(stuffReturn.getFullName());
        holder.returnDate.setText(stuffReturn.getDateReturn());
        String description = "";
        String plate = stuffReturn.getPlateReturn();
        if(Integer.parseInt(plate.trim())!=0){
            description = description + "Plate \n";
        }
        String spoon = stuffReturn.getSpoonReturn();
        if(Integer.parseInt(spoon.trim())!=0){
            description = description + "Spoon \n";
        }
        String bottle = stuffReturn.getBottleReturn();
        if(Integer.parseInt(bottle.trim())!=0){
            description = description + "Bottle \n";
        }
        Log.e("RETURN",""+stuffReturn.getFullName()+" | "+"PLATE : "+plate+" SPOON : "+spoon+" BOTTLE : "+bottle);
        holder.description.setText(description);


    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public class SalesVH extends RecyclerView.ViewHolder {
        ImageView employeeImage;
        TextView returnDate,description,employee;

        public SalesVH(View itemView) {
            super(itemView);
            employeeImage = (ImageView) itemView.findViewById(R.id.employee_img);
            employee =  (TextView) itemView.findViewById(R.id.tv_employee);
            returnDate = (TextView) itemView.findViewById(R.id.tv_date);
            description = (TextView) itemView.findViewById(R.id.tv_description);
        }
    }

}
