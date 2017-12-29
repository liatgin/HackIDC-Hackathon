package com.ngf.smartcart;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;

public class NfcTagNotifier implements TagNotifier {
    private NfcAdapter mAdapter;
    private Activity mActivity;
    private TagNotificationListener mListener;

    public NfcTagNotifier() {
        mAdapter = null;
        mActivity = null;
        mListener = null;
    }

    public void setActivity(Activity activity) throws Exception {
        if (isRunning()) {
            throw new Exception("Cannot set activity while running");
        }

        this.mActivity = activity;
    }

    public void setListener(TagNotificationListener listener) throws Exception {
        if (isRunning()) {
            throw new Exception("Cannot set listener while running");
        }

        this.mListener = listener;
    }

    public boolean isRunning() {
        return mAdapter != null;
    }

    public void start() throws Exception {
        if (isRunning()) {
            return;
        }

        if (mActivity == null)
        {
            throw new Exception("Activity not set");
        }

        if (mListener == null)
        {
            throw new Exception("Listener not set");
        }

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mActivity);

        if (adapter == null)
        {
            throw new Exception("NFC is not supported");
        }

        if (!adapter.isEnabled())
        {
            throw new Exception("NFC is disabled");
        }

        mAdapter = adapter;

        handleIntent(mActivity.getIntent());
    }

    public void stop() {
        if (!isRunning()) {
            return;
        }

        mAdapter = null;
    }

    public void resume() {
        if (!isRunning()) {
            return;
        }

        final Intent intent = new Intent(mActivity.getApplicationContext(), mActivity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(mActivity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];

        String[][] techList = new String[][] {
                new String[] { MifareClassic.class.getName() },
                new String[] { NfcA.class.getName() },
                new String[] { NdefFormatable.class.getName() }
        };

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        //filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        mAdapter.enableForegroundDispatch(mActivity, pendingIntent, filters, techList);
    }

    public void pause() {
        if (!isRunning()) {
            return;
        }

        mAdapter.disableForegroundDispatch(mActivity);
    }

    public void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            int tagId = 0;

            for (byte b : tag.getId()) {
                tagId <<= 8;
                tagId |= (((int)b) & 0xFF);
            }

            mListener.notify(tagId);
        }
    }
}
