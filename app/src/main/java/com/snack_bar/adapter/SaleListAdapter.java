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
        holder.employee.setText(sales.getEmployeeName());
        holder.cashier.setText(""+sales.getCashier());
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

    private void deleteRecord(int position,View v)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(v.getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(context);
        }
        builder.setCancelable(false);
        builder.setTitle("Suppression vente")
                .setMessage("Voulez-vouz supprimer ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SaleItemListModel sales = salesList.get(position);
                        db.deleteSale(sales.getSaleId());
                        //new DeleteSale().execute(sales.getSaleId());
                        salesList.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context,"Vente "+sales.getSaleDate(),Toast.LENGTH_LONG).show();
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
