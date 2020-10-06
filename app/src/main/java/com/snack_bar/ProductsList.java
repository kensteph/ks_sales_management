package com.snack_bar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.snack_bar.adapter.OrderAdapter;
import com.snack_bar.adapter.ProductListAdapter;
import com.snack_bar.database.DatabaseHelper;
import com.snack_bar.model.Item;
import com.snack_bar.model.Order;
import com.snack_bar.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class ProductsList extends AppCompatActivity implements ProductListAdapter.ProductAdapterCallBac, OrderAdapter.IOrderAdapterCallback {
    private List<Item> productList;
    //Holds the data that are added to cart
    private List<Order> orderList;
    RecyclerView recyclerView;
    private TextView txtCount;
    private RelativeLayout rlCart;
    private ProgressDialog dialog;
    private int employeeSelectedID=0;
    private int employeeCashierID=0;
    private int  materialID=1;
    ProductListAdapter adapter;
    OrderAdapter orderAdapter;
    DatabaseHelper db ;
    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        ActionBar actionBar = getSupportActionBar();
        //GET THE INFO FROM THE ACTIVITY
        String EmployeeFullName = getIntent().getStringExtra("EmployeeFullName");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(EmployeeFullName);
        productList = new ArrayList<>();
        orderList = new ArrayList<>();
        helper = new Helper();
        //INIT DB
        db=new DatabaseHelper(this);
        //LOAD COMPONENT
        recyclerView = (RecyclerView) findViewById(R.id.rvProductList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter = new ProductListAdapter(productList, this,ProductsList.this);
        recyclerView.setAdapter(adapter);
        //LOAD PRODUCT FROM DB
        new LoadProductsFromDB().execute();
       // dummyData();
    }

    private void dummyData(){
        productList.add(new Item(1,1,2,"Fanta",150,"https://saudeezagency.com/MyImages/fanta.jpg"));
        productList.add(new Item(2,1,2,"Pepsi",150,"https://saudeezagency.com/MyImages/pepsi.jpg"));
        productList.add(new Item(3,1,2,"Malta H",150,"https://saudeezagency.com/MyImages/malta.jpeg"));
        productList.add(new Item(4,1,2,"Jumex",150,"https://saudeezagency.com/MyImages/jumex.jpg"));
        productList.add(new Item(5,1,2,"Prestige",150,"https://saudeezagency.com/MyImages/prestige.png"));
        productList.add(new Item(6,1,2,"Tampico",150,"https://saudeezagency.com/MyImages/tampico.jpeg"));
        productList.add(new Item(7,1,2,"Sprite",150,"https://saudeezagency.com/MyImages/sprite.jpg"));
        productList.add(new Item(8,1,2,"Spaghetti",150,"https://saudeezagency.com/MyImages/spaghetti.png"));
        productList.add(new Item(9,1,2,"Gatorade",150,"https://saudeezagency.com/MyImages/gatorade.jpg"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cart_menu, menu);
        final View actionCart = menu.findItem(R.id.cart_menu_action).getActionView();
        txtCount = actionCart.findViewById(R.id.txtCount);
        rlCart = (RelativeLayout) actionCart.findViewById(R.id.rlCart);
        rlCart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                displayCart();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onAddProductCallback(Item product) {
        addProductToCart(product,1);
        Log.d("CART","ITEM IN CART : ");
        for (Order order : orderList){
            Item item = order.item;
            Log.d("ADD ITEM","ITEM ADDED : "+order.quantity+" "+item.name);
        }

    }

    @Override
    public void onIncreaseDecreaseCallback() {
        updateOrderTotal();
        updateBadge();
    }

    //LOAD PRODUCTS FROM DB
    public class LoadProductsFromDB extends AsyncTask<Void, Void, List<Item>> {

        @Override
        protected List<Item> doInBackground(Void... voids) {
            return db.getProducts();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Item> products) {
            super.onPostExecute(products);
            for(Item product : products){
                productList.add(product);
            }
            adapter.notifyDataSetChanged();
            Log.d("PRODUCT FOUND IN DB","NB : "+productList.size());
        }
    }

    //============================================  OPERATION RELATIVE AU CART ==================================================
    private void addProductToCart(Item item, int quantity) {
        boolean isAdded = false;
Log.d("ADD ITEM","ITEM ADDED : "+quantity+" "+item.name);
        for (Order order : orderList)
        {
            if (order.item.id == item.id)
            {
                //if item already added to cart, dont add new order
                //just add the quantity
                isAdded = true;
                order.quantity += quantity;
                order.extendedPrice += item.unitPrice;
                break;
            }
        }

        //if item's not added yet
        if (!isAdded)
        {
            orderList.add(new Order(item, quantity));
        }

//        orderAdapter.notifyDataSetChanged();
//        rvOrder.smoothScrollToPosition(orderList.size() - 1);
        updateOrderTotal();
        updateBadge();
    }
    private void updateBadge() {
        if (orderList.size() == 0)
        {
            txtCount.setVisibility(View.INVISIBLE);
            //btnCompleteOrder.setVisibility(View.INVISIBLE);
        } else
        {
            txtCount.setVisibility(View.VISIBLE);
            txtCount.setText(String.valueOf(orderList.size()));
//            if(employeeSelectedID!=0) {
//                btnCompleteOrder.setVisibility(View.VISIBLE);
//            }
        }
    }
    private double getOrderTotal() {
        double total = 0.0;

        for (Order order : orderList)
        {
            total += order.extendedPrice;
        }

        return total;
    }
    private void updateOrderTotal() {
        double total = getOrderTotal();
//        txtTotal.setText(String.format("%.2f", total));
//        totalCart = total;
    }
    private void displayCart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductsList.this);
        View view = getLayoutInflater().inflate(R.layout.cart_layout, null);
        final RecyclerView rvCart = view.findViewById(R.id.rvOrders);
        final Button btnSaveOrder = view.findViewById(R.id.btnSaveOrder);
        rvCart.setHasFixedSize(true);
        rvCart.setLayoutManager(new GridLayoutManager(this,2));
        orderAdapter = new OrderAdapter(orderList,this,ProductsList.this);
        rvCart.setAdapter(orderAdapter);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        //dialog.getWindow().getAttributes().windowAnimations = R.style.DetailDialogAnimation;
        dialog.show();
        btnSaveOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new SaveOrder().execute();
            }
        });
    }
    //Represents an asynchronous task used to complete the order
    public class SaveOrder extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            employeeSelectedID=helper.getRandomId(100,4000);
            boolean rep = db.saveSaleDetails(orderList,materialID,employeeSelectedID,employeeCashierID,0.0);
            return rep;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress("Traitement de la requete...",false);
            clearAll();
            showMessage(true,"Commande enregistrée avec succès...");
        }

        @Override
        protected void onCancelled()
        {
            showProgress("Traitement de la requete...",false);
            showMessage(false,"Une erreur est survenue.Veuillez réessayer.");
        }
    }
    // Shows the progress
    private void showProgress(String msg,boolean show) {
        if (dialog == null)
        {
            dialog = new ProgressDialog(ProductsList.this);
            dialog.setMessage(msg);
            dialog.setCancelable(false);
        }

        if (show)
        {
            dialog.show();
        } else
        {
            dialog.dismiss();
        }
    }
    //Clears all orders from the cart
    private void clearAll() {
        orderList.clear();
        orderAdapter.notifyDataSetChanged();
        updateBadge();
        updateOrderTotal();
    }
    //Shows a message by using Snackbar
    private void showMessage(Boolean isSuccessful, String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        if (isSuccessful)
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(ProductsList.this, R.color.colorAccent));
        } else
        {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(ProductsList.this, R.color.design_default_color_error));
        }

        snackbar.show();
    }
}