package org.profeda.dictionary;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
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
    public String Gloss;
    public String Pronunciation;
    public List<String> Definitions;
    public List<Lift.Example> Examples;
    public String Synonym;
    public String Antonym;

    // Reads all translations stored in entry e and puts them
    // in the local map Translations
    public LiftCache(Lift.Entry e, String tr) {
        if (e.lexicalUnit != null &&
                e.lexicalUnit.form != null &&
                e.lexicalUnit.form.text != null) {
            Original = e.lexicalUnit.form.text.text;
            Gloss = e.getGloss(tr);
            Pronunciation = e.getPronunciation();
            Definitions = e.getDefinitions(tr);
            Examples = e.getExamples(tr);
            Synonym = e.getSynonym();
            Antonym = e.getAntonym();
        }
    }

    public String String(){
        return "Original: " + Original +
                "\nGloss: " + Gloss +
                "\nPronunciation: " + Pronunciation +
                "\nDefinitions: " + Definitions +
                "\nExamples: " + Examples +
                "\nSynonym: " + Synonym +
                "\nAntonym: " + Antonym + "\n";
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(Original);
        out.writeObject(Gloss);
        out.writeObject(Pronunciation);
        out.writeObject(Definitions);
        out.writeObject(Examples);
        out.writeObject(Synonym);
        out.writeObject(Antonym);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Original = (String)in.readObject();
        Gloss = (String)in.readObject();
        Pronunciation = (String)in.readObject();
        Definitions = (List<String>)in.readObject();
        Examples = (List<Lift.Example>)in.readObject();
        Synonym = (String)in.readObject();
        Antonym = (String)in.readObject();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}
