package com.ngf.smartcart;
import android.content.Context;
import android.os.RemoteException;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;
/**
 * Created by Liat Ginosar on 07/04/2015.
 */
public class EstimoteBeaconFinder implements BeaconFinder {

    protected double mOldDistance = -1;
    protected double mCurrDistance = -1;
    protected DistanceListener mBeaconDist;
    public BeaconManager mBeaconManager;
    private BeaconId mBeaconId;
    private long mScanPeriod;
    private long mWaitTime;
    private Region mRegion;
    private double[] lastDistances;
    private int runIndex = 0;
    private int mAvgCount;

    private final double DIST_EPSILON = 0.4;

    public EstimoteBeaconFinder(Context context, int averageCount) {
        mBeaconDist = null;
        mScanPeriod = 1000;
        mWaitTime = 0;
        mAvgCount = (averageCount < 1) ? 1 : averageCount;
        lastDistances = new double[mAvgCount];

        for(int i = 0; i < mAvgCount; i++){
            lastDistances[i] = -1;
        }

        mBeaconId = new BeaconId("00000000-0000-0000-0000-000000000000", 0, 0);

        mBeaconManager = new BeaconManager(context);
        mBeaconManager.setForegroundScanPeriod(mScanPeriod, mWaitTime);
    }

    public void setListener(DistanceListener listener) {
        mBeaconDist = listener;
    }

    public void setScanPeriod(long period) {
        mScanPeriod = period;
        mBeaconManager.setForegroundScanPeriod(period, mWaitTime);
    }

    public void setWaitPeriod(long period) {
        mWaitTime = period;
        mBeaconManager.setForegroundScanPeriod(mScanPeriod, period);
    }

    public void setTargetBeacon(BeaconId beacon) {
        mBeaconId = beacon;
    }


    public double calcAvgDistance(){
        int avgDistance = 0;
        double sum = 0, count = 0;

        for (int i = 0; i < Math.min(runIndex, mAvgCount); i ++)
        {
            if (lastDistances[i] > 0) {
                sum += lastDistances[i];
                count++;
            }
        }

        if (count == 0) {
            return -1;
        }

        return sum / count;
    }
    private void updateDistance(Beacon beacon) {
        BeaconFinder.DistanceState ic = BeaconFinder.DistanceState.UNKNOWN;

        if (beacon == null) {
            mBeaconDist.updateDistance(-1, ic);
        }
        else {
            double distance = Utils.computeAccuracy(beacon);
            if (runIndex < mAvgCount)
            {
                lastDistances[runIndex] = distance;
                runIndex = runIndex + 1;
            }
            else {
                for (int i = 0; i < mAvgCount - 1; i++) {
                    lastDistances[i] = lastDistances[i + 1];
                }
                lastDistances[mAvgCount - 1] = distance;
            }

            if (this.mOldDistance < 0) {
                this.mOldDistance = mCurrDistance;
            }

            this.mCurrDistance = distance;

            double avgDistance = calcAvgDistance();

            if (avgDistance < 0 || mOldDistance < 0) {
                ic = BeaconFinder.DistanceState.UNKNOWN;
            } else if (avgDistance < mOldDistance - DIST_EPSILON) {
                ic = BeaconFinder.DistanceState.CLOSER;
                mOldDistance = avgDistance;
            } else if (avgDistance > mOldDistance + DIST_EPSILON) {
                ic = BeaconFinder.DistanceState.FARTHER;
                mOldDistance = avgDistance;
            } else {
                ic = BeaconFinder.DistanceState.SAME;
            }

            if (mBeaconDist != null) {
                mBeaconDist.updateDistance(avgDistance, ic);
            }
        }
    }

    private boolean compareBeacon(Beacon beacon) {
        if (!beacon.getProximityUUID().equals(mBeaconId.getUuid()))
        {
            return false;
        }

        if (beacon.getMajor() != mBeaconId.getMajor())
        {
            return false;
        }

        if (beacon.getMinor() != mBeaconId.getMinor()) {
            return false;
        }

        return true;
    }

    public void start() {
        //mRegion = new Region("regionOfNewBeacon", mBeaconId.getUuid(), mBeaconId.getMajor(), mBeaconId.getMinor());
        mRegion = new Region("regionOfNewBeacon", null, null, null);

        mBeaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {

                // Just in case if there are multiple beacons with the same uuid, major, minor.
                Beacon foundBeacon = null;
                for (Beacon rangedBeacon : rangedBeacons) {
                    if (compareBeacon(rangedBeacon)) {
                        foundBeacon = rangedBeacon;
                        System.out.print("the Uuid of the beacon is: " + rangedBeacon.getProximityUUID());
                        System.out.print("the minor of the beacon is: " + rangedBeacon.getMinor());
                        // System.out.print("the major of the beacon is: " + rangedBeacon.);
                    }
                }

                updateDistance(foundBeacon);
            }
        });

        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    mBeaconManager.startRanging(mRegion);
                } catch (RemoteException e) {
                    System.out.print("problem with ranging");
                }
            }
        });
    }

    public void stop() {
        mBeaconManager.disconnect();
    }
}
