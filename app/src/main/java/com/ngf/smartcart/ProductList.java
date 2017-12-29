package com.ngf.smartcart;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roni on 08/04/2015.
 */
public class ProductList extends ArrayList<Product> {
    private final String PREFS_FILE = "scart";
    private final String PREFS_PREFIX = "products";

    private Database mDatabase = new Database();

    public ProductList() {

    }

    public void load(Context mContext, boolean allowSelect) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_FILE, 0);

        clear();
        int size = prefs.getInt(PREFS_PREFIX + "_size", 0);

        for (int i = 0; i < size; i++) {
            int id = prefs.getInt(PREFS_PREFIX + "_id_" + i, 0);
            boolean selected = prefs.getBoolean(PREFS_PREFIX + "_sel_" + i, false);
            Product p = getProductById(id);

            if (p != null) {
                Product prod = new Product(p);

                prod.mSelected = allowSelect ? selected : false;

                add(prod);
            }
        }
    }

    public boolean store(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(PREFS_PREFIX + "_size", size());

        for (int i = 0; i < size(); i++) {
            editor.putInt(PREFS_PREFIX + "_id_" + i, get(i).mTagId);
            editor.putBoolean(PREFS_PREFIX + "_sel_" + i, get(i).getSelected());
        }

        return editor.commit();
    }

    public Product getProductById(int id) {
        return mDatabase.getProductByID(id);
    }

    public Database getDatabase() {
        return mDatabase;
    }

    public List<Product> getSelectedProducts() {
        List<Product> products = new ArrayList<Product>();

        for (Product p : this) {
            if (p.getSelected()) {
                products.add(p);
            }
        }

        return products;
    }

    public void resetSelectedProducts() {
        for (Product p : this) {
            if (p.getSelected()) {
                p.mSelected = false;
            }
        }
    }
}
