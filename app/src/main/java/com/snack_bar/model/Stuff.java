package com.snack_bar.model;

public class Stuff {
    private int stuffId;
    private int qty;
    private String stuffName;
    private boolean isSelected;
    private String urlImage;


    public Stuff(int stuffId, String stuffName,int qty,String urlImage) {
        this.stuffId = stuffId;
        this.stuffName = stuffName;
        this.urlImage = urlImage;
        this.isSelected = false;
        this.qty=qty;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getStuffId() {
        return stuffId;
    }

    public void setStuffId(int stuffId) {
        this.stuffId = stuffId;
    }

    public String getStuffName() {
        return stuffName;
    }

    public void setStuffName(String stuffName) {
        this.stuffName = stuffName;
    }

    public String getUrlImage() {
        return urlImage;
    }
    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}
