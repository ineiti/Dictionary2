package org.profeda.dictionary;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Created by ineiti on 19/10/2015.
 */
public class WordList {
    // The lift-file we're working with - only accessible through our
    // methods
    public Lift lift;
    // The base language of the lift-file
    public String LanguageBase;
    // This holds a list of all translations from source-language to the destination
    // languages, one map-entry per destination language
    Map<String, SortedMap<String, LiftCache>> TranslationList;
    // Create a version-int out of major, minor and patch
    public static int versionMajor = 1;
    public static int versionMinor = 9;
    public static int versionPatch = 3;
    public static int versionId = versionMajor * 0x10000 + versionMinor * 0x100 +
            versionPatch;
    // If versionTest is > 0, then this version is used when writing
    // the cache file, so the 'detect old' test can work out
    public int versionTest = 0;

    // Directly loading the cache
    public WordList(InputStream cache) throws Exception {
        Log.i("wl", "Loading wordlist");
        ObjectInputStream ois = new ObjectInputStream(cache);
        int cacheVersion = (int) ois.readObject();
        if (cacheVersion != versionId) {
            System.out.println("Cache-version mismatch");
            Log.d("Lift", "Version mismatch.");
            ois.close();
        } else {
            Log.i("WordList", "Initializing internal variables");
            System.out.println("Initializing internal variables");
            TranslationList = (Map<String, SortedMap<String, LiftCache>>) ois.readObject();
            LanguageBase = (String) ois.readObject();
            ois.close();
            Log.i("WordList", "Language-size:" + String.valueOf(TranslationList.size()));
            Log.i("WordList", "Language-size:" + String.valueOf(TranslationList.get("en").size()));
        }
    }

    // Mainly for test purposes, being able to load .lift-file from local
    // directory
    public WordList(String liftName, Languages l) throws Exception {
        Log.i("WordList", System.getProperty("user.dir"));
        FileInputStream liftFile = new FileInputStream("app/src/test/java/org/profeda/dictionary/" + liftName);
        InitLift(liftFile, l);
        liftFile.close();
    }

    public boolean LoadCache(String cacheName) throws Exception {
        // Read in cache-file
        // Check whether it's up-to-date
        File cacheFile = new File(cacheName);
        // Check if cache is here
        if (cacheFile.exists()) {
            System.out.println("Loading cache-file");
            Log.i("WordList", "Loading cache-file 1.2 " + cacheFile.getAbsolutePath());
            FileInputStream cache = new FileInputStream(cacheName);
            ObjectInputStream ois = new ObjectInputStream(cache);
            int cacheVersion = (int) ois.readObject();
            if (cacheVersion != versionId) {
                System.out.println("Cache-version mismatch");
                Log.d("Lift", "Version mismatch.");
                ois.close();
                cache.close();
            } else {
                Log.i("WordList", "Initializing internal variables");
                System.out.println("Initializing internal variables");
                TranslationList = (Map<String, SortedMap<String, LiftCache>>) ois.readObject();
                LanguageBase = (String) ois.readObject();
                ois.close();
                Log.i("WordList", "Language-size:" + String.valueOf(TranslationList.size()));
                Log.i("WordList", "Language-size:" + String.valueOf(TranslationList.get("en").size()));
                return true;
            }
        } else {
            System.out.println("Didn't find cache-file");
        }
        return false;
    }

    // Adds an entry to the liftcache-list, appending spaces to already available
    // lang
    public void AddEntry(SortedMap<String, LiftCache> tle, String src, LiftCache lc) {
        // Hack to add multiple definitions by adding spaces
        while (tle.containsKey(src)) {
            src += " ";
        }
        tle.put(src, lc);
    }

