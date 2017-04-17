package org.profeda.dictionary;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;


/**
 * Created by ineiti on 11/09/2015.
 */
public class LiftTest {
    public WordList wordList;
    private String liftName = "teda-fr-en-ar.161223.lift";
    private String cacheName = "app/src/main/assets/teda.cache";

    @Test
    public void testCacheWrite() throws Exception {
        System.out.println(new java.io.File( "." ).getCanonicalPath());
        File f = new File(cacheName);
        f.delete();
        wordList = new WordList(liftName, Translate.initLanguages());
        System.out.println("Wordlist loaded");
        wordList.WriteCache(cacheName);
        assertTrue(f.exists());
    }

    @Test
    public void testRefTudaga() throws Exception{
        loadWholeFile();
        Map<String, LiftCache> results = wordList.searchWord("cilab", "tuq");
        LiftCache result = results.get("cilab");
        System.out.println(result.RefTudaga);
        assertTrue(result.RefTudaga.contains("bigi2"));
    }

    @Test
    public void testId() throws Exception{
        loadWholeFile();
        Map<String, LiftCache> results = wordList.searchWord("kowus", "tuq");
        LiftCache result = results.get("kowus");
        System.out.println("RefTudaga is: " + result.RefTudaga);
        assertTrue(result.RefTudaga.contains("mûyi"));
    }

    @Test
    public void testLiftLoad() throws Exception {
        loadWholeFile();
        System.out.println("File is loaded");
        System.out.println("Lift version " + wordList.lift.version);
        System.out.println("WordList version " + wordList.versionId);
        System.out.println(wordList.lift.entry.get(0).sense.get(0).gloss.get(1).lang);
        assertEquals("0.13", wordList.lift.version);
        assertEquals(0x10802, wordList.versionId);
    }


    @Test
    public void testLoadAndSearchWord() throws Exception {
        loadWholeFile();
        searchWord();
    }

    @Test
    public void testSearch() throws Exception{
        loadWholeFile();
        List<String> translations = Arrays.asList("ali", "kirki", "kubo turti");

        for (String s : translations) {
            Map<String, LiftCache> e = wordList.searchWord(s, "en");
            assertTrue(e.size() > 0);
            for (Map.Entry<String, LiftCache> ent : e.entrySet()) {
                System.out.println(ent.getValue().Original + " means " + ent.getValue().String());
            }
        }
    }

    @Test
    public void testSearch2() throws Exception{
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchWord("kirki", "en");
        System.out.println(result.size());
        assertTrue(result.size() == 1);
        for (Map.Entry<String, LiftCache> ent : result.entrySet()) {
            System.out.println(ent.getValue().Original + " means " + ent.getValue().String());
        }
        result = wordList.searchWord("a", "en");
        System.out.println(result.size());
        assertTrue(result.size() == 128);
        for (Map.Entry<String, LiftCache> ent : result.entrySet()) {
            System.out.println(ent.getValue().Original + " means " + ent.getValue().String());
        }
    }

    @Test
    public void testRegexp(){
        String sentence = "The quick brown fox";
        Pattern p1 = Pattern.compile(".*\\bfox", Pattern.CASE_INSENSITIVE);
        Pattern p11 = Pattern.compile(".*\\box", Pattern.CASE_INSENSITIVE);
        Pattern p2 = Pattern.compile(".*\\bThe.*", Pattern.CASE_INSENSITIVE);
        Pattern p21 = Pattern.compile(".*\\bhe.*", Pattern.CASE_INSENSITIVE);

        assertTrue(p1.matcher(sentence).matches());
        assertTrue(!p11.matcher(sentence).matches());
        assertTrue(p2.matcher(sentence).matches());
        assertTrue(!p21.matcher(sentence).matches());
    }

    public void searchWord() throws Exception {
        List<String> translations = Arrays.asList("ali", "kirki", "kubo turti");

        for (String s : translations) {
            Map<String, LiftCache> e = wordList.searchWord(s, "en");
            if (e.size() == 0) {
                System.out.println("Didn't find " + s);
            } else {
                for (Map.Entry<String, LiftCache> ent : e.entrySet()) {
                    System.out.println(ent.getValue().Original + " means " + ent.getValue().String());
                }
            }
        }
    }


    public void searchWordBack() throws Exception {
        List<String> translations = Arrays.asList("house", "man", "woman");

        for (String s : translations) {
            Map<String, LiftCache> bt = wordList.searchWordSource(s, "en");
            if (bt.size() == 0) {
                System.out.println("Didn't find " + s);
            } else {
                for (Map.Entry<String, LiftCache> ent : bt.entrySet()) {
                    System.out.println(ent.getKey() + " means " +
                            ent.getValue().Original);
                }
            }
        }
    }

    @Test
    public void testDouble() throws Exception{
        loadWholeFile();
        Map<String, LiftCache>results = wordList.searchWord("ndogusi", "en");
        LiftCache ndogusi = results.get("ndogusi_4030259a-3b36-41ce-bfbf-f4ae4855db6b");
        assertTrue(ndogusi.Senses.size() == 2);
        System.out.println(ndogusi.Senses.get(0).GlossDef());
        System.out.println(ndogusi.Senses.get(1).GlossDef());
    }

