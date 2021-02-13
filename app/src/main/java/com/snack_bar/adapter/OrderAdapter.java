package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Item;
import com.snack_bar.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrdersVH> {
    List<Order> ordersList;
    Context context;
    private Activity activity; //The activity we want to communicate with
    private DatabaseHelper db;

    private IOrderAdapterCallback orderCallback;
    //MY INTERFACE TO INSURE THE COMMUNICATION
    public interface IOrderAdapterCallback
    {
        void onIncreaseDecreaseCallback();
    }

    public OrderAdapter(List<Order> ordersList, Context context,Activity activity) {
        this.ordersList = ordersList;
        this.context = context;
        this.activity=activity;
        //THE ORDER SELECTED IN THE ACTIVITY
        orderCallback = (IOrderAdapterCallback) activity;
    }

    @Override
    public OrdersVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_order_item,parent,false);
        return new OrdersVH(view);
    }

    @Override
    public void onBindViewHolder(OrdersVH holder, @SuppressLint("RecyclerView") final int position) {
        //db=new DatabaseHelper(context);
        Order order = ordersList.get(position);
        Item item = order.item;
        Glide.with(context)
                .load(item.url)
                .into(holder.productImage);
        holder.productName.setText(item.name);
        holder.qty.setText(String.valueOf(order.quantity));
        holder.price.setText(String.valueOf(item.unitPrice));
        //CLICK ON THE INCREASE BUTTON
        holder.btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //INCREASE THE QTY
                int newQty = order.quantity+1;
                //NEW SUBTOTAL
                Double subTotal = newQty * order.item.unitPrice;
                order.quantity=newQty;
                order.extendedPrice=subTotal;

                holder.qty.setText(String.valueOf(newQty));
                notifyDataSetChanged();
                //TELL THE FUNCTION IN THE ACTIVITY YOU CAN INCREASE
                orderCallback.onIncreaseDecreaseCallback();
            }
        });

        //CLICK ON THE DECREASE BUTTON
        holder.btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //INCREASE THE QTY
                int newQty = order.quantity-1;
                //NEW SUBTOTAL
                Double subTotal = newQty * order.item.unitPrice;
                order.quantity=newQty;
                order.extendedPrice=subTotal;

                holder.qty.setText(String.valueOf(newQty));
                //IF THE NUMBER BECOME 0 THEN REMOVE THIS ITEM FROM CART
                if (order.quantity == 0){
                    ordersList.remove(position);
                }
                notifyDataSetChanged();
                //TELL THE FUNCTION IN THE ACTIVITY YOU CAN INCREASE
                orderCallback.onIncreaseDecreaseCallback();
            }
        });
    }
    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public class OrdersVH extends RecyclerView.ViewHolder {
        ImageButton btnIncrease,btnDecrease;
        ImageView productImage;
        TextView productName,qty,price;

        public OrdersVH(View itemView) {
            super(itemView);
            productImage = (ImageView) itemView.findViewById(R.id.productImageCart);
            productName = (TextView) itemView.findViewById(R.id.productNameCart);
            btnIncrease = (ImageButton) itemView.findViewById(R.id.btnIncrease);
            btnDecrease = (ImageButton) itemView.findViewById(R.id.btnDecrease);
            qty = (TextView) itemView.findViewById(R.id.qty);
            price = (TextView) itemView.findViewById(R.id.productPrice);
        }
    }


}

