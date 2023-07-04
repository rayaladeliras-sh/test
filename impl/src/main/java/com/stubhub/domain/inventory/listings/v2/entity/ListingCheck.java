package com.stubhub.domain.inventory.listings.v2.entity;

public class ListingCheck {

    private boolean isListed = false;
    private String message;
    public boolean getIsListed() {
        return isListed;
    }
    public void setIsListed(boolean isListed) {
        this.isListed = isListed;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
}
