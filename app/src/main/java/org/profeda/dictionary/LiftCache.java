package org.profeda.dictionary;

import android.text.TextUtils;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.profeda.dictionary.Translate.wordList;

/**
 * Holds one entry for the LiftCache for easy serialisation
 * Only for a translation to a given language
 */
public class LiftCache implements Serializable {
    private static final long serialVersionUID = 3141592657316229L;
    // If we use a 'deAccent'ized map to point to here, we can
    // use this string to get back the original
    public String Original;
    public String Pronunciation;
    public String Searchable;
    public String RefTudaga;
    public String BaseForm;
    public List<String> SearchableSenses;
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
            if (e.relation != null && tr == "tuq") {
                for (Lift.Entry.Relation r : e.relation) {
                    if (r.type.equals("RefTudaga")) {
                        Lift.Entry rt = lift.findById(r.ref);
                        if (rt == null) {
                            System.out.println("Didn't find " + e.getOriginal() + ":" + r.ref);
                        } else {
                            RefTudaga = rt.getOriginal();
//                            System.out.println("Found RefTudaga " + e.getOriginal() + "->" + RefTudaga);
                        }
                    }
                }
            }
            // Add all cross-references
            Cross = new ArrayList<String>();
            for (String c : e.getCross()) {
                Lift.Entry ref = lift.findById(c);
                if (ref != null) {
                    Cross.add(ref.getOriginal());
                } else {
//                    System.out.println("LiftCache in -"+e.getOriginal()+"- didn't find cross-ref: " + c);
                }
            }
            Senses = new ArrayList<LiftCacheDefinition>();
            if (e.sense == null) {
//                System.out.println("No sense for: " + e.getOriginal());
            } else {
                for (Lift.Entry.Sense s : e.getSenses()) {
                    LiftCacheDefinition lcd = new LiftCacheDefinition(s, tr);
                    if (lcd.GlossDef() == "") {
//                        System.out.println("Empty sense for " + Original);
                    } else {
                        Senses.add(lcd);
                    }
                }
            }
            Searchable = WordList.deAccent(Original);
            SearchableSenses = new ArrayList<>();
            for (LiftCacheDefinition def : Senses) {
                SearchableSenses.add(WordList.deAccent(def.GlossDef()));
            }
        }
    }

    // Returns whether this entry matches the search string, storing
    // the de-accentized string in the cache
    public boolean matches(Pattern reg) {
        return reg.matcher(Searchable).matches();
    }

    // Returns whether a string matches partially or completely the entry.
    public boolean matchesString(String str){
        Pattern reg = Pattern.compile(".*\\b" + str + ".*", Pattern.CASE_INSENSITIVE);
        return reg.matcher(Searchable).matches();
    }

    // Returns whether the pattern matches any of the gloss/definition
    // and returns a list of all matching definitions
    public List<String> matchesSenses(Pattern reg) {
        List<String> ret = new ArrayList<>();
        for (String s : SearchableSenses) {
            if (reg.matcher(s).matches()) {
                ret.add(s);
            }
        }
        return ret;
    }

    public String SensesToString() {
        String s = "";
        for (LiftCacheDefinition def : Senses) {
            s += def.GlossDef() + " ";
        }
        return s;
    }

    public String GetFirstSense() {
        if (Senses.size() > 0) {
            return Senses.get(0).GlossDef();
        }
        return "";
    }

    public String String() {
        String ret = "Original: " + Original +
                "\nPronunciation: " + Pronunciation +
                "\nCross reference: " + Cross +
                "\nDefinitions: " + SensesToString() +
                "\n";
        if (RefTudaga != null) {
            ret += "Refarab: " + RefTudaga + "\n";
        }
        return ret;
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
    // more than 1 lang, else shows only the single entry. If
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
                ColorText line = new ColorText("");
                line.addTextColor(Integer.toString(nbr), "#ff8888");
                line.addText(": " + e);
                ret.add(line.result);
                nbr++;
            }
        }
        return TextUtils.join("<br>", ret);
    }


    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(Original);
        out.writeObject(Pronunciation);
        out.writeObject(RefTudaga);
        out.writeObject(Cross);
        out.writeObject(Senses);
        out.writeObject(Searchable);
        out.writeObject(SearchableSenses);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Original = (String) in.readObject();
        Pronunciation = (String) in.readObject();
        RefTudaga = (String) in.readObject();
        Cross = (List<String>) in.readObject();
        Senses = (List<LiftCacheDefinition>) in.readObject();
        Searchable = (String)in.readObject();
        SearchableSenses = (List<String>)in.readObject();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}
