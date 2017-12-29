package com.ngf.smartcart;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;


public class ProductNavigationActivity extends ActionBarActivity {

    public final static String MESSAGE_PROD_NAME = "com.ngf.smartcart.MESSAGE_PROD_NAME";
    public final static String MESSAGE_PROD_UUID = "com.ngf.smartcart.MESSAGE_PROD_UUID";
    public final static String MESSAGE_PROD_MAJOR = "com.ngf.smartcart.MESSAGE_PROD_MAJOR";
    public final static String MESSAGE_PROD_MINOR = "com.ngf.smartcart.MESSAGE_PROD_MINOR";
    public final int ESTIMOTE_AVG_COUNT = 10;

    TagNotifier mTagNotifier = new NfcTagNotifier();
    BeaconFinder mBeaconFinder = new EstimoteBeaconFinder(this, ESTIMOTE_AVG_COUNT);
    ImageView circleImg;
    TextView distanceText;
    TextView beaconText;
    ProgressBar beaconProgress;

    private class UIBeaconFinder implements BeaconFinder.DistanceListener {
        public void updateDistance(double distance, BeaconFinder.DistanceState state) {
            // TODO: more than screen
            final double origWidth = 800.0;
            final double origHeight = 800.0;
            String text = "";

            switch (state) {
                case UNKNOWN:
                    distanceText.setTextColor(Color.parseColor("#848484"));
                    text = "Out of range";
                    break;
                case SAME:
                    distanceText.setTextColor( Color.parseColor("#000000"));
                    text = "No change";
                    break;
                case CLOSER:
                    distanceText.setTextColor(Color.parseColor("#FE642E"));
                    text = "Getting closer";
                    break;
                case FARTHER:
                    distanceText.setTextColor(Color.parseColor("#2ECCFA"));
                    text = "Getting farther";
                    break;
            }

            distanceText.setText(text);

            if (state == BeaconFinder.DistanceState.UNKNOWN) {
                showBeacon(false);
            }
            else {
                distance = Math.min(distance, 6.0) / 6.0;

                if (distance <= 0.16) {
                    distanceText.setTextColor( Color.parseColor("#DF0101"));
                    distanceText.setText("Found!");
                    distance = 0.16;
                }

                int newRadius = (int) (origWidth * distance);
                circleImg.getLayoutParams().height = newRadius;
                circleImg.getLayoutParams().width = newRadius;
                showBeacon(true);
            }

            circleImg.requestLayout();
        }
    }

    private void showBeacon(boolean found) {
        circleImg.setVisibility(found ? View.VISIBLE : View.INVISIBLE);
        beaconProgress.setVisibility(found ? View.INVISIBLE : View.VISIBLE);
        beaconText.setVisibility(found ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_navigation);

        Intent intent = getIntent();

        setResult(0);

        if (intent == null) {
            finish();
        }

        String name = intent.getStringExtra(MESSAGE_PROD_NAME);
        String uuid = intent.getStringExtra(MESSAGE_PROD_UUID);
        int major = intent.getIntExtra(MESSAGE_PROD_MAJOR, -1);
        int minor = intent.getIntExtra(MESSAGE_PROD_MINOR, -1);

        if (name == null || uuid == null || major == -1 || minor == -1) {
            finish();
        }

        setTitle("Navigate to " + name);

        circleImg = (ImageView) findViewById(R.id.img_circle);
        distanceText = (TextView) findViewById(R.id.text_distance_state);
        beaconText = (TextView) findViewById(R.id.text_beacon);
        beaconProgress = (ProgressBar) findViewById(R.id.progress_beacon);

        mBeaconFinder.setScanPeriod(300);
        mBeaconFinder.setWaitPeriod(0);
        mBeaconFinder.setTargetBeacon(new BeaconId(uuid, major, minor));
        mBeaconFinder.setListener(new UIBeaconFinder());

        try {
            mTagNotifier.setActivity(this);
            mTagNotifier.setListener(new TagNotifier.TagNotificationListener() {
                @Override
                public void notify(int tagId) {
                    setResult(tagId);
                    finish();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_product_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mBeaconFinder.start();
            mTagNotifier.start();
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        mTagNotifier.stop();
        mBeaconFinder.stop();
        setResult(0);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTagNotifier.resume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        mTagNotifier.pause();
        setResult(0);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTagNotifier.handleIntent(intent);
    }
}
