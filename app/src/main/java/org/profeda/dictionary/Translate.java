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

import static org.profeda.dictionary.WordList.versionMajor;
import static org.profeda.dictionary.WordList.versionMinor;
import static org.profeda.dictionary.WordList.versionPatch;

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
    public Languages Lang;
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
        Lang = initLanguages();

        loadingDialog = ProgressDialog.show(Translate.this, "Please wait,",
                "Loading database");
        new LoadBackground().execute();
        toasting = false;
        setKeyListener();
        setListItemListener();

        Log.i("oncreate", "finished");
    }

    @Override
    public void onBackPressed() {
        // As there is always a growing element at the end, size == 1 means the history
        // is empty.
        History.HistoryEntry he = history.getLast();
        if (he.LangID == -1) {
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
                if (Lang.BaseDst()) {
                    String text = searchResultString.get(position);
                    Log.i("showDetailText", text);
                    changeTranslationDirectionSearchList(null);
                    history.addItem(Lang);
                    etSearch.setText(text);
                    showSearch();
                } else {
                    history.addItem(Lang);
                    LiftCache lc = searchResultLiftCache.get(position);
                    Log.i("showDetailLC", lc.Original);
                    if (lc.RefTudaga != null){
                        showSearch(lc.RefTudaga);
                    } else {
                        Intent intent = new Intent(getBaseContext(), WordDetail.class);
                        intent.putExtra("EXTRA_LIFTCACHE", lc);
                        intent.putExtra("EXTRA_DEST", Lang.Dst().Lang_Lift);
                        intent.putExtra("EXTRA_ENTRIES", Lang);
                        startActivityForResult(intent, WORD_DETAIL_RESULT);
                    }
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
            if (Lang.BaseDst()) {
                Map<String, LiftCache> result = wordList.searchWordSource(word, Lang.Src().Lang_Lift);
                if (result != null) {
                    for (Map.Entry<String, LiftCache> tr : result.entrySet()) {
                        LiftCache lc = tr.getValue();
                        Log.i("showSearch", "BaseDst: " + lc.toString());
                        searchResultString.add(lc.Original);
                        TranslationItem newsData = new TranslationItem();
                        newsData.source = lc.Senses.get(0).Gloss;
                        newsData.translation = lc.Original;
                        resultList.add(newsData);
                    }
                }
            } else {
                Map<String, LiftCache> result = wordList.searchWord(word, Lang.Dst().Lang_Lift);
                if (result != null) {
                    for (Map.Entry<String, LiftCache> tr : result.entrySet()) {
                        LiftCache lc = tr.getValue();
                        Log.i("showSearch", "BaseSrc: " + lc.toString());
                        int count = 1;
                        int sensesCnt = lc.Senses.size();
                        if (lc.RefTudaga != null) {
                            TranslationItem newsData = new TranslationItem();
                            newsData.reftudaga = lc.RefTudaga;
                            resultList.add(newsData);
                            searchResultLiftCache.add(lc);
                            Log.i("Display", "RefTudaga is:" + lc.RefTudaga);
                        }
                        for (LiftCacheDefinition s : lc.Senses) {
                            //System.out.println("Sense: " + s.String() + "=" + s.GlossDef() + ";");
                            TranslationItem newsData = new TranslationItem();
                            if (count == 1) {
                                newsData.source = new ColorText().
                                        addTextColor(lc.Original, Lang.Src().Color).result;
                            }
                            if (sensesCnt > 1) {
                                newsData.translation = new ColorText(String.format("(%d)", count)).
                                        addTextColor(s.GlossDef(), Lang.Dst().Color).result;
                            } else {
                                newsData.translation = new ColorText().
                                        addTextColor(s.GlossDef(), Lang.Dst().Color).result;
                            }
                            newsData.example = s.ExamplesString(Lang);
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
        if (text.equals("")){
            showSearch();
        }
    }

    // Cleans the search field
    public void deleteSearch(View b) {
        history.addItem(Lang);
        etSearch.setText("");
        showSearch();
    }

    // Loads the lift-database in the background and dismisses the progressdialog
    // once done.
    public class LoadBackground extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... names) {
            try {
                wordList = new WordList(getAssets().open("teda.cache"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (long) 0;
        }

        @Override
        protected void onPostExecute(Long result) {
            Log.i("loading", "postexecute");
            loadingDialog.dismiss();
            Lang.SetDirection(sharedPref.getInt("LangId", 0));
            history = new History(Lang);
            setLanguages(Lang.GetDirection());
        }
    }

    // Sets the Text-view fields for the language and saves the
    // actual state
    public void setLanguages(int id) {
        Lang.SetDirection(id);
        Log.i("Setting language", Integer.toString(id));
        String source = Lang.Src().Lang_Abbr;
        String dest = Lang.Dst().Lang_Abbr;
        String src = Lang.Src().Lang_Full;
        etSearch.setHint(Lang.Src().Search);
        src +=  ": ";
        String srcRtL = "";
        if (WordList.hasRightToLeft(src)) {
            srcRtL = src;
            src = "";
        }
        tvLangSrc.setText(src);
        tvLangSrcRtL.setText(srcRtL);
        tvLangDst.setText(Lang.Dst().Lang_Full);
        editor.putInt("LangId", id);
        editor.commit();
        String title = source + "<->" + dest;
        Log.i("setLanguages", "title will be: " + title);
        ActionMenuItemView mitem = (ActionMenuItemView) findViewById(R.id.action_language);
        mitem.setTitle(title);
        showSearch();
    }

    public void showAbout() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String app = String.format("Dictionary-app %d.%d.%d\n",
                versionMajor, versionMinor, versionPatch);
        builder.setMessage(app + "Based on SIL Lift-files\n(c) 2017 by Linus Gasser\n" +
                "ineiti@gasser.blue")
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

            List<String> listItems = Lang.TranslationList();
            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(Lang.Src().Pick);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    //Toast.makeText(getApplicationContext(), String.valueOf(item), Toast.LENGTH_SHORT).show();
                    setLanguages(item);
                    history.addItem(Lang);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    // Inverts source- and destination language
    public void changeTranslationDirection(String search) {
        Lang.ChangeDirection();
        history.addItem(Lang);
        setLanguages(Lang.GetDirection());
        showSearch(search);
    }

    // Inverts the source- and destination- language and starts a new
    // search with the first result of the list
    public void changeTranslationDirectionSearchList(View v) {
        String search = "";
        if (etSearch.getText().length() > 0) {
            // Only use translated text if search-field is not empty.
            if (Lang.BaseDst()) {
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
        }
        changeTranslationDirection(search);
        showSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_translate, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static Languages initLanguages(){
        Languages Lang;
        Lang = new Languages(new Languages.Language("tuq", "TU", "Tudaga",
                "المعنى ", "أَمْثال", "أَمْثال إِضافيّة ", "Baradi", "#000000",
                "ka daama yoŋ"));
        Lang.AddLanguage(new Languages.Language("fr", "FR", "Français",
                "Sens", "Exemples", "Exemples supplémentaires", "Rechercher", "#444488",
                "Choisissez une traduction"));
        Lang.AddLanguage(new Languages.Language("ayl", "AR", "عربي",
                "المعنى ", "أَمْثال", "أَمْثال إِضافيّة ", "بحث", "#448844",
                "Ka daama yoŋ"));
        Lang.AddLanguage(new Languages.Language("en", "EN", "English", "Meaning", "Examples",
                "Additional Sentences", "Search", "#884444",
                "Pick a translation"));
        return Lang;
    }
}
