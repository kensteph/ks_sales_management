package com.snack_bar.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snack_bar.R;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Item;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductsVH> {
    List<Item> productsList;
    Context context;
    private Activity activity; //The activity we want to communicate with
    private DatabaseHelper db;
    private ProductAdapterCallBac productCallback;

    public ProductListAdapter(List<Item> productsList, Context context,Activity activity) {
        this.productsList = productsList;
        this.context = context;
        this.activity=activity;
        productCallback = (ProductAdapterCallBac) activity; //THE PRODUCT SELECTED IN THE ACTIVITY
    }

    //MY INTERFACE TO INSURE THE COMMUNICATION
    public interface ProductAdapterCallBac{
        void onAddProductCallback(Item product);
    }

    @Override
    public ProductsVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_row,parent,false);
        return new ProductsVH(view);
    }

    @Override
    public void onBindViewHolder(ProductsVH holder, @SuppressLint("RecyclerView") final int position) {
        //db=new DatabaseHelper(context);
        Item product = productsList.get(position);
        if(product.url.isEmpty()){
            holder.productImage.setImageResource(R.drawable.ic_product_avatar);
        }else{
            Glide.with(context)
                    .load(product.url)
                    .into(holder.productImage);
        }

        holder.productName.setText(product.name);
        holder.productPrice.setText(String.format("%.2f", product.unitPrice));
        //CLICK ON THE CARD
        holder.cardItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(productCallback != null){
                    //TELL THE FUNCTION IN THE ACTIVITY YOU CAN ADD THIS PRODUCT TO CART
                    productCallback.onAddProductCallback(product);
                }
            }
        });
        //CLICK ON THE IMAGE
        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(productCallback != null){
                    //TELL THE FUNCTION IN THE ACTIVITY YOU CAN ADD THIS PRODUCT TO CART
                    productCallback.onAddProductCallback(product);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public class ProductsVH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName,productPrice;
        CardView cardItem;

        public ProductsVH(View itemView) {
            super(itemView);
            productImage = (ImageView) itemView.findViewById(R.id.productImage);
            productName = (TextView) itemView.findViewById(R.id.productName);
            productPrice = (TextView) itemView.findViewById(R.id.productPrice);
            cardItem = (CardView) itemView.findViewById(R.id.card_view);
        }
    }


}
