package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.SaleItemListModel;

import java.util.List;


public class SaleListAdapter extends RecyclerView.Adapter<SaleListAdapter.SalesVH> {
    List<SaleItemListModel> salesList;
    Context context;
    private DatabaseHelper db;
    public SaleListAdapter(List<SaleItemListModel> salesList, Context context) {
        this.salesList = salesList;
        this.context = context;
    }

    @Override
    public SalesVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sale_list,parent,false);
        return new SalesVH(view);
    }

    @Override
    public void onBindViewHolder(SalesVH holder, @SuppressLint("RecyclerView") final int position) {
        db=new DatabaseHelper(context);
        SaleItemListModel sales = salesList.get(position);
        holder.saleDate.setText(sales.getSaleDate());
        String employeeInfo = sales.getEmployeeName();
        holder.employee.setText(employeeInfo.toUpperCase());
        holder.cashier.setText("");
        holder.description.setText(sales.getSaleDescription());
        boolean isExpandable = sales.isExpandable();
        holder.myExpandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public class SalesVH extends RecyclerView.ViewHolder {
        TextView saleDate, employee, cashier, description;
        ImageButton btnDelete;
        LinearLayout linearLayout;
        RelativeLayout myExpandableLayout;

        public SalesVH(View itemView) {
            super(itemView);
            saleDate = itemView.findViewById(R.id.txtSaleDate);
            employee = itemView.findViewById(R.id.txtEmployee);
            cashier = itemView.findViewById(R.id.txtCashier);
            description = itemView.findViewById(R.id.txtDescription);

            btnDelete = itemView.findViewById(R.id.btnDeleteSale);

            linearLayout = itemView.findViewById(R.id.llSales);
            myExpandableLayout = itemView.findViewById(R.id.expandable_layout);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SaleItemListModel sales = salesList.get(getAdapterPosition());
                    sales.setExpandable(!sales.isExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });


        }
    }

}
