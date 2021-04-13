package com.snack_bar.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.iv_stuff.setImageResource(stuff.getDrawableImage());
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

    @Override
    public int getItemCount() {
        return stuffs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_stuff_name;
        ImageView iv_stuff,iv_check;
        CardView cv_stuff;
        LinearLayout ll_stuff_to_return;
        ViewHolder(View itemView) {
            super(itemView);
            tv_stuff_name = (TextView) itemView.findViewById(R.id.tv_stuff_name);
            iv_stuff = (ImageView) itemView.findViewById(R.id.iv_stuff);
            iv_check = (ImageView) itemView.findViewById(R.id.iv_check);
            cv_stuff= (CardView) itemView.findViewById(R.id.card_view_stuff);
            ll_stuff_to_return = (LinearLayout) itemView.findViewById(R.id.ll_stuff_to_return);
        }
    }
}
