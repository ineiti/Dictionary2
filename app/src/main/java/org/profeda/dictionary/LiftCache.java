package org.profeda.dictionary;

import android.text.TextUtils;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds one entry for the LiftCache for easy serialisation
 * Only for a translation to a given language
 */
public class LiftCache implements Serializable {
    private static final long serialVersionUID = 3141592657316227L;
    // If Full is non-null, it points to the entry where this sub-text
    // has been taken. I.e. "abi ndoduro" will create two entries: one
    // full and one with "ndoduro", pointing to "abi ndoduro".
    // These are called link-entries.
    public String Full;
    // If we use a 'deAccent'ized map to point to here, we can
    // use this string to get back the original
    public String Original;
    public String Gloss;
    public String Pronunciation;
    public List<String> Definitions;
    public List<Lift.Example> Examples;
    public String Synonym;
    public String Antonym;
    public List<String> Cross;

    // Reads all translations stored in entry e and puts them
    // in the local map Translations
    public LiftCache(Lift.Entry e, Lift lift, String tr) {
        if (e.lexicalUnit != null &&
                e.lexicalUnit.form != null &&
                e.lexicalUnit.form.text != null) {
            Original = e.getOriginal();
            Gloss = e.getGloss(tr);
            Pronunciation = e.getPronunciation();
            Definitions = e.getDefinitions(tr);
            Examples = e.getExamples(tr);
            Synonym = e.getSynonym();
            Antonym = e.getAntonym();
            Cross = e.getCross(lift);
        }
    }

    // This creates a LiftCache based on another LiftCache
    public LiftCache(LiftCache lc) {
        Full = Language.deAccent(lc.Original);
    }

    public String String() {
        return "Original: " + Original +
                "\nGloss: " + Gloss +
                "\nPronunciation: " + Pronunciation +
                "\nDefinitions: " + Definitions +
                "\nExamples: " + Examples +
                "\nSynonym: " + Synonym +
                "\nAntonym: " + Antonym +
                "\nCross reference: " + Cross +
                "\n";
    }

    // Returns the definitions in a list or gloss if no definition found
    public String TranslationString() {
        return LiftCache.concatList(Definitions, Gloss);
    }

    // Returns the examples in a list
    public String ExamplesString() {
        List<String> examplesList = new ArrayList<String>();
        for (Lift.Example ex : Examples) {
            examplesList.add(ex.Example + " -> " + ex.Translation);
        }
        return LiftCache.concatList(examplesList);
    }

    // Returns the cross-references as a string
    public String CrossString() {
        return LiftCache.concatList(Cross);
    }

    // Converts a list of strings to a numbered list if there are
    // more than 1 entries, else shows only the single entry. If
    // no entry is present, shows the alternative text or nothing
    // if that one is emtpy
    public static String concatList(List<String> entries, String alternative) {
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

    public static String concatList(List<String> entries) {
        return concatList(entries, "");
    }


    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(Full);
        if (Full == null) {
            // Only write other fields if it's not a link-entry
            out.writeObject(Original);
            out.writeObject(Gloss);
            out.writeObject(Pronunciation);
            out.writeObject(Definitions);
            out.writeObject(Examples);
            out.writeObject(Synonym);
            out.writeObject(Antonym);
            out.writeObject(Cross);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Full = (String) in.readObject();
        if (Full == null) {
            // Only read other fields if it's not a link-entry
            Original = (String) in.readObject();
            Gloss = (String) in.readObject();
            Pronunciation = (String) in.readObject();
            Definitions = (List<String>) in.readObject();
            Examples = (List<Lift.Example>) in.readObject();
            Synonym = (String) in.readObject();
            Antonym = (String) in.readObject();
            Cross = (List<String>) in.readObject();
        }
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}