    @Test
    public void testLanguages() throws Exception {
        wordList = new WordList("test-languages-1.lift", Translate.initLanguages());
        System.out.println(wordList.LanguageBase);
//        assertTrue(wordList.Languages.size() == 3);
    }

    @Test
    public void testCacheRead() throws Exception {
        testCacheWrite();
        assertTrue(wordList.LoadCache(cacheName));
        searchWord();
    }

    @Test
    public void testCacheWordList() throws Exception{
        testCacheWrite();
        WordList wl = new WordList(cacheName, null);
        assertTrue(wl.TranslationList.size() > 0);
        searchWord();
    }

    @Test
    public void testBackTranslation() throws Exception{
        testCacheWrite();
        WordList wl = new WordList(cacheName, null);
        //loadWholeFile();
        //WordList wl = wordList;
//        assertTrue(wl.BackTranslationList.size() > 0);
//        assertTrue(wl.BackTranslationList.get("en").size() > 0);
        searchWordBack();
    }

    @Test
    public void testVersionMismatch() throws Exception {
        loadWholeFile();
        wordList.versionTest = 2;
        wordList.WriteCache(cacheName);
        assertTrue(!wordList.LoadCache(cacheName));
    }

    @Test
    public void testDefinitions() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchWord("borsu", "en");
        LiftCache lc = result.get(result.keySet().toArray()[0]);
        System.out.println(result);
        System.out.println(lc);
        List<LiftCacheDefinition> s = lc.Senses;
        System.out.println(s.get(0).Definition);
        System.out.println(s.get(1).Definition);
        assertTrue(s.get(0).Definition.equals("alone"));
        assertTrue(s.get(1).Definition.equals("only"));
    }

    @Test
    public void testSenses() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchWord("borsu", "en");
        List<LiftCacheDefinition> s = result.get("borsu").Senses;
        System.out.println(s);
        assertTrue(s.size() == 2);
    }

    @Test
    public void testDeAccent() throws Exception {
        loadWholeFile();

        Map<String, LiftCache> arabic = wordList.searchWord("aci", "ayl");
        String arabic_str = arabic.get("aci").String();
        System.out.println(arabic_str);
        System.out.println(WordList.deAccent(arabic_str));

        String ret = WordList.deAccent("hello(there)");
        assertTrue(ret.equals("hellothere"));
    }

    @Test
    public void testRefArab() throws Exception{
        loadWholeFile();

        Map<String, LiftCache> melti = wordList.searchWord("melti", "en");
        System.out.println(melti);
        System.out.println(melti.get("melti_55d65bb1-cdc9-40e5-af8d-5bcbc40df8b2").String());
    }

    @Test
    public void testMultiDest() throws Exception {
        loadWholeFile();

        Map<String, LiftCache> fromTudaga1 = wordList.searchWord("adigibi", "en");
        Map<String, LiftCache> fromTudaga2 = wordList.searchWord("aci", "en");
        System.out.println(fromTudaga1.get("adigibi").String());
        System.out.println(fromTudaga2.get("aci").String());

        Map<String, LiftCache> old = wordList.searchWordSource("^old woman$", "en");
        System.out.println(old);
        assertTrue(old.size() == 2);

        old = wordList.searchWordSource("^dead$", "en");
        System.out.println(old);
        assertTrue(old.size() == 3);
    }


    @Test
    public void testMultiSource() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> fromTudaga1 = wordList.searchWord("^bidi$", "en");
        System.out.println(fromTudaga1);
        System.out.println(fromTudaga1.get("bidi").String());
        System.out.println(fromTudaga1.get("bidi ").String());
        System.out.println(fromTudaga1.get("bidi  ").String());

        // There should be 'bidi', 'bîdi' and 'bîdibidi'
        assertTrue(fromTudaga1.size() == 4);
    }

//    @Test
    public void testSearchExample() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchExamples("huna", "en");
        System.out.println(result.size());
        System.out.println(result);

        // Actual results in lift-file - will probably change with next
        // lift-file...
        assertTrue(result.size() == 25);
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
    public void testArabic() throws Exception {
        loadWholeFile();
        Map<String, LiftCache> result = wordList.searchWord("abba", "ayl");
        LiftCache abba = result.get("abba");
        String arab = abba.String();
        String search1 = "اب";
        String search2 = "أَبٌ";

        System.out.println(abba);
        System.out.println(arab);
        System.out.println(WordList.deAccent(search2));
        System.out.println(WordList.deAccent(arab));
        System.out.println();
    }

    @Test
    public void testArabic2() throws Exception {
        loadWholeFile();
        String search1 = "اب";
        String search2 = "أَبٌ";
        Map<String, LiftCache> result = wordList.searchWordSource(search1, "ayl");
        System.out.println(result);
        result = wordList.searchWordSource(search2, "ayl");
        System.out.println(result);
    }

    @Test
    public void testResultsSource() throws Exception {
        loadWholeFile();
        String search = "play";
        Map<String, LiftCache> result = wordList.searchWordSource(search, "en");
        System.out.println(result);
        LiftCache play = result.get("slide down play game");
        System.out.println(play.Original);
        System.out.println(play.Senses.toString());
    }

    private void loadWholeFile() throws Exception {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        wordList = new WordList(liftName, Translate.initLanguages());
    }
}
