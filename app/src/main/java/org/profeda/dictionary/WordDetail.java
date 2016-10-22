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
    Map<String, LiftCacheDefinition> exampleResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        Bundle bundle = getIntent().getExtras();
        liftCache = (LiftCache) bundle.get("EXTRA_LIFTCACHE");
        dest = (String) bundle.get("EXTRA_DEST");
        entries = (HashMap<String, String>) bundle.get("EXTRA_ENTRIES");
        String search = liftCache.Original;
        setTitle(search);
        Log.i("WordDetail", liftCache.toString());

        TableLayout tl = (TableLayout) findViewById(R.id.tlWordDetail);
        addLine(tl, "->", liftCache.RefArab, "");
        addLine(tl, "->", liftCache.BaseForm, "");
        addLine(tl, "->", liftCache.CrossString(), "");
        int senses = liftCache.Senses.size();
        for (int i = 0; i < senses; i++) {
            LiftCacheDefinition lcd = liftCache.Senses.get(i);
            addDefinition(tl, search, lcd, i, senses);
        }
        new SearchBackground().execute(liftCache.Original);
    }

    public void addDefinition(TableLayout tl, String search, LiftCacheDefinition lcd, int counter, int size) {
        String countStr = "";
        if (size > 1) {
            countStr = String.format(" (%d)", counter + 1);
        }
        addLine(tl, countStr, lcd.GlossDef(), search);
        addLine(tl, "=", lcd.Synonym, "");
        addLine(tl, "≠", lcd.Antonym, "");
        addLine(tl, entries.get("Examples"), lcd.ExamplesString(), search);
    }

    public void addLine(TableLayout tl, String label, String text, String search) {
        if (text != null && !text.isEmpty()) {
            if (search.isEmpty()) {
                search = text;
            }
            addRow(tl, label, text, search);
        }
    }

    // Searches for examples that match and displays all found, except the
    // examples of the current word
    public class SearchBackground extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... searchStr) {
            try {
                String search = Language.deAccent(searchStr[0]);
                exampleResults = new TreeMap<String, LiftCacheDefinition>();

                String regex = ".*\\b" + search + "\\b.*";
                for (Map.Entry<String, LiftCache> entry :
                        Translate.wordList.TranslationList.get(dest).entrySet()) {
                    if (isCancelled()) break;
                    for (LiftCacheDefinition sense : entry.getValue().Senses) {
                        for (Lift.ExampleStr ex : sense.Examples) {
                            if (Language.deAccent(ex.Example).matches(regex)) {
                                if (!entry.getKey().equals(Language.deAccent(liftCache.Original))) {
                                    exampleResults.put(entry.getValue().Original, sense);
                                    //publishProgress(0);
                                }
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
            for (Map.Entry<String, LiftCacheDefinition> entry : exampleResults.entrySet()) {
                usage += entry.getValue().ExamplesString() + "\n";
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            if (exampleResults.size() > 0) {
                TableLayout tl = (TableLayout) findViewById(R.id.tlWordDetail);
                addRow(tl, "*** " + entries.get("Additional") + " ***", "", "");
                for (Map.Entry<String, LiftCacheDefinition> entry : exampleResults.entrySet()) {
                    String word = entry.getKey();
                    String example = entry.getValue().ExamplesString();
                    // Create header with word
                    addRow(tl, word, "", word);
                    // Add example below
                    addRow(tl, "", example, word);
                }
            }
        }
    }

    // Adds one row to the tablelayout
    public void addRow(TableLayout tl, String label, String text, final String search) {
        TableRow tr = new TableRow(getApplicationContext());
        boolean hasLabel = label != null && !label.isEmpty();
        boolean hasText = text != null && !text.isEmpty();
        int span = (hasLabel && hasText) ? 1 : 2;
        if (hasLabel) {
            tr.addView(makeTextView(label, span, true));
        }
        if (hasText) {
            tr.addView(makeTextView(text, span, false));
        }
        tr.setOnClickListener(new AdapterView.OnClickListener() {
                                  public void onClick(View childView) {
                                      returnSearch(search, false);
                                  }
                              }
        );
        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public TextView makeTextView(String str, int span, boolean gray) {
        TextView tv = new TextView(getApplicationContext());
        tv.setText(str);
        TableRow.LayoutParams tlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(2, 2, 2, 2);
        tlp.span = span;
        tlp.weight = (gray && span == 1) ? 0f : 1f;
        if (gray) {
            if (span > 1) {
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
            }
            tv.setBackgroundColor(Color.LTGRAY);
        }
        tv.setLayoutParams(tlp);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(22);
        return tv;
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
