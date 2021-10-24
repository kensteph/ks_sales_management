package com.snack_bar.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snack_bar.R;
import com.snack_bar.model.Stuff;

import java.util.List;

public class StuffAdapter extends RecyclerView.Adapter<StuffAdapter.ViewHolder >  {
    private List<Stuff> stuffs;
    private Context context;
    private Activity activity;
    private StuffCallBack stuffCallBack;

    //MY INTERFACE TO INSURE THE COMMUNICATION
    public interface StuffCallBack{
        void onSelectStuff(Stuff stuff);
        void onIncreaseOrDecrease(Stuff stuff,String action);
    }

    public StuffAdapter(Context context,Activity activity ,List<Stuff> stuffs) {
        this.stuffs = stuffs;
        this.context=context;
        this.activity=activity;
        stuffCallBack = (StuffCallBack) activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stuff_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Stuff stuff = stuffs.get(position);
        holder.tv_stuff_name.setText(stuff.getStuffName().toUpperCase());
                if(stuff.getUrlImage().isEmpty()){
                    holder.iv_stuff.setImageResource(R.drawable.ic_product_avatar);
                }else{
                    Glide.with(context)
                            .load(stuff.getUrlImage())
                            .into(holder.iv_stuff);
                }

        holder.tv_qte_stuff_return.setText( ""+ stuff.getQty());
        if(stuff.isSelected()){
            holder.iv_check.setImageResource(R.drawable.check);
        }else{
            holder.iv_check.setImageResource(0);

        }
        holder.ll_stuff_to_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDeselect(stuff,position);
            }
        });
        holder.cv_stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDeselect(stuff,position);
            }
        });
        holder.iv_stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDeselect(stuff,position);
            }
        });
        holder.iv_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDeselect(stuff,position);
            }
        });

        holder.ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseOrDecreaseQty(stuff,position,"Add");
            }
        });
        holder.ib_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseOrDecreaseQty(stuff,position,"Minus");
            }
        });
    }

    private void selectDeselect(Stuff stuff,int position){
        String msg = "";
        if(stuff.isSelected()){
            msg="STUFF DESELECTED "+stuff.getStuffName().toUpperCase();
            stuff.setSelected(false);
        }else{
            msg="STUFF SELECTED "+stuff.getStuffName().toUpperCase();
            stuff.setSelected(true);
        }
        //Toast.makeText(view.getContext(),msg, Toast.LENGTH_LONG).show();
        notifyItemChanged(position);
        stuffCallBack.onSelectStuff(stuff);
    }

    private void increaseOrDecreaseQty(Stuff stuff,int position,String what){
        String msg = what+" "+stuff.getStuffName().toUpperCase();
        int oldQty = stuff.getQty();
        if(what=="Add"){
            int newQty = oldQty+1;
            stuff.setQty(newQty);
        }else{
            if(oldQty>1){
                int newQty = oldQty-1;
                stuff.setQty(newQty);
            }
        }
        //Toast.makeText(context,what, Toast.LENGTH_LONG).show();
        notifyItemChanged(position);
        //stuffCallBack.onSelectStuff(stuff);
    }

    @Override
    public int getItemCount() {
        return stuffs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_stuff_name,tv_qte_stuff_return;
        ImageView iv_stuff,iv_check;
        ImageButton ib_add,ib_minus;
        CardView cv_stuff;
        LinearLayout ll_stuff_to_return;
        ViewHolder(View itemView) {
            super(itemView);
            tv_stuff_name = (TextView) itemView.findViewById(R.id.tv_stuff_name);
            tv_qte_stuff_return = (TextView) itemView.findViewById(R.id.tv_qte_stuff_return);
            iv_stuff = (ImageView) itemView.findViewById(R.id.iv_stuff);
            iv_check = (ImageView) itemView.findViewById(R.id.iv_check);
            cv_stuff= (CardView) itemView.findViewById(R.id.card_view_stuff);
            ll_stuff_to_return = (LinearLayout) itemView.findViewById(R.id.ll_stuff_to_return);
            ib_add = (ImageButton) itemView.findViewById(R.id.ib_add);
            ib_minus = (ImageButton) itemView.findViewById(R.id.ib_minus);
        }
    }
}