    // Setting up the TranslationList - used when the cache is not here
    // yet.
    public void InitLift(InputStream name, Languages l) throws Exception {
        lift = Lift.ReadLift(name);
        System.out.println("Lift is read");
        if (lift == null) {
            throw Exception.class.newInstance();
        }
        TranslationList = new HashMap<String, SortedMap<String, LiftCache>>();
        for (String lang : l.LiftList()) {
//            System.out.println("Language: " + lang.toString());
            SortedMap<String, LiftCache> tle = new TreeMap<>();
            for (Lift.Entry e : lift.entry) {
                if (e.lexicalUnit != null) {
                    LiftCache lc = new LiftCache(e, lift, lang);
                    tle.put(e.id, lc);
                }
            }
            TranslationList.put(lang, tle);
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
        final String search = deAccent(searchOrig);
        // Comparator for implementing dictionary sorting.
        Comparator<String> dictionarySort = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return WordList.dictSort(search, s1, s2);
            }
        };
        Map<String, LiftCache> result = new TreeMap<String, LiftCache>(dictionarySort);
        SortedMap<String, LiftCache> tl = TranslationList.get(dest);
        if (tl == null) {
            Log.i("searchWord", "Null-search" + searchOrig + " for language: " + dest);
        } else {
            Pattern reg = Pattern.compile(".*\\b" + search + ".*", Pattern.CASE_INSENSITIVE);
            Log.i("searchWord", search + " for language: " + dest);


//            int found = 0;
            for (Map.Entry<String, LiftCache> entry : tl.entrySet()) {
                if (entry.getValue().matches(reg)) {
//                    Log.i("Found", entry.getKey());
                    String word = entry.getValue().Searchable;
                    while (result.containsKey(word)) {
                        word += " ";
                    }
                    result.put(word, entry.getValue());
//                    found++;
//                    if (found > 10){
//                        break;
//                    }
                }
            }
        }
        return result;
    }

    // If searching for 'he', the wanted order is:
    // he(1), he eats(2), he swims(2), head(3), hell(3), as he said(4), as a head(5)
    // So, if available
    // 1. the word only
    // 2. the word with additions
    // 3. words starting with the search-pattern
    // 4. the search-pattern as a word in other phrases
    // 5. the search-pattern in other phrases
    public static int dictSort(String search, String s1, String s2) {
        int sort1 = dictSortEval(search, s1);
        int sort2 = dictSortEval(search, s2);
//        Log.i("Sorting", search + " " + s1 + " " + s2 + ":" +
//                String.valueOf(sort1) + String.valueOf(sort2));
        if (sort1 < sort2) {
            return -1;
        } else if (sort2 < sort1) {
            return 1;
        }
        return s1.compareTo(s2);
    }

    // Evaluates the string
    public static int dictSortEval(String search, String s) {
        if (s.equals(search)) {
            return 1;
        } else if (s.startsWith(search + " ")) {
            return 2;
        } else if (s.startsWith(search)) {
            return 3;
        } else if (s.contains(" " + search + " ") || s.endsWith(" " + search)) {
            return 4;
        }
        return 5;
    }

    // Searches a word from the source-language and returns the destination
    // language 'dest' for all /\b#{search}\b*/
    // Returns a map of words with LiftCaches
    public Map<String, LiftCache> searchExamples(String searchOrig, String dest) {
        String search = deAccent(searchOrig);
        Map<String, LiftCache> result = new TreeMap<String, LiftCache>();

        String regex = ".*\\b" + search + "\\b.*";
        for (Map.Entry<String, LiftCache> entry :
                TranslationList.get(dest).entrySet()) {
            for (Lift.ExampleStr ex : entry.getValue().Senses.get(0).Examples) {
                System.out.println("   " + deAccent(ex.Example));
                if (deAccent(ex.Example).matches(regex)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    // Searches a word from the dest-language and returns the source-
    // language for all /#{search}*/
    public Map<String, LiftCache> searchWordSource(String searchOrig, String source) {
        final String search = deAccent(searchOrig);
        Comparator<String> dictionarySort = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return WordList.dictSort(search, s1, s2);
            }
        };
        Map<String, LiftCache> result = new TreeMap<String, LiftCache>(dictionarySort);
        SortedMap<String, LiftCache> tl = TranslationList.get(source);
        if (tl == null) {
            Log.i("searchWordSource", "Null-search" + searchOrig + " for language: " + source);
        } else {
            Pattern reg = Pattern.compile(".*\\b" + search + ".*", Pattern.CASE_INSENSITIVE);
            Log.i("searchWordSource", search + " for language: " + source);
            for (Map.Entry<String, LiftCache> entry : tl.entrySet()) {
                List<String> results = entry.getValue().matchesSenses(reg);
                if (results.size() > 0) {
                    for (String res : results) {
                        String word = res;
                        while (result.containsKey(word)) {
                            word += " ";
                        }
                        result.put(word, entry.getValue());
                    }
                }
            }
        }
        return result;
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

    // Remove all accents from string.
    public static String deAccent(String str) {
        if (str == null) {
            return "";
        }
        String nfdNormalizedString = Normalizer.normalize(str.toLowerCase(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String ret = pattern.matcher(nfdNormalizedString).replaceAll("");
        ret = ret.replaceAll("[()]", "");
        ret = Pattern.compile("[\\p{P}\\p{Mn}]").matcher(ret).replaceAll("");
        ret = ret.replaceAll("ŋ", "n");
        ret = ret.replaceAll("œ", "oe");
        ret = ret.trim();
        return ret;
    }
}