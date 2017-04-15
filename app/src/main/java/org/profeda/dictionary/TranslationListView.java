package org.profeda.dictionary;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
            setTextHtml(holder.translation, vh.translation);
            setHeight(holder.source, vh.source);
            setHeight(holder.example, vh.example);
            setHeight(holder.refarab, vh.reftudaga);
        }
        return convertView;
    }

    private void setTextHtml(TextView tv, String str){
        if (str != null && str.length() > 0) {
            Log.i("tlv: ", str);
            SpannableString text = new SpannableString(Html.fromHtml(str));
            tv.setText(text, TextView.BufferType.SPANNABLE);
        }
    }

    private void setHeight(TextView tv, String str){
        if (str != null && str.length() > 0){
            Log.i("tlv: ", str);
            setTextHtml(tv, str);
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
