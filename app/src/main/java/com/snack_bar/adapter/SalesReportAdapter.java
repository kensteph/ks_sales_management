package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.SalesReportModel;

import java.util.List;


public class SalesReportAdapter extends RecyclerView.Adapter<SalesReportAdapter.SalesVH> {
    List<SalesReportModel> salesList;
    Context context;
    private DatabaseHelper db;
    public SalesReportAdapter(List<SalesReportModel> salesList, Context context) {
        this.salesList = salesList;
        this.context = context;
    }

    @Override
    public SalesVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sync_sales_row,parent,false);
        return new SalesVH(view);
    }

    @Override
    public void onBindViewHolder(SalesVH holder, @SuppressLint("RecyclerView") final int position) {
        SalesReportModel productReport = salesList.get(position);
        if(productReport.getProductImage().isEmpty()){
            holder.productImage.setImageResource(R.drawable.ic_product_avatar);
        }else{
            Glide.with(context)
                    .load(productReport.getProductImage())
                    .into(holder.productImage);
        }

        holder.productName.setText(productReport.getProductName());
        holder.productQty.setText("Qty : "+productReport.getQuantitySold());
        holder.productPrice.setText("Price : "+productReport.getProductPrice().toString());
        holder.productAmount.setText("Total : "+productReport.getAmountSold().toString());
        holder.productSaleDate.setText("Date : "+productReport.getSaleDate());

    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public class SalesVH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName,productPrice,productQty,productAmount,productSaleDate;

        public SalesVH(View itemView) {
            super(itemView);
            productImage = (ImageView) itemView.findViewById(R.id.productImage);
            productName = (TextView) itemView.findViewById(R.id.tv_product);
            productQty = (TextView) itemView.findViewById(R.id.tv_product_qty);
            productPrice = (TextView) itemView.findViewById(R.id.tv_product_price);
            productAmount = (TextView) itemView.findViewById(R.id.tv_product_total);
            productSaleDate = (TextView) itemView.findViewById(R.id.tv_date);
        }
    }

}
