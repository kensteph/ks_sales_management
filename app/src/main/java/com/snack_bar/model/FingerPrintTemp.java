package com.snack_bar.model;

public class FingerPrintTemp {
    private String fingerPrintImageBase64;
    private String fingerPrintTemplateBase64;
    private String finger;
    private int employeeId;

    public String getFinger() {
        return finger;
    }

    public void setFinger(String finger) {
        this.finger = finger;
    }
    public String getFingerPrintImageBase64() {
        return fingerPrintImageBase64;
    }

    public void setFingerPrintImageBase64(String fingerPrintImageBase64) {
        this.fingerPrintImageBase64 = fingerPrintImageBase64;
    }

    public String getFingerPrintTemplateBase64() {
        return fingerPrintTemplateBase64;
    }

    public void setFingerPrintTemplateBase64(String fingerPrintTemplateBase64) {
        this.fingerPrintTemplateBase64 = fingerPrintTemplateBase64;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
}
