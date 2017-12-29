package com.ngf.smartcart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

// TODO: type lower-case
public class MainActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener /*, AdapterView.OnItemLongClickListener*/ {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /**
         * Touching the selected item disables it.
         */
        Product prod = (Product) mProdList.get(position);


        if (prod.getSelected()) {
            prod.mSelected = false;
            // Turn white.
            view.setBackgroundColor(Color.parseColor("#F0F0F0"));
        }
        else {
            Intent intent = new Intent(this, ProductNavigationActivity.class);
            intent.putExtra(ProductNavigationActivity.MESSAGE_PROD_NAME, prod.mName);
            intent.putExtra(ProductNavigationActivity.MESSAGE_PROD_UUID, prod.mBeaconId.getUuid());
            intent.putExtra(ProductNavigationActivity.MESSAGE_PROD_MAJOR, prod.mBeaconId.getMajor());
            intent.putExtra(ProductNavigationActivity.MESSAGE_PROD_MINOR, prod.mBeaconId.getMinor());

            startActivityForResult(intent, 0);
        }
    }

    /**
     * This class represents a grocery product.
     */
    private class NFCTagNotificationListener implements TagNotifier.TagNotificationListener {
        public void notify(int tagId) {
            processTag(tagId);
        }
    }

    Button addButton;
    EditText mainEditText;
    Button clearButton;
    TextView totalTextView;
    Button payButton;

    ListView mainListView;
    ProductListAdapter mProdListAdapter;
    ProductList mProdList = new ProductList();
    boolean mDisableWelcome = false;
    SharedPreferences mSharedPreferences;

    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";

    private static final int NOT_FOUND = -1;

    TagNotifier mTagNotifier = new NfcTagNotifier();

    boolean mPaymentReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProdList.load(this, false);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mProdListAdapter.clear();
                mProdListAdapter.notifyDataSetChanged();
                mProdList.clear();
                updateTotal();
            }
        });

        payButton = (Button) findViewById(R.id.pay_button);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                processPayment(0);
            }
        });

        // 3. Access the EditText defined in layout XML
        mainEditText = (EditText) findViewById(R.id.main_edittext);

        // 4. Access the ListView
        mainListView = (ListView) findViewById(R.id.main_listview);

        // 4. Access Total
        totalTextView = (TextView) findViewById(R.id.total_textview);

        // Create a ProductListAdapter for the ListView
        mProdListAdapter = new ProductListAdapter(this, mProdList, true);

        // Set the ListView to use the ArrayAdapter
        mainListView.setAdapter(mProdListAdapter);

        // 5. Set this activity to react to list items being pressed
        mainListView.setOnItemClickListener(this);
        mainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Product typedProd = (Product) mProdList.get(pos);

                //View view = getViewByPosition(pos, mainListView);
                mProdListAdapter.remove(typedProd);
                mProdListAdapter.notifyDataSetChanged();

                updateTotal();

                return true;
            }
        });

        // 7. Greet the user, or ask for their name if new
        displayWelcome();

        // For debug
        initList();

        try {
            mTagNotifier.setActivity(this);
            mTagNotifier.setListener(new NFCTagNotificationListener());
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initList() {
        mProdListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v)
    {
        String typedDBName;
        String typedName = mainEditText.getText().toString();

        if (typedName.length() >= 1) {
            String upLetter = typedName.substring(0,1).toUpperCase();
            String rest = typedName.substring(1);
            typedDBName= upLetter + rest;
        }
        else {
            typedDBName= typedName;
        }

        Product typedProd = mProdList.getDatabase().getProductByName(typedDBName);

        if (typedProd != null) {
            // Create copy in order to distinguish between the products.
            Product newSameProd = new Product(typedProd.mName, typedProd.mTagId, typedProd.mSelected, typedProd.mBeaconId, typedProd.mPrice);

            // Also add that value to the list shown in the ListView
            mProdListAdapter.add(newSameProd);
            mProdListAdapter.notifyDataSetChanged();
            mainEditText.setText("");
        }
        else {
            Toast.makeText(this, "\"" + typedName + "\" doesn't exist", Toast.LENGTH_LONG).show();
        }
    }

    public void processTag(int tagId) {
        if (mProdList.getDatabase().isCash(tagId)) {
            processPayment(tagId);
        }
        else {
            selectItem(tagId);
        }
    }

    public void processPayment(final int tagId) {
        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.MESSAGE_CASH_ID, tagId);
        startActivityForResult(intent, 0);
    }

    /**
     * For RFID usage
     *
     * @param tagId tag id.
     */
    public void selectItem(int tagId) {
        Product scannedProd = mProdList.getDatabase().getProductByID(tagId);
        if (scannedProd != null) {
            int index = mProdList.indexOf(scannedProd);
            int color = Color.parseColor("#D2E4FC");

            if (index != NOT_FOUND) {
                View view = getViewByPosition(index, mainListView);
                for (Object p : mProdList) {
                    if (((Product) p).mTagId == scannedProd.mTagId && !((Product) p).getSelected()) {
                        colorListViewItem((Product) p);
                        break;
                    }
                }
            }
            else {
                // Add to list
                scannedProd = new Product(mProdList.getDatabase().getProductByID(tagId));
                mProdListAdapter.add(scannedProd);
                mProdListAdapter.notifyDataSetChanged();

                // After the item is added to the listView, it is colored.
                colorListViewItem(scannedProd);
            }
        } else {
            Toast.makeText(this, "\"" + tagId + "\" isn't recognized", Toast.LENGTH_LONG).show();
        }
    }

    private void colorListViewItem(Product p) {
        int index;
        for (index = 0; index < mProdList.size(); index++) {
            if (mProdList.get(index) == p) {
                break;
            }
        }

        p.mSelected = true;

        if (index < mProdList.size()) {
            View view = getViewByPosition(index, mainListView);
            view.setBackgroundColor(Color.parseColor("#D2E4FC"));
            view.invalidate();
        }

        updateTotal();
    }

    private void color() {
        for (int i = 0; i < mProdList.size() ; i++) {
            View view = getViewByPosition(i, mainListView);
            if (mProdList.get(i).getSelected()) {
                view.setBackgroundColor(Color.parseColor("#D2E4FC"));
                view.invalidate();
            }
            else {
                view.setBackgroundColor(Color.parseColor("#F0F0F0"));
                view.invalidate();
            }
        }
    }

    private void updateTotal() {
        double total = 0;

        for (Product p : mProdList) {
            if (p.getSelected()) {
                total += p.mPrice;
            }
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        totalTextView.setText("$" + df.format(total));

        color();
    }



    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public ArrayList<Integer> selectedItemsId() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Object p : mProdList) {
            result.add(((Product)p).mTagId);
        }
        return result;
    }

    public void displayWelcome() {

        // Access the device's key-value storage
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Read the user's name,
        // or an empty string if nothing found
        String name = mSharedPreferences.getString(PREF_NAME, "");

        if (name.length() > 0) {

            // If the name is valid, display a Toast welcoming them
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
        }
        else {

            // otherwise, show a dialog to ask for their name
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setMessage("What is your name?");

            // Create EditText for entry
            final EditText input = new EditText(this);
            alert.setView(input);

            // Make an "OK" button to save the name
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {

                    // Grab the EditText's input
                    String inputName = input.getText().toString();

                    // Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.commit();

                    // Welcome the new user
                    Toast.makeText(getApplicationContext(), "Welcome, " + inputName + "!", Toast.LENGTH_LONG).show();
                }
            });

            // Make a "Cancel" button
            // that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {}
            });

            alert.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mDisableWelcome = true;

        if (resultCode != 0) {
            processTag(resultCode);
        }
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
    protected void onResume() {
        super.onResume();

        if (mDisableWelcome) {
            mDisableWelcome = false;
        }
        else {
            // 7. Greet the user, or ask for their name if new
            displayWelcome();
        }

        mTagNotifier.resume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        mTagNotifier.pause();
        mProdList.store(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mTagNotifier.stop();
        mProdList.store(this);
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTagNotifier.handleIntent(intent);

        mDisableWelcome = true;
    }
}
