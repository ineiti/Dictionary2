package org.profeda.dictionary;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * LiftCacheDefinition holds one definition of a word for one language
 */
public class LiftCacheDefinition implements Serializable {
    private static final long serialVersionUID = 3141592657316228L;
    public String Gloss;
    public String Pronunciation;
    public String Definition;
    public String Synonym;
    public String Antonym;
    public List<Lift.ExampleStr> Examples;

    // Constructor reading in all elements of a Lift.Entry.Sense for
    // one translation
    public LiftCacheDefinition(Lift.Entry.Sense s, String tr) {
        Gloss = s.getGloss(tr);
        Definition = s.getDefinition(tr);
        Examples = s.getExamples(tr);
        Synonym = s.getSynonym();
        Antonym = s.getAntonym();
    }

    // Returns the examples in a list
    public String ExamplesString() {
        List<String> examplesList = new ArrayList<String>();
        for (Lift.ExampleStr ex : Examples) {
            examplesList.add(ex.Example + " -> " + ex.Translation);
        }
        return LiftCache.concatList(examplesList);
    }

    // Returns all information in a string
    public String String() {
        return "\nGloss:" + Gloss +
                "\nDefinition: " + Definition +
                "\nPronunciation: " + Pronunciation +
                "\nSynonym: " + Synonym +
                "\nAntonym: " + Antonym +
                "\nExamples: " + ExamplesString() +
                "\n";
    }

    // Returns the gloss, or if it doesn't exist, the Definition
    public String GlossDef() {
        if (Definition != null && Definition != "") {
            return Definition;
        } else if (Gloss != null && Gloss != "") {
            return Gloss;
        } else {
            System.out.println("No GlossDef for " + this.String());
            return "";
        }
    }

    // Used for serialisation
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(Gloss);
        out.writeObject(Pronunciation);
        out.writeObject(Definition);
        out.writeObject(Synonym);
        out.writeObject(Antonym);
        out.writeObject(Examples);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Gloss = (String) in.readObject();
        Pronunciation = (String) in.readObject();
        Definition = (String) in.readObject();
        Synonym = (String) in.readObject();
        Antonym = (String) in.readObject();
        Examples = (List<Lift.ExampleStr>) in.readObject();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}
