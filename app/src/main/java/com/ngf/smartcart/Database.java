package com.ngf.smartcart;

import java.util.Hashtable;

/**
 * Created by Roni on 08/04/2015.
 */
public class Database {
    public class CashAddress {
        String mHost;
        int mPort;

        public CashAddress(String host, int port)
        {
            mHost = host;
            mPort = port;
        }

        public String getHost() {
            return mHost;
        }

        public int getPort() {
            return mPort;
        }
    }

    private static final String BEACON_1_UUID = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
    private static final int BEACON_1_ID2 = 18847;
    private static final int BEACON_1_ID3 = 17390;

    public static final int DEFAULT_CASH = 150994943;

    Hashtable<String, Product> mMarketDBKeyName = new Hashtable<String, Product>();
    Hashtable<Integer, Product> mMarketDBKeyID = new Hashtable<Integer, Product>();

    Hashtable<Integer, CashAddress> mCashAddresses = new Hashtable<>();

    public Database() {
        init();
    }

    private void init() {
        mCashAddresses.put(DEFAULT_CASH, new CashAddress("10.10.0.158", 5030));

        /**
         * Create market's products.
         */
        Product milk = new Product("Milk", 615741915, new BeaconId(BEACON_1_UUID, BEACON_1_ID2, BEACON_1_ID3), 1.50);
        Product coffee = new Product("Coffee", 2, new BeaconId(BEACON_1_UUID, BEACON_1_ID2, BEACON_1_ID3), 3.5);
        Product sugar = new Product("Sugar", 3, new BeaconId(BEACON_1_UUID, BEACON_1_ID2, BEACON_1_ID3), 5.0);
        Product eggs = new Product("Eggs", 4, new BeaconId(BEACON_1_UUID, BEACON_1_ID2, BEACON_1_ID3), 1.00);
        Product cookies = new Product("Cookies", 5, new BeaconId(BEACON_1_UUID, BEACON_1_ID2, BEACON_1_ID3), 10.00);

        /**
         * Holds market's products.
         */
        mMarketDBKeyName.put(milk.mName, milk);
        mMarketDBKeyName.put(coffee.mName, coffee);
        mMarketDBKeyName.put(sugar.mName, sugar);
        mMarketDBKeyName.put(cookies.mName, cookies);
        mMarketDBKeyName.put(eggs.mName, eggs);

        mMarketDBKeyID.put(milk.mTagId, milk);
        mMarketDBKeyID.put(coffee.mTagId, coffee);
        mMarketDBKeyID.put(sugar.mTagId, sugar);
        mMarketDBKeyID.put(cookies.mTagId, cookies);
        mMarketDBKeyID.put(eggs.mTagId, eggs);
    }

    public Product getProductByName(String name) {
        return mMarketDBKeyName.get(name);
    }

    public Product getProductByID(int id) {
        return mMarketDBKeyID.get(id);
    }

    public boolean isCash(int tagId) {
        return mCashAddresses.containsKey(tagId);
    }

    public CashAddress getCash(int id) {
        return mCashAddresses.get(id);
    }
}
