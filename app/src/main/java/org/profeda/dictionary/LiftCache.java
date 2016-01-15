package org.profeda.dictionary;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds one entry for the LiftCache for easy serialisation
 * Only for a translation to a given language
 */
public class LiftCache implements Serializable {
    private static final long serialVersionUID = 3141592657316227L;
    // If we use a 'deAccent'ized map to point to here, we can
    // use this string to get back the original
    public String Original;
    public String Pronunciation;
    public String Searchable;
    public List<String> Cross;
    public List<LiftCacheDefinition> Senses;

    // Reads all translations stored in entry e and puts them
    // in the local map Translations
    public LiftCache(Lift.Entry e, Lift lift, String tr) {
        if (e.lexicalUnit != null &&
                e.lexicalUnit.form != null &&
                e.lexicalUnit.form.text != null) {
            Original = e.getOriginal();
            Pronunciation = e.getPronunciation();
            // Add all cross-references
            Cross = new ArrayList<String>();
            for (String c : e.getCross()) {
                Lift.Entry ref = lift.findById(c);
                if (ref != null) {
                    Cross.add(ref.getOriginal());
                } else {
                    System.out.println("LiftCache didn't find ref: " + c);
                }
            }
            Senses = new ArrayList<LiftCacheDefinition>();
            for (Lift.Entry.Sense s : e.getSenses()) {
                Senses.add(new LiftCacheDefinition(s, tr));
            }
        }
    }

    // Returns whether this entry matches the search string, storing
    // the de-accentized string in the cache
    public boolean matches(String search) {
        if (Searchable == null) {
            Searchable = Language.deAccent(Original);
        }
        return Searchable.matches(search );
        //return Searchable.indexOf(search) >= 0;
    }

    public String SensesToString() {
        String s = "";
        for (LiftCacheDefinition def : Senses) {
            s += def.String();
        }
        return s;
    }

    public String String() {
        return "Original: " + Original +
                "\nPronunciation: " + Pronunciation +
                "\nCross reference: " + Cross +
                "\nDefinitions: " + SensesToString() +
                "\n";
    }

    // Returns the glosses in a list or definitions if no gloss is found
    public List<String> TranslationString() {
        return LiftCache.concatDefinitionList(Senses);
    }

    // Returns the cross-references as a string
    public String CrossString() {
        return LiftCache.concatList(Cross);
    }

    // Returns a list<string> of all definitions in that LiftCache
    public static List<String> concatDefinitionList(List<LiftCacheDefinition> entries) {
        ArrayList<String> ret = new ArrayList<>();
        if (entries.size() == 0) {
            return null;
        } else {
            for (LiftCacheDefinition e : entries) {
                ret.add(e.String());
            }
        }
        return ret;
    }

    // Converts a list of strings to a numbered list if there are
    // more than 1 entries, else shows only the single entry. If
    // no entry is present, shows the alternative text or nothing
    // if that one is emtpy
    public static String concatList(List<String> entries) {
        ArrayList<String> ret = new ArrayList<>();
        if (entries.size() == 0) {
            return null;
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


    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(Original);
        out.writeObject(Pronunciation);
        out.writeObject(Cross);
        out.writeObject(Senses);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Original = (String) in.readObject();
        Pronunciation = (String) in.readObject();
        Cross = (List<String>) in.readObject();
        Senses = (List<LiftCacheDefinition>) in.readObject();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}
