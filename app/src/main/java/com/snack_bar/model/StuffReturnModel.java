package com.snack_bar.model;

public class StuffReturnModel {
    private int returnId;
    private String dateReturn;
    private int employeeId;
    private String fullName;
    private int stuffReturnId;
    private String stuffName;
    private int stuffQty;


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

    public int getStuffReturnId() {
        return stuffReturnId;
    }

    public void setStuffReturnId(int stuffReturnId) {
        this.stuffReturnId = stuffReturnId;
    }

    public String getStuffName() {
        return stuffName;
    }

    public void setStuffName(String stuffName) {
        this.stuffName = stuffName;
    }

    public int getStuffQty() {
        return stuffQty;
    }

    public void setStuffQty(int stuffQty) {
        this.stuffQty = stuffQty;
    }
}
