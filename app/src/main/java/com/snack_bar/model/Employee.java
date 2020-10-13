package com.snack_bar.model;

import java.io.Serializable;

public class Employee implements Serializable {
    private int employee_id,employee_entreprise;
    private String employee_code,employee_prenom,employee_nom,full_name;

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public Employee() {
    }

    public Employee(int employee_id, int employee_entreprise, String employee_code, String employee_prenom, String employee_nom) {
        this.employee_id = employee_id;
        this.employee_entreprise = employee_entreprise;
        this.employee_code = employee_code;
        this.employee_prenom = employee_prenom;
        this.employee_nom = employee_nom;
        this.full_name=employee_prenom+" "+employee_nom;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getEmployee_entreprise() {
        return employee_entreprise;
    }

    public void setEmployee_entreprise(int employee_entreprise) {
        this.employee_entreprise = employee_entreprise;
    }

    public String getEmployee_code() {
        return employee_code;
    }

    public void setEmployee_code(String employee_code) {
        this.employee_code = employee_code;
    }

    public String getEmployee_prenom() {
        return employee_prenom;
    }

    public void setEmployee_prenom(String employee_prenom) {
        this.employee_prenom = employee_prenom;
    }

    public String getEmployee_nom() {
        return employee_nom;
    }

    public void setEmployee_nom(String employee_nom) {
        this.employee_nom = employee_nom;
    }
}
