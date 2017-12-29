package com.ngf.smartcart;

/**
 * Created by Liat Ginosar on 07/04/2015.
 */
public interface BeaconFinder {
    enum DistanceState {
        UNKNOWN,
        CLOSER,
        SAME,
        FARTHER
    }

    public interface DistanceListener
    {
        void updateDistance(double distance, DistanceState state);
    }

    void setListener(DistanceListener listener);
    void setScanPeriod(long period);
    void setWaitPeriod(long period);
    void setTargetBeacon(BeaconId beacon);
    void start();
    void stop();
}

