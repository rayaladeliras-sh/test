package com.stubhub.domain.inventory.listings.v2.entity;

import java.util.Calendar;

public class DM {
    private int id;
    private String name;
    private Calendar estimatedDeliveryTime;


    public DM(int id, String name, Calendar estimatedDeliveryTime) {
        this.id = id;
        this.name = name;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(Calendar estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
}
