package org.profeda.dictionary;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by ineiti on 11/09/2015.
 */
public class LiftTest {
    public WordList wordList;
    private String liftName = "teda-fr-en-ar.lift";
    private String cacheName = "src/main/assets/teda.cache";

    @Test
    public void testLiftLoad() throws Exception {
        loadWholeFile();
        System.out.println("File is loaded");
        System.out.println("Lift version " + wordList.lift.version);
        System.out.println("WordList version " + wordList.versionId);
        System.out.println(wordList.lift.entry.get(0).sense.get(0).gloss.get(1).lang);
        assert (wordList.lift.version.equals("0.13"));
        assert (wordList.versionId == 0x10400);
    }

    @Test
    public void testLoadAndSearchWord() throws Exception {
        loadWholeFile();
        searchWord();
    }

    public void searchWord() throws Exception {
        List<String> translations = Arrays.asList("a", "kirki", "kubo turti");

        for (String s : translations) {
            Map<String, LiftCache> e = wordList.searchWord(s, "en");
            if (e.size() == 0) {
                System.out.println("Didn't find " + s);
            } else {
                for (Map.Entry<String, LiftCache> ent : e.entrySet()) {
                    System.out.println(ent.getValue().Original + " means " + ent.getValue().Gloss);
                }
            }
        }
    }

    @Test
    public void testLanguages() throws Exception {
        wordList = new WordList("test-languages-1.lift");
        System.out.println(wordList.Languages);
        assert (wordList.Languages.size() == 3);
    }

    @Test
    public void testCacheWrite() throws Exception {
        File f = new File(cacheName);
        f.delete();
        wordList = new WordList(liftName);
        System.out.println("Wordlist loaded");
        wordList.WriteCache(cacheName);
        assert (f.exists());
    }

    @Test
    public void testCacheRead() throws Exception {
        testCacheWrite();
        assert (wordList.LoadCache(cacheName));
        searchWord();
    }

    @Test
    public void testVersionMismatch() throws Exception {
        loadWholeFile();
        wordList.versionTest = 2;
        wordList.WriteCache(cacheName);
        assert (!wordList.LoadCache(cacheName));
    }

    @Test
    public void testDefinitions() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchWord("borsu", "en");
        System.out.println(result.get("borsu").Definitions);
        assert (result.get("borsu").Definitions.size() == 2);
    }

    @Test
    public void testDeAccent() throws Exception {
        loadWholeFile();

        Map<String, LiftCache> arabic = wordList.searchWord("aci", "ayl");
        String arabic_str = arabic.get("aci").Gloss;
        System.out.println(arabic_str);
        System.out.println(Language.deAccent(arabic_str));

        String ret = Language.deAccent("hello(there)");
        assert (ret.equals("hellothere"));
    }

    @Test
    public void testMultiDest() throws Exception {
        loadWholeFile();

        Map<String, LiftCache> fromTudaga1 = wordList.searchWord("adigibi", "en");
        Map<String, LiftCache> fromTudaga2 = wordList.searchWord("aci", "en");
        System.out.println(fromTudaga1.get("adigibi").String());
        System.out.println(fromTudaga2.get("aci").String());

        Map<String, WordList.BackTrans> old = wordList.searchWordSource("old woman", "en");
        System.out.println(old);
        assert (old.size() == 2);
    }

    @Test
    public void testMultiSource() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> fromTudaga1 = wordList.searchWord("bidi", "en");
        System.out.println(fromTudaga1);
        System.out.println(fromTudaga1.get("bidi").String());
        System.out.println(fromTudaga1.get("bidi ").String());
        System.out.println(fromTudaga1.get("bidi  ").String());

        // There should be 'bidi', 'bîdi' and 'bîdibidi'
        assert (fromTudaga1.size() == 4);
    }

    @Test
    public void testSearchExample() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchExamples("huna", "en");
        System.out.println(result.size());
        System.out.println(result);

        // Actual results in lift-file - will probably change with next
        // lift-file...
        assert (result.size() == 25);
    }

    @Test
    public void testLearnRegexp() {
        String src1 = "1: english words\n2: more words";
        String src2 = "more words";
        Matcher m1 = Pattern.compile("(1: )*([^ ]+)").matcher(src1);
        m1.find();

        System.out.println(m1.group(1));
        System.out.println(m1.group(2));
    }

    @Test
    public void testArabic() throws Exception{
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchWord("abba", "ayl");
        LiftCache abba = result.get("abba");
        String arab = abba.Gloss;
        String search1 = "اب";
        String search2 = "أَبٌ";

        System.out.println(abba);
        System.out.println(arab);
        System.out.println(Language.deAccent(search2));
        System.out.println(Language.deAccent(arab));
        System.out.println();
    }

    @Test
    public void testArabic2() throws Exception{
        loadWholeFile();
        String search1 = "اب";
        String search2 = "أَبٌ";
        Map<String, WordList.BackTrans> result = wordList.searchWordSource(search1, "ayl");
        System.out.println(result);
        result = wordList.searchWordSource(search2, "ayl");
        System.out.println(result);
    }

    private void loadWholeFile() throws Exception {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        wordList = new WordList(liftName);
    }
}
