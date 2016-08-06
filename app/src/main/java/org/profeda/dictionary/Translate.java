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
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Translate extends AppCompatActivity {
    public static WordList wordList;
    Boolean toasting;
    TextView etSearch;
    TextView tvLangSrc;
    TextView tvLangSrcRtL;
    TextView tvLangDst;
    ListView lvTranslations;
    ProgressDialog loadingDialog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    List<String> searchResultString;
    List<LiftCache> searchResultLiftCache;
    History history;

    // Menu-id of the language-choice
    int LangId;
    static int WORD_DETAIL_RESULT = 1;


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
        setListItemListener();
        Log.i("oncreate", "finished");
    }

    @Override
    public void onBackPressed(){
        // As there is always a growing element at the end, size == 1 means the history
        // is empty.
        History.HistoryEntry he = history.getLast();
        if (he.LangID == -1){
            Log.i("back", "finishing");
            finish();
            return;
        }
        Log.i("back", String.valueOf(he.LangID) + he.text);
        setLanguages(he.LangID);
        showSearch(he.text);
    }

    // Sets up a listener for the ListView. If it's translating FROM the main language,
    // it presents details, else it changes the translation direction
    private void setListItemListener() {
        lvTranslations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parentView, View childView,
                                    int position, long id) {
                Log.i("soicl", "fired for position " + String.valueOf(position));
                if (LangId >= wordList.Languages.size()) {
                    String text = searchResultString.get(position);
                    Log.i("showDetailText", text);
                    changeTranslationDirectionSearchList(null);
                    history.addItem(LangId);
                    etSearch.setText(text);
                    showSearch();
                } else {
                    history.addItem(LangId);
                    LiftCache lc = searchResultLiftCache.get(position);
                    Log.i("showDetailLC", lc.Original);
                    Intent intent = new Intent(getBaseContext(), WordDetail.class);
                    intent.putExtra("EXTRA_LIFTCACHE", lc);
                    intent.putExtra("EXTRA_DEST", getLangDest(LangId));
                    intent.putExtra("EXTRA_ENTRIES", Language.uiTranslations(getLangDest(LangId)));
                    startActivityForResult(intent, WORD_DETAIL_RESULT);
                }
            }
        });
    }

    // Upon clicking a word in the result page, we will search
    // for that word - either in the source or in the destination
    // langauge, depending on "inverseSearch"
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == WORD_DETAIL_RESULT) {
            if (resultCode == RESULT_OK) {
                String search = data.getStringExtra("searchValue");
                if (data.getBooleanExtra("inverseSearch", false)) {
                    changeTranslationDirection(search);
                } else {
                    showSearch(search);
                }
            }
        }
    }

    // Launches a search for each keypress and disables enter key
    private void setKeyListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Log.i("onTextChanged:", s.toString());
                    showSearch();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // Nothing to do
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Nothing to do

            }
        });
    }

    // Searches for the text in etSearch and displays it in the list
    // lvTranslations
    public void showSearch() {
        String word = etSearch.getText().toString();
        Log.i("showSearch", word);
        history.setText(word);
        searchResultLiftCache = new ArrayList<LiftCache>();
        searchResultString = new ArrayList<>();
        ArrayList<TranslationItem> resultList = new ArrayList<TranslationItem>();

        if (word.length() > 0) {
            if (LangId >= wordList.Languages.size()) {
                Map<String, LiftCache> result = wordList.searchWordSource(word, getLangSource(LangId));
                if (result != null) {
                    for (Map.Entry<String, LiftCache> tr : result.entrySet()) {
                        searchResultString.add(tr.getValue().Original);
                        TranslationItem newsData = new TranslationItem();
                        newsData.source = tr.getKey();
                        newsData.translation = tr.getValue().Original;
                        resultList.add(newsData);
                    }
                }
            } else {
                Map<String, LiftCache> result = wordList.searchWord(word, getLangDest(LangId));
                if (result != null) {
                    for (Map.Entry<String, LiftCache> tr : result.entrySet()) {
                        LiftCache lc = tr.getValue();
                        int count = 1;
                        int sensesCnt = lc.Senses.size();
                        for (LiftCacheDefinition s : lc.Senses) {
                            //System.out.println("Sense: " + s.String() + "=" + s.GlossDef() + ";");
                            TranslationItem newsData = new TranslationItem();
                            if (count == 1) {
                                newsData.source = lc.Original;
                            }
                            if (count == sensesCnt) {
                                newsData.refarab = lc.RefArab;
                                //System.out.println("RefArab is:" + lc.RefArab);
                            }
                            if (sensesCnt > 1) {
                                newsData.translation = "(" + String.valueOf(count) + ") " + s.GlossDef();
                            } else {
                                newsData.translation = s.GlossDef();
                            }
                            newsData.example = s.ExamplesString();
                            count++;
                            resultList.add(newsData);
                            searchResultLiftCache.add(lc);
                        }
                    }
                }
            }


        }
        lvTranslations.setAdapter(new TranslationListView(this, resultList));
    }

    // Sets the search field and searches for that one
    public void showSearch(String text) {
        Log.i("showSearchT", text);
        etSearch.setText(text);
        showSearch();
    }

    // Cleans the search field
    public void deleteSearch(View b) {
        history.addItem(LangId);
        showSearch("");
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
            Log.i("loading", "postexecute");
            loadingDialog.dismiss();
            LangId = sharedPref.getInt("LangId", 0);
            history = new History(LangId);
            setLanguages(LangId);
        }
    }

    // Sets the Text-view fields for the language and saves the
    // actual state
    public void setLanguages(int id) {
        Log.i("Setting language", Integer.toString(id));
        LangId = id;
        String source = getLangSource(id);
        String dest = getLangDest(id);
        String src = Language.langToFull(source) + ": ";
        String srcRtL = "";
        if (WordList.hasRightToLeft(src)) {
            srcRtL = src;
            src = "";
        }
        tvLangSrc.setText(src);
        tvLangSrcRtL.setText(srcRtL);
        tvLangDst.setText(Language.langToFull(dest));
        editor.putInt("LangId", id);
        editor.commit();
        String title = source + "<->" + dest;
        Log.i("setLanguages", "title will be: " + title);
        ActionMenuItemView mitem = (ActionMenuItemView) findViewById(R.id.action_language);
        mitem.setTitle(title);
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

    public void showAbout() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Dictionary-app\nBased on SIL Lift-files\n(c) 2016 by Linus Gasser\n" +
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
        String mstr = item.toString();
        //System.out.println("Menu is " + mstr + " - with id:" + String.valueOf(item.getGroupId()));
        if (mstr.equals("About")) {
            showAbout();
        } else {
            // This is our 'language'-text
            List<String> listItems = new ArrayList<String>();
            for (int index = 0; index < wordList.Languages.size() * 2; index++) {
                String langstr = Language.langToFull(getLangSource(index)) + " -> " +
                        Language.langToFull(getLangDest(index));
                listItems.add(langstr);
            }

            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a translation");

            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    //Toast.makeText(getApplicationContext(), String.valueOf(item), Toast.LENGTH_SHORT).show();
                    setLanguages(item);
                    history.addItem(LangId);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    // Inverts source- and destination language
    public void changeTranslationDirection(String search) {
        int size = wordList.Languages.size();
        setLanguages((LangId + size) % (2 * size));
        history.addItem(LangId);
        showSearch(search);
    }

    // Inverts the source- and destination- language and starts a new
    // search with the first result of the list
    public void changeTranslationDirectionSearchList(View v) {
        Log.i("changeDirection", String.valueOf(LangId));
        String search = "";
        if (LangId >= wordList.Languages.size()) {
            Log.i("changeDirection", "Going from backtranslation to normal");
            if (searchResultString != null && searchResultString.size() > 0) {
                search = searchResultString.get(0);
            }
        } else {
            Log.i("changeDirection", "Going from normal to backtranslation");
            if (searchResultLiftCache != null && searchResultLiftCache.size() > 0) {
                search = searchResultLiftCache.get(0).GetFirstSense();
            }
        }
        changeTranslationDirection(search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_translate, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
