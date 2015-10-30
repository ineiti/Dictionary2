package org.profeda.dictionary;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by ineiti on 07/09/2015.
 */
public class TranslationListView extends BaseAdapter {

    private ArrayList<TranslationItem> listData;
    private LayoutInflater layoutInflater;

    public TranslationListView(Context aContext, ArrayList<TranslationItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.source = (TextView) convertView.findViewById(R.id.source);
            holder.translation = (TextView) convertView.findViewById(R.id.translation);
            holder.example = (TextView) convertView.findViewById(R.id.example);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TranslationItem vh = listData.get(position);
        holder.source.setText(vh.source);
        holder.translation.setText(vh.translation);
        String ex = vh.example;
        // Make the list-element a bit shorter if there is no example
        if (ex != null && ex.length() > 0) {
            holder.example.setText(ex);
            // Without this the first element of the list doesn't get displayed...
            holder.example.setMaxHeight(100);
        } else {
            holder.example.setMaxHeight(0);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView source;
        TextView translation;
        TextView example;
    }
}
