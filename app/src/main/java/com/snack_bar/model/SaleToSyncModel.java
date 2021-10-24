package com.snack_bar.model;

public class SaleToSyncModel {
    int productId;
    int employeeId;
    int qtyPurchase;
    double price;
    String dateSale;
    public SaleToSyncModel(int productId, int employeeId, int qtyPurchase, double price,String dateSale) {
        this.productId = productId;
        this.employeeId = employeeId;
        this.qtyPurchase = qtyPurchase;
        this.price = price;
        this.dateSale=dateSale;
    }

    public String getDateSale() {
        return dateSale;
    }

    public void setDateSale(String dateSale) {
        this.dateSale = dateSale;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getQtyPurchase() {
        return qtyPurchase;
    }

    public void setQtyPurchase(int qtyPurchase) {
        this.qtyPurchase = qtyPurchase;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
