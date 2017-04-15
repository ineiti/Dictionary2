package org.profeda.dictionary;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by ineiti on 13/11/2015.
 */
public class Languages implements Serializable {
    private static final long serialVersionUID = 3141592657326330L;
    private int directionIndex = 0;
    private Language base;
    private ArrayList<Language> translations = new ArrayList<>();

    // Initialize languages with the base-language.
    public Languages(Language t) {
        base = t;
    }

    // First language added needs to be the base-language
    public void AddLanguage(Language t) {
        translations.add(t);
    }

    public int transNbr() {
        return translations.size();
    }

    // Directions are:
    // Base -> language_1
    // Base -> language_2
    // ...
    // language_1 -> Base
    // language_2 -> Base
    // ...
    public void SetDirection(int d) {
        directionIndex = d % (2 * transNbr());
    }

    // Returns the direction as numerical index.
    public int GetDirection() {
        return directionIndex;
    }

    // Returns the current source-language.
    public Language Src() {
        if (BaseSrc()) {
            return base;
        } else {
            return translations.get(directionIndex % transNbr());
        }
    }

    // Returns the current destination-language.
    public Language Dst() {
        if (BaseDst()) {
            return base;
        } else {
            return translations.get(directionIndex % transNbr());
        }
    }

    // Returns the list of known languages in lift-format.
    public List<String> LiftList() {
        List<String> ret = new ArrayList<>();
        ret.add(base.Lang_Lift);
        for (Language l : translations) {
            ret.add(l.Lang_Lift);
        }
        return ret;
    }

    // Returns a list of posible translations from/to base-language.
    public List<String> TranslationList() {
        List<String> listItems = new ArrayList<String>();
        for (Language l : translations) {
            listItems.add(base.Lang_Full + " -> " +
                    l.Lang_Full);
        }
        for (Language l : translations) {
            listItems.add(l.Lang_Full + " -> " +
                    base.Lang_Full);
        }
        return listItems;
    }

    // Returns true if the base is the source.
    public boolean BaseSrc() {
        return directionIndex < transNbr();
    }

    // Returns true if the base is the destination.
    public boolean BaseDst() {
        return !BaseSrc();
    }

    // Inverses source- and destination-language.
    public void ChangeDirection() {
        SetDirection(transNbr() + directionIndex);
    }

    public static class Language implements Serializable {
        private static final long serialVersionUID = 3141592657316330L;
        public String Lang_Lift;
        public String Lang_Abbr;
        public String Lang_Full;
        public String Meaning;
        public String Examples;
        public String Additional;
        public String Search;
        public String Color;
        public String Pick;

        public Language(String lift, String abbr, String full, String meaning, String examples,
                        String additional, String search, String color, String pick) {
            Lang_Lift = lift;
            Lang_Abbr = abbr;
            Lang_Full = full;
            Meaning = meaning;
            Examples = examples;
            Additional = additional;
            Search = search;
            Color = color;
            Pick = pick;
        }

//        private void writeObject(java.io.ObjectOutputStream out)
//                throws IOException {
//            out.writeObject(Lang_Lift);
//            out.writeObject(Lang_Abbr);
//            out.writeObject(Lang_Full);
//            out.writeObject(Meaning);
//            out.writeObject(Examples);
//            out.writeObject(Additional);
//            out.writeObject(Search);
//            out.writeObject(Color);
//        }
//
//        private void readObject(java.io.ObjectInputStream in)
//                throws IOException, ClassNotFoundException {
//            Lang_Lift = (String) in.readObject();
//            Lang_Abbr = (String) in.readObject();
//            Lang_Full = (String) in.readObject();
//            Meaning = (String) in.readObject();
//            Examples = (String) in.readObject();
//            Additional = (String) in.readObject();
//            Search = (String) in.readObject();
//            Color = (String) in.readObject();
//        }

        private void readObjectNoData()
                throws ObjectStreamException {
        }
    }
}
