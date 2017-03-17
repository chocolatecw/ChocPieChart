package com.xysk.library.bean;

/**
 * Created by Administrator on 2017/3/15.
 */
public class PieData {
    private String name;
    private float proportion;
    private int color;

    public PieData() {

    }

    public PieData(String name, float proportion, int color) {
        this.name = name;
        this.proportion = proportion;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getProportion() {
        return proportion;
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
    }
}
