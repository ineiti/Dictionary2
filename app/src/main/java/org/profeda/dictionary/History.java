package org.profeda.dictionary;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ineiti on 05/08/2016.
 */

public class History {
    List<HistoryEntry> entries;

    public History(int lang){
        entries = new ArrayList<>();
        addItem(lang);
    }

    public void addItem(int lang){
        // Check if the last two are the same
        int len = entries.size();
        if (len >= 2){
            if (entries.get(len - 1).equals(entries.get(len - 2))){
                entries.remove(len - 1);
                len--;
            }
        }
        // Don't add an entry if the last one is empty
        if (len >= 1 && entries.get(len-1).text.equals("")){
            entries.remove(len-1);
        }
        entries.add(new HistoryEntry(lang));
    }

    public void setText(String t){
        entries.get(entries.size()-1).text = t;
    }

    public HistoryEntry getLast(){
        Log.i("History", entries.toString());
        if (entries.size() > 1) {
            return entries.remove(entries.size() - 2);
        }
        return new HistoryEntry(-1);
    }


    public class HistoryEntry{
        String text;
        int LangID;

        public HistoryEntry(int lang){
            LangID = lang;
            text = "";
        }

        public boolean equals(HistoryEntry he){
            return text.equals(he.text) && LangID == he.LangID;
        }
    }

}
