package com.snack_bar.model;

public class Stuff {
    private int stuffId;
    private String stuffName;
    private int drawableImage;
    private boolean isSelected;


    public Stuff(int stuffId, String stuffName, int drawableImage, boolean isSelected) {
        this.stuffId = stuffId;
        this.stuffName = stuffName;
        this.drawableImage = drawableImage;
        this.isSelected = isSelected;
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

    public int getDrawableImage() {
        return drawableImage;
    }

    public void setDrawableImage(int drawableImage) {
        this.drawableImage = drawableImage;
    }
}
