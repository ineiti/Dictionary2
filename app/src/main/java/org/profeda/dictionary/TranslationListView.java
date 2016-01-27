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
            holder.refarab = (TextView) convertView.findViewById(R.id.refarab);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TranslationItem vh = listData.get(position);
        if (vh != null) {
            holder.translation.setText(vh.translation);
            setHeight(vh.source, holder.source);
            setHeight(vh.example, holder.example);
            setHeight(vh.refarab, holder.refarab);
        }
        return convertView;
    }

    private void setHeight(String str, TextView tv){
        if (str != null && str.length() > 0){
            tv.setText(str);
            tv.setMaxHeight(100);
        } else {
            tv.setMaxHeight(0);
        }
    }

    static class ViewHolder {
        TextView source;
        TextView translation;
        TextView example;
        TextView refarab;
    }
}
