package com.snack_bar.model;

public class StuffReturnModel {
    private int returnId;
    private String dateReturn;
    private int employeeId;
    private String fullName;
    private String plateReturn;
    private String spoonReturn;
    private String bottleReturn;

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateReturn() {
        return dateReturn;
    }

    public void setDateReturn(String dateReturn) {
        this.dateReturn = dateReturn;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getPlateReturn() {
        return plateReturn;
    }

    public void setPlateReturn(String plateReturn) {
        this.plateReturn = plateReturn;
    }

    public String getSpoonReturn() {
        return spoonReturn;
    }

    public void setSpoonReturn(String spoonReturn) {
        this.spoonReturn = spoonReturn;
    }

    public String getBottleReturn() {
        return bottleReturn;
    }

    public void setBottleReturn(String bottleReturn) {
        this.bottleReturn = bottleReturn;
    }
}
