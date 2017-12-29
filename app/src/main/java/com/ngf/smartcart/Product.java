package com.ngf.smartcart;

/**
 * Created by Roni on 08/04/2015.
 */
public class Product {
    String mName;
    int mTagId;
    boolean mSelected;
    BeaconId mBeaconId;
    double mPrice;

    public Product(String name, int id, BeaconId beaconID, double price) {
        this.mName = name;
        this.mTagId = id;
        this.mSelected = false;
        this.mBeaconId = beaconID;
        this.mPrice = price;
    }

    public Product(String name, int id, boolean selected, BeaconId beaconID, double price) {
        this.mName = name;
        this.mTagId = id;
        this.mSelected = selected;
        this.mBeaconId = beaconID;
        this.mPrice = price;
    }

    public Product(Product cpy) {
        this.mName = cpy.mName;
        this.mTagId = cpy.mTagId;
        this.mSelected = cpy.mSelected;
        this.mBeaconId = cpy.mBeaconId;
        this.mPrice = cpy.mPrice;
    }

    public String toString() {
        return this.mName + "  $" + this.mPrice ;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Product)) {
            return false;
        } else {
            Product that = (Product) obj;
            return this.mTagId == that.mTagId;
        }
    }

    public int hashCode() {
        return this.mName.hashCode();
    }
    public boolean getSelected() {
        return this.mSelected;
    }
}
