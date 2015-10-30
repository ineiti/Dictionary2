package org.profeda.dictionary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Translate extends AppCompatActivity {
    WordList wordList;
    Boolean toasting;
    TextView etSearch;
    TextView tvLangSrc;
    TextView tvLangSrcRtL;
    TextView tvLangDst;
    ListView lvTranslations;
    ProgressDialog loadingDialog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    // Menu-id of the language-choice
    int LangId;
    static int menuLanguage = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        etSearch = (EditText) findViewById(R.id.etSearch);
        lvTranslations = (ListView) findViewById(R.id.translations);
        tvLangSrc = (TextView) findViewById(R.id.tvLangSrc);
        tvLangSrcRtL = (TextView) findViewById(R.id.tvLangSrcRtL);
        tvLangDst = (TextView) findViewById(R.id.tvLangDst);

        String liftFile = "teda-fr-en-ar.lift";
        // Shorter file for faster tests
        //String liftFile = "teda-short.lift";
        loadingDialog = ProgressDialog.show(Translate.this, "Please wait...",
                "Loading database " + liftFile);
        new LoadBackground().execute(liftFile);
        toasting = false;
        setKeyListener();
        Log.i("oncreate", "finished");
    }

    // Launches a search for each keypress and disables enter key
    private void setKeyListener() {
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                showSearch();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        return true;
                }
                return false;
            }
        });
    }

    // Converts a list of strings to a numbered list if there are
    // more than 1 entries, else shows only the single entry. If
    // no entry is present, shows the alternative text or nothing
    // if that one is emtpy
    public String concatList(List<String> entries, String alternative) {
        ArrayList<String> ret = new ArrayList<>();
        if (entries.size() == 0) {
            if (alternative != null && alternative != "") {
                ret.add(alternative);
            }
        } else if (entries.size() == 1) {
            ret.add(entries.get(0));
        } else {
            int nbr = 1;
            for (String e : entries) {
                ret.add(String.valueOf(nbr) + ": " + e);
                nbr++;
            }
        }
        return TextUtils.join("\n", ret);
    }

    // Searches for the text in etSearch and displays it in the list
    // lvTranslations
    public void showSearch() {
        String word = etSearch.getText().toString();
        ArrayList<TranslationItem> resultList = new ArrayList<TranslationItem>();

        if (word.length() > 0) {
            if (LangId >= wordList.Languages.size()) {
                Map<String, String> result = wordList.searchWordSource(word, getLangSource(LangId));
                if (result != null) {
                    for (Map.Entry<String, String> tr : result.entrySet()) {
                        TranslationItem newsData = new TranslationItem();
                        newsData.source = tr.getKey();
                        newsData.translation = tr.getValue();
                        resultList.add(newsData);
                    }
                }
            } else {
                Map<String, LiftCache> result = wordList.searchWord(word, getLangDest(LangId));
                if (result != null) {
                    for (Map.Entry<String, LiftCache> tr : result.entrySet()) {
                        LiftCache lc = tr.getValue();
                        TranslationItem newsData = new TranslationItem();
                        newsData.source = lc.Original;
                        newsData.translation = concatList(lc.Definitions,
                                lc.Gloss);

                        List<String> examples = new ArrayList<String>();
                        for (Lift.Example ex: lc.Examples) {
                            examples.add(ex.Example + " -> " + ex.Translation);
                        }
                        newsData.example = concatList(examples, null);
                        resultList.add(newsData);
                    }
                }
            }


        }
        lvTranslations.setAdapter(new TranslationListView(this, resultList));
    }

    public void deleteSearch(View b) {
        etSearch.setText("");
        showSearch();
    }

    public void showDetail(View b) {
        Intent intent = new Intent(this, WordDetail.class);
        startActivity(intent);
    }

    // Loads the lift-database in the background and dismisses the progressdialog
    // once done.
    public class LoadBackground extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... names) {
            try {
                if (names.length < 1) {
                    return 0L;
                }
                String name = names[0];
                String cache = getFilesDir().getPath() + "/" + name + ".ser";
                wordList = new WordList(cache, getAssets().open(name));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (long) 0;
        }

        @Override
        protected void onPostExecute(Long result) {
            loadingDialog.dismiss();
            LangId = sharedPref.getInt("LangId", 0);
            setLanguages(LangId);
            invalidateOptionsMenu();
            Log.i("loadgin", "postexecute");
        }
    }

    // Sets the Text-view fields for the language and saves the
    // actual state
    public void setLanguages(int id) {
        Log.i("Setting language", Integer.toString(id));
        LangId = id;
        String source = getLangSource(id);
        String dest = getLangDest(id);
        String src = WordList.langToFull(source) + ": ";
        String srcRtL = "";
        if (WordList.hasRightToLeft(src)) {
            srcRtL = src;
            src = "";
        }
        tvLangSrc.setText(src);
        tvLangSrcRtL.setText(srcRtL);
        tvLangDst.setText(WordList.langToFull(dest));
        editor.putInt("LangId", id);
        editor.commit();
        showSearch();
    }


    // Convert the index in the menu to the destination language
    public String getLangDest(int index) {
        int langs = wordList.Languages.size();
        int lang = index % langs;
        if (index >= langs) {
            return wordList.LanguageBase;
        } else {
            return wordList.Languages.get(lang);
        }
    }

    // Convert the index in the menu to the source language
    // Use the fact that the sources and destinations are symmetrical
    public String getLangSource(int index) {
        int langs = wordList.Languages.size();
        return getLangDest((index + langs) % (langs * 2));
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_translate, menu);
        Log.i("menu", "create");
        return true;
    }
    */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("menu", "prepareoptions");
        menu.removeGroup(menuLanguage);
        if (wordList != null) {
            // Offer the translation FROM the base-language to the other languages,
            // then the other way around.
            for (int index = 0; index < wordList.Languages.size() * 2; index++) {
                String langstr = WordList.langToFull(getLangSource(index)) + " -> " +
                        WordList.langToFull(getLangDest(index));
                menu.add(menuLanguage, index + 1, index + 1, langstr);
            }
        }
        return true;
    }

    public void showAbout() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Dictionary-app\nBased on SIL Lift-files\n(c) 2015 by Linus Gasser\n" +
                "ineiti@profeda.org")
                .setTitle("About");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.toString().equals("About")) {
            showAbout();
        } else {
            int id = item.getItemId() - 1;
            setLanguages(id);
        }

        return super.onOptionsItemSelected(item);
    }
}
