package org.profeda.dictionary;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;


/**
 * Created by ineiti on 05/08/2016.
 */

public class WordListTest {
    String liftName = "teda-fr-en-ar.161223.lift";
    String cacheName = "src/main/assets/teda.cache";

    @Test
    public void testCacheWrite() throws Exception {
        File f = new File(cacheName);
        f.delete();
        WordList wordList = new WordList(liftName, Translate.initLanguages());
        System.out.println("Wordlist loaded");
        wordList.WriteCache(cacheName);
        assertTrue(f.exists());
        System.out.println("Wordlist saved");
    }

    @Test
    public void testDictSortEval() throws Exception{
        assertEquals(1, WordList.dictSortEval("he", "he"));
        assertEquals(2, WordList.dictSortEval("he", "he eats"));
        assertEquals(3, WordList.dictSortEval("he", "head"));
        assertEquals(4, WordList.dictSortEval("he", "is it he"));
        assertEquals(4, WordList.dictSortEval("he", "is it he eating"));
        assertEquals(5, WordList.dictSortEval("he", "is it his head"));
        assertEquals(5, WordList.dictSortEval("he", "is it his head turning"));
    }

    @Test
    public void testDictSort() throws Exception{
        testDictSortResult("he", "he", "he eats");
        testDictSortResult("he", "he", "head");
        testDictSortResult("he", "head", "I saw he");
    }

    public void testDictSortResult(String search, String s1, String s2) throws Exception{
        assertEquals(-1, WordList.dictSort(search, s1, s2));
        assertEquals(1, WordList.dictSort(search, s2, s1));
        assertEquals(0, WordList.dictSort(search, s1, s1));
        assertEquals(0, WordList.dictSort(search, s2, s2));
    }
}
