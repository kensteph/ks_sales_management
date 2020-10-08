package com.snack_bar.model;

public class EmployeeFingerTemplate {
    private int employeeId;
    private byte[] fingerTemplate;

    public EmployeeFingerTemplate(int employeeId, byte[] fingerTemplate) {
        this.employeeId = employeeId;
        this.fingerTemplate = fingerTemplate;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public byte[] getFingerTemplate() {
        return fingerTemplate;
    }

    public void setFingerTemplate(byte[] fingerTemplate) {
        this.fingerTemplate = fingerTemplate;
    }
}
