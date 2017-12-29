package com.ngf.smartcart;

/**
 * Created by Liat Ginosar on 07/04/2015.
 */
public class BeaconId {
    private String mUuid;
    private int mMajor, mMinor;

    BeaconId(String uuid, int major, int minor) {
        mUuid = uuid;
        mMajor = major;
        mMinor = minor;
    }

    public String getUuid() {
        return mUuid;
    }

    public int getMajor()
    {
        return mMajor;
    }

    public int getMinor()
    {
        return mMinor;
    }
}
