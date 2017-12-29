package com.ngf.smartcart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class ProductListAdapter extends BaseAdapter {
    Context context;
    List<Product> data;
    boolean showDelete;
    private static LayoutInflater inflater = null;

    public ProductListAdapter(Context context, List<Product> data, boolean showDelete) {
        this.context = context;
        this.data = data;
        this.showDelete = showDelete;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = inflater.inflate(R.layout.product_list_item, null);
        }

        TextView name = (TextView) v.findViewById(android.R.id.text1);
        TextView price = (TextView) v.findViewById(android.R.id.text2);
        ImageView delete = (ImageView) v.findViewById(android.R.id.closeButton);

        DecimalFormat df = new DecimalFormat("#0.00");

        name.setText(data.get(position).mName);
        price.setText("$" + df.format(data.get(position).mPrice));
        delete.setVisibility(showDelete ? View.VISIBLE : View.GONE);

        return v;
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void add(Product p) {
        data.add(p);
        notifyDataSetChanged();
    }

    public void remove(Product p) {
        data.remove(p);
        notifyDataSetChanged();
    }
}
