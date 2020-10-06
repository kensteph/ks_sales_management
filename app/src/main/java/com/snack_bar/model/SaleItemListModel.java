package com.snack_bar.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaleItemListModel {
    private String saleDate,saleDescription;
    private int materialId,employee,cashier;
    private String employeeName;
    private double total;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    private List<Order>item;

    public List<Order> getItem() {
        return item;
    }

    public void setItem(List<Order> item) {
        this.item = item;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }



    @SerializedName("saleDate")
    private int saleId;
    @SerializedName("saleId")
    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    private boolean isExpandable;

    public SaleItemListModel() {
        this.isExpandable=false;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public SaleItemListModel(String saleDate, int employee, int cashier, String saleDescription) {
        this.saleDate = saleDate;
        this.employee = employee;
        this.cashier = cashier;
        this.saleDescription = saleDescription;
        this.isExpandable=false;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public int getEmployee() {
        return employee;
    }

    public void setEmployee(int employee) {
        this.employee = employee;
    }

    public int getCashier() {
        return cashier;
    }

    public void setCashier(int cashier) {
        this.cashier = cashier;
    }

    public String getSaleDescription() {
        return saleDescription;
    }

    public void setSaleDescription(String saleDescription) {
        this.saleDescription = saleDescription;
    }

    @Override
    public String toString() {
        return "SaleItemListModel{" +
                "saleDate='" + saleDate + '\'' +
                ", employee='" + employee + '\'' +
                ", cashier='" + cashier + '\'' +
                ", saleDescription='" + saleDescription + '\'' +
                '}';
    }
}
