package com.snack_bar.model;

import com.machinezoo.sourceafis.FingerprintTemplate;

import java.io.Serializable;

public class FingerPrint implements Serializable {
    private int employeeId;
    private byte[] fingerPrintByteArray;
    private byte[] fingerPrintTemplate;

    public byte[] getFingerPrintTemplate() {
        return fingerPrintTemplate;
    }

    public void setFingerPrintTemplate(byte[] fingerPrintTemplate) {
        this.fingerPrintTemplate = fingerPrintTemplate;
    }

    private String employeeFullName,employeeCode;

    public String getEmployeeFullName() {
        return employeeFullName;
    }

    public void setEmployeeFullName(String employeeFullName) {
        this.employeeFullName = employeeFullName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public FingerPrint() {
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public byte[] getFingerPrintByteArray() {
        return fingerPrintByteArray;
    }

    public void setFingerPrintByteArray(byte[] fingerPrintByteArray) {
        this.fingerPrintByteArray = fingerPrintByteArray;
    }
}
