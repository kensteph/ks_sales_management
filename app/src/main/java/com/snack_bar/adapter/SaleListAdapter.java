package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.snack_bar.R;
import com.snack_bar.SalesListActivity;
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
//        if(employeeInfo.length()>30){
//            employeeInfo = employeeInfo.substring(0,29)+"...";
//        }
        holder.employee.setText(employeeInfo.toUpperCase());
        holder.cashier.setText("");
        holder.description.setText(sales.getSaleDescription());
        boolean isExpandable = sales.isExpandable();
        holder.myExpandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRecord(position,view);
            }
        });
    }

    private void deleteRecord(int position,View v) {
        SaleItemListModel sales = salesList.get(position);
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(v.getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(context);
        }
        builder.setCancelable(false);
        builder.setTitle("Sale Deletion "+sales.getSaleId())
                .setMessage("Do you want to delete this sale ? \n\n"+sales.getEmployeeName()+"\n"+sales.getSaleDescription())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        db.deleteSale(sales.getSaleId());
                        salesList.remove(position);
                        notifyDataSetChanged();
                        int nbSales = salesList.size();
                        String text = "";
                        if(nbSales >1){
                            text = nbSales+" Sales to Sync";
                        }else{
                            if(nbSales==0){
                                text = "No Sale to Sync";
                                SalesListActivity.btnSynchronizeSales.setEnabled(false);
                            }else{
                                text = nbSales+" Sale to Sync";
                            }
                        }
                        SalesListActivity.btnSynchronizeSales.setText(text);
                        Toast.makeText(context,"SALE DELETED ID : "+sales.getSaleId(),Toast.LENGTH_LONG).show();
                        Log.d("DELETE", "onClick: "+sales.toString());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing
                    }
                })
                .show();
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

//DELETE SALE IN THE BACKGROUND
public class DeleteSale extends AsyncTask<Integer, Void, Boolean>
{

    @Override
    protected Boolean doInBackground(Integer... integers) {
        return db.deleteSale(integers[0]);
    }


}


}
