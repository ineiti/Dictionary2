package org.profeda.dictionary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordDetail extends AppCompatActivity {
    LiftCache liftCache;
    String dest;
    HashMap<String, String> entries;
    Map<String, LiftCache> exampleResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        Bundle bundle = getIntent().getExtras();
        liftCache = (LiftCache) bundle.get("EXTRA_LIFTCACHE");
        dest = (String) bundle.get("EXTRA_DEST");
        entries = (HashMap<String, String>) bundle.get("EXTRA_ENTRIES");
        Log.i("WordDetail", liftCache.toString());
        ((TextView) findViewById(R.id.tvWDTitle)).setText(liftCache.Original);
        /*
        setEntry(R.id.trPhon, R.id.tvPhon, R.id.tvPhonText,
                "Pronunciation", liftCache.Pronunciation);
                */
        setEntry(R.id.trDefinition, R.id.tvDefinition, R.id.tvDefinitionText,
                "Definitions", liftCache.TranslationString());
        setEntry(R.id.trExample, R.id.tvExample, R.id.tvExampleText,
                "Examples", liftCache.ExamplesString());
        setEntry(R.id.trSynonym, R.id.tvSynonym, R.id.tvSynonymText,
                "Synonym", liftCache.Synonym);
        setEntry(R.id.trAntonym, R.id.tvAntonym, R.id.tvAntonymText,
                "Antonym", liftCache.Antonym);
        setEntry(R.id.trCross, R.id.tvCross, R.id.tvCrossText,
                "CrossRef", liftCache.CrossString());
        setEntry(R.id.trUsage, R.id.tvUsage, R.id.tvUsageText,
                "Usage", "");
        new SearchBackground().execute(liftCache.Original);
    }

    public void setEntry(int trEntry, int tvEntry, int tvText, String entry, String text) {
        TextView tvE = (TextView) findViewById(tvEntry);
        TextView tvT = (TextView) findViewById(tvText);
        TableRow row = (TableRow) findViewById(trEntry);
        if (text != null && text.length() > 0) {
            tvE.setEnabled(true);
            tvT.setEnabled(true);
            row.setVisibility(View.VISIBLE);
            tvE.setText(entries.get(entry));
            tvT.setText(text);
        } else {
            tvE.setEnabled(false);
            tvT.setEnabled(false);
            row.setVisibility(View.GONE);
        }
    }

    // Searches for examples that match and displays all found, except the
    // examples of the current word
    public class SearchBackground extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... searchStr) {
            try {
                String search = Language.deAccent(searchStr[0]);
                exampleResults = new TreeMap<String, LiftCache>();

                String regex = ".*\\b" + search + "\\b.*";
                for (Map.Entry<String, LiftCache> entry :
                        Translate.wordList.TranslationList.get(dest).entrySet()) {
                    if (isCancelled()) break;
                    for (Lift.Example ex : entry.getValue().Examples) {
                        if (Language.deAccent(ex.Example).matches(regex)) {
                            if (!entry.getKey().equals(Language.deAccent(liftCache.Original))) {
                                exampleResults.put(entry.getKey(), entry.getValue());
                                //publishProgress(0);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return (long) 0;
        }

        @Override
        // Not used for the moment
        protected void onProgressUpdate(Integer... progress) {
            String usage = "";
            for (Map.Entry<String, LiftCache> entry : exampleResults.entrySet()) {
                usage += entry.getValue().ExamplesString() + "\n";
            }
            setEntry(R.id.trUsage, R.id.tvUsage, R.id.tvUsageText,
                    "Usage", usage);
        }

        @Override
        protected void onPostExecute(Long result) {
            for (Map.Entry<String, LiftCache> entry : exampleResults.entrySet()) {
                LiftCache lc = entry.getValue();
                addExample(lc.Original, lc.ExamplesString());
            }
        }
    }

    // Adds an example to the tableview - contrary to the example of the word,
    // these examples have the word that holds the example as a header, and the
    // example below.
    public void addExample(final String word, String example) {
        TableLayout tl = (TableLayout) findViewById(R.id.tlWordDetail);

        // Create header with word
        addRow(tl, word, word, true);

        // Add example below
        addRow(tl, example, word, false);
    }

    // Adds one row to the tablelayout
    public void addRow(TableLayout tl, String show, final String search, boolean header){
        TableRow tr = new TableRow(getApplicationContext());
        TextView tv = new TextView(getApplicationContext());
        tv.setText(show);
        TableRow.LayoutParams tlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(2, 2, 2, 2);
        tlp.span = 2;
        tlp.weight = 1.0f;
        if (header){
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setBackgroundColor(Color.LTGRAY);
        }
        tv.setLayoutParams(tlp);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(22);
        tr.addView(tv);
        tr.setOnClickListener(new AdapterView.OnClickListener() {
                                  public void onClick(View childView) {
                                      returnSearch(search, false);
                                  }
                              }
        );
        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                //noinspection SimplifiableIfStatement
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Returns a value to be searched for.
    // If inverse == true then it will swap the languages to be searched for
    public void returnSearch(String search, boolean inverse) {
        Intent resultData = new Intent();
        resultData.putExtra("inverseSearch", inverse);
        Matcher m1 = Pattern.compile("(1: )*([\\w]+)").matcher(search);
        m1.find();
        resultData.putExtra("searchValue", m1.group(2));
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }

    // Handler for inverse searches
    public void clickInverseSearch(View b) {
        returnSearch(((TextView) b).getText().toString(), true);
    }

    // Handler for direct searches
    public void clickSearch(View b) {
        returnSearch(((TextView) b).getText().toString(), false);
    }
}
