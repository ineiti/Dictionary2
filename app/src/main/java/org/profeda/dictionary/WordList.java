package org.profeda.dictionary;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by ineiti on 19/10/2015.
 */
public class WordList {
    // The lift-file we're working with - only accessible through our
    // methods
    public Lift lift;
    // The base language of the lift-file
    public String LanguageBase;
    // All languages where 'base'-language is translated to
    public List<String> Languages;
    // This holds a list of all translations from source-language to the destination
    // languages, one map-entry per destination language
    Map<String, SortedMap<String, LiftCache>> TranslationList;
    // First entry is language, then a triple of
    // DeAccentized word, BackTrans
    public static class BackTrans implements Serializable{
        private static final long serialVersionUID = 3141592657316228L;
        public String BackOriginal;
        public String Source;
        public BackTrans(String bo, String src){
            this.BackOriginal = bo;
            this.Source = src;
        }

        private void writeObject(java.io.ObjectOutputStream out)
                throws IOException {
            out.writeObject(BackOriginal);
            out.writeObject(Source);
        }

        private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            BackOriginal = (String) in.readObject();
            Source = (String) in.readObject();
        }

    }
    Map<String, SortedMap<String, BackTrans>> BackTranslationList;
    // Create a version-int out of major, minor and patch
    public static int versionMajor = 1;
    public static int versionMinor = 7;
    public static int versionPatch = 0;
    public static int versionId = versionMajor * 0x10000 + versionMinor * 0x100 +
            versionPatch;
    // If versionTest is > 0, then this version is used when writing
    // the cache file, so the 'detect old' test can work out
    public int versionTest = 0;

    // New WordList - takes the name of a .lift-file
    // If the cache-file exists, reads it and does some checks on
    // whether it's still up-to-date. It uses the program-version,
    // size of the .lift-file and it's date.
    // If the cache-file doesn't exist, reads in .lift-file and
    // saves the cache-file.
    // Call like lift.ReadCache(new FileInputStream(name));
    public WordList(String cacheName, InputStream liftName) throws Exception {
        if (!LoadCache(cacheName)) {
            // If the cache file hasn't been loaded, we load the whole .lift-file
            // and write the new cache
            Log.i("WordList", "Loading lift-file ");
            InitLift(liftName);
            WriteCache(cacheName);
        }
    }

    // Mainly for test purposes, being able to load .lift-file from local
    // directory
    public WordList(String liftName) throws Exception {
        Log.i("WordList", System.getProperty("user.dir"));
        FileInputStream liftFile = new FileInputStream("src/main/assets/" + liftName);
        InitLift(liftFile);
        liftFile.close();
    }

    public boolean LoadCache(String cacheName) throws Exception {
        // Read in cache-file
        // Check whether it's up-to-date
        File cacheFile = new File(cacheName);
        // Check if cache is here
        if (cacheFile.exists()) {
            Log.i("WordList", "Loading cache-file " + cacheFile.getAbsolutePath());
            FileInputStream cache = new FileInputStream(cacheName);
            ObjectInputStream ois = new ObjectInputStream(cache);
            int cacheVersion = (int) ois.readObject();
            if (cacheVersion != versionId) {
                Log.d("Lift", "Version mismatch.");
                ois.close();
                cache.close();
            } else {
                Log.i("WordList", "Initializing internal variables");
                TranslationList = (Map<String, SortedMap<String, LiftCache>>) ois.readObject();
                LanguageBase = (String) ois.readObject();
                Languages = (List<String>) ois.readObject();
                BackTranslationList = (Map<String, SortedMap<String, BackTrans>>)ois.readObject();
                ois.close();
                return true;
            }
        }
        return false;
    }

    // Adds an entry to the liftcache-list, appending spaces to already available
    // entries
    public void AddEntry(SortedMap<String, LiftCache> tle, String src, LiftCache lc){
        // Hack to add multiple definitions by adding spaces
        while (tle.containsKey(src)) {
            src += " ";
        }
        tle.put(src, lc);
    }

    // Setting up the TranslationList - used when the cache is not here
    // yet.
    public void InitLift(InputStream name) throws Exception {
        lift = Lift.ReadLift(name);
        if (lift == null) {
            throw Exception.class.newInstance();
        }
        TranslationList = new HashMap<String, SortedMap<String, LiftCache>>();
        getLanguages();
        for (String lang : Languages) {
            SortedMap<String, LiftCache> tle = new TreeMap<>();
            for (Lift.Entry e : lift.entry) {
                if (e.lexicalUnit != null) {
                    String src = Language.deAccent(e.lexicalUnit.form.text.text);
                    LiftCache lc = new LiftCache(e, lift, lang);
                    tle.put(e.id, lc);
                }
            }
            TranslationList.put(lang, tle);
        }
        //SetupBackTranslation();
    }

    // Copying TranslationList to BackTranslationList, so that we can
    // find the definition of a word from a destination language
    public void SetupBackTranslation() {
        // First set up the BackTranslationList with one Treemap per destination language
        BackTranslationList = new HashMap<>();
        for (String lang : Languages) {
            BackTranslationList.put(lang, new TreeMap<String, BackTrans>());
        }

        // Now iterate over all languages and add all words to the backtranslation
        for (String lang : Languages) {
            System.out.println("Setting up backtranslation for " + lang);
            SortedMap<String, LiftCache> tl = TranslationList.get(lang);
            SortedMap<String, BackTrans> bt = BackTranslationList.get(lang);
            for (String wordSource : tl.keySet()) {
                String wordDest = Language.deAccent(tl.get(wordSource).Senses.get(0).Gloss);
                // Hack to add multiple definitions by adding spaces
                while (bt.containsKey(wordDest)) {
                    wordDest += " ";
                }
                BackTrans backtr = new BackTrans(tl.get(wordSource).Senses.get(0).Gloss,
                        tl.get(wordSource).Original);
                bt.put(wordDest, backtr);
            }
        }
    }

    // Writes the actual information to a file 'cache' for faster
    // loading
    public void WriteCache(String name) throws Exception {
        FileOutputStream cache = new FileOutputStream(name);
        ObjectOutputStream oos = new ObjectOutputStream(cache);
        // Allow for a different version in tests
        if (versionTest > 0) {
            oos.writeObject(versionTest);
        } else {
            oos.writeObject(versionId);
        }
        oos.writeObject(TranslationList);
        oos.writeObject(LanguageBase);
        oos.writeObject(getLanguages());
        oos.writeObject(BackTranslationList);
        oos.close();
    }

    public <V> SortedMap<String, V> filterPrefix(SortedMap<String, V> baseMap, String prefix) {
        if (prefix.length() > 0) {
            char nextLetter = (char) (prefix.charAt(prefix.length() - 1) + 1);
            String end = prefix.substring(0, prefix.length() - 1) + nextLetter;
            return baseMap.subMap(prefix, end);
        }
        return baseMap;
    }

    // Searches a word from the source-language and returns the destination
    // language 'dest' for all /^#{search}*/
    // Returns a map of words with LiftCaches
    public Map<String, LiftCache> searchWord(String searchOrig, String dest) {
        String search = Language.deAccent(searchOrig);
        Map<String, LiftCache> result = new TreeMap<String, LiftCache>();
        SortedMap<String, LiftCache> tl = TranslationList.get(dest);
        if (tl == null) {
            Log.i("searchWord", "Null-search" + search + " for language: " + dest);
        } else {
            Log.i("searchWord", search + " for language: " + dest);
            for (Map.Entry<String, LiftCache> entry: tl.entrySet()){
                if (entry.getValue().matches(search)){
                    result.put(entry.getKey(), entry.getValue());
                }
            }
//            for (Map.Entry<String, LiftCache> entry : filterPrefix(tl, search).entrySet()) {
//                result.put(entry.getKey(), entry.getValue());
//            }
        }
        return result;
    }

    // Searches a word from the source-language and returns the destination
    // language 'dest' for all /\b#{search}\b*/
    // Returns a map of words with LiftCaches
    public Map<String, LiftCache> searchExamples(String searchOrig, String dest) {
        String search = Language.deAccent(searchOrig);
        Map<String, LiftCache> result = new TreeMap<String, LiftCache>();

        String regex = ".*\\b" + search + "\\b.*";
        for (Map.Entry<String, LiftCache> entry :
                TranslationList.get(dest).entrySet()) {
            for (Lift.ExampleStr ex : entry.getValue().Senses.get(0).Examples) {
                System.out.println("   " + Language.deAccent(ex.Example));
                if (Language.deAccent(ex.Example).matches(regex)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    // Searches a word from the dest-language and returns the source-
    // language for all /#{search}*/
    public Map<String, BackTrans> searchWordSource(String searchOrig, String source) {
        String search = Language.deAccent(searchOrig);
        SortedMap<String, BackTrans> list = BackTranslationList.get(source);
        if (list == null) {
            Log.i("searchWordSource", "searching on null list " + source);
            return null;
        } else {
            return filterPrefix(list, search);
        }
    }

    // Searches for available languages - supposes that first 'entry' with a
    // 'lexical-unit' has all languages in either 'sense/gloss' or 'sense/definition'
    public List<String> getLanguages() {
        if (Languages != null) {
            return Languages;
        }
        Languages = new ArrayList<String>();
        if (lift.entry != null && lift.entry.size() > 0) {
            Lift.Entry.LexicalUnit lu = lift.entry.get(0).lexicalUnit;
            if (lu != null && lu.form != null) {
                Languages.add(lu.form.lang);
            }
            Lift.Entry.Sense sense = lift.entry.get(0).sense.get(0);
            if (sense != null) {
                for (Lift.Entry.Sense.Gloss g : sense.gloss) {
                    Languages.add(g.lang);
                }
            }
        }
        // We suppose the first language is the base language
        LanguageBase = Languages.remove(0);
        return Languages;
    }

    // Returns the full name of the language
    public static String langToFull(String lang) {
        return Language.langToFull(lang);
    }

    // Checks if any Right-to-Left characters are present
    public static boolean hasRightToLeft(String str) {
        char[] chars = str.toCharArray();
        for (char c : chars) {
            if (c >= 0x600 && c <= 0x6ff) {
                return true;
            }
        }
        return false;
    }
}