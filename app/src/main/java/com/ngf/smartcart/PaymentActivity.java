package com.ngf.smartcart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class PaymentActivity extends ActionBarActivity {

    public final static String MESSAGE_CASH_ID = "com.ngf.smartcart.MESSAGE_CASH_ID";

    private ProductList mProducts = new ProductList();
    TagNotifier mTagNotifier = new NfcTagNotifier();
    boolean mPaymentReady = true;
    TextView mStatusTextView;
    ListView mInvoiceListView;
    ProductListAdapter mProductsAdapter;
    ProgressBar mProgressBar;
    TextView mTotalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        setTitle("Payment");

        int tagId = 0;
        Intent intent = getIntent();

        if (intent != null)
        {
            tagId = intent.getIntExtra(MESSAGE_CASH_ID, 0);
        }

        mProducts.load(this, true);

        mStatusTextView = (TextView) findViewById(R.id.text_payment_state);
        mInvoiceListView = (ListView) findViewById(R.id.listview_invoice);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_payment);
        mTotalTextView = (TextView) findViewById(R.id.text_payment_total);

        mProductsAdapter = new ProductListAdapter(this, mProducts.getSelectedProducts(), false);

        // Set the ListView to use the ArrayAdapter
        mInvoiceListView.setAdapter(mProductsAdapter);

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

        processPayment(tagId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payment, menu);
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

    public void processPayment(final int srcTagId) {
        if (!mPaymentReady) {
            Toast.makeText(PaymentActivity.this, "Payment already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        mPaymentReady = false;

        Thread paymentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int tagId = srcTagId;

                    if (tagId == 0) {
                        tagId = mProducts.getDatabase().DEFAULT_CASH;
                    }

                    Database.CashAddress addr = mProducts.getDatabase().getCash(tagId);

                    if (addr == null) {
                        setTitleOnUi("Payment failed", false);
                        makeToastOnUi(PaymentActivity.this, "Unknown cash register", Toast.LENGTH_LONG);
                        return;
                    }

                    CashClient client = new CashClient();

                    client.clear();

                    for (Object p : mProducts.getSelectedProducts()) {
                        client.add(((Product) p).mTagId, 1);
                    }

                    final double total = client.send(addr.getHost(), addr.getPort());

                    setTitleOnUi("Payment done", false);
                    setTotalOnUi(total, false);

                    mProducts.resetSelectedProducts();
                    mProducts.store(PaymentActivity.this);
                } catch (Exception e) {
                    setTitleOnUi("Payment failed", false);
                    makeToastOnUi(PaymentActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                } finally {
                    mPaymentReady = true;
                }
            }
        });

        paymentThread.start();
    }

    void setTotalOnUi(final double total, final boolean showProgress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DecimalFormat df = new DecimalFormat("#.00");
                final String message = "Total: $" + df.format(total);

                mTotalTextView.setText(message);
                mTotalTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    void setTitleOnUi(final CharSequence message, final boolean showProgress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusTextView.setText(message);
                mProgressBar.setVisibility(showProgress ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    static void makeToastOnUi(final Activity activity, final CharSequence message, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, duration).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mTagNotifier.start();
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        mTagNotifier.stop();
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
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTagNotifier.handleIntent(intent);
    }
}
