package org.profeda.dictionary;

import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Root
public class Lift {
    @Attribute
    public String producer;
    @Element
    public Header header;
    @ElementList(inline = true)
    public List<Entry> entry;
    @Attribute
    public String version;

    // Searches the lift-file in the local directory - only
    // usable for debugging purposes!
    public static Lift ReadLift(String name) throws Exception {
        Serializer serializer = new Persister();
        File source = new File("src/main/assets/" + name);
        Lift l = serializer.read(Lift.class, source);
        return l;
    }

    // Searches the lift-file through an inputstream, which is
    // what you get usually from the ressources
    public static Lift ReadLift(InputStream source) throws Exception {
        Serializer serializer = new Persister();
        return serializer.read(Lift.class, source);
    }

    // Searches for an entry with id
    public Entry findById(String id){
        for (Entry e: entry){
            if (e.id.equals(id)){
                return e;
            }
        }
        return null;
    }

    @Root
    public static class Header {
        @Element
        public Ranges ranges;

        @Element
        public Fields fields;

        @Root
        public static class Ranges {
            @ElementList(inline = true)
            public List<Range> range;

            @Root
            public static class Range {
                @Attribute
                public String id;
                @Attribute
                public String href;
            }
        }

        @Root
        public static class Fields {
            @ElementList(inline = true)
            public List<Field> field;
        }
    }

    @Root
    public static class Entry {
        @Attribute
        public String id;
        @Attribute
        public String dateCreated;
        @Attribute
        public String dateModified;
        @Attribute
        public String guid;
        @Attribute(required = false)
        public String dateDeleted;
        @Attribute(required = false)
        public String order;

        @Element(name = "lexical-unit", required = false)
        public LexicalUnit lexicalUnit;
        @Element(required = false)
        public Trait trait;
        @ElementList(inline = true, required = false)
        public List<Pronunciation> pronunciation;
        @ElementList(inline = true, required = false)
        public List<Sense> sense;
        @ElementList(inline = true, required = false)
        public List<Relation> relation;

        // Returns the original string
        public String getOriginal(){
            return lexicalUnit.form.text.text;
        }

        // Searches for the gloss
        public String getGloss(String translation) {
            if (sense.size() > 0) {
                for (Sense s : sense) {
                    if (s.gloss != null) {
                        for (Sense.Gloss g : s.gloss) {
                            if (g.lang.equals(translation)) {
                                return g.text.text;
                            }
                        }
                    }
                }
            }
            //System.out.println("Get gloss missing: " + translation + ":" + lexicalUnit.form.text.text);
            return "";
        }

        // Searches for the pronunciation
        public String getPronunciation() {
            if (pronunciation != null) {
                if (pronunciation.size() > 0 && pronunciation.get(0) != null) {
                    return pronunciation.get(0).form.text.text;
                }
            }
            return null;
        }

        // Returns a list of definitions
        public List<String> getDefinitions(String translation) {
            List<String> defs = new ArrayList<String>();
            if (sense.size() > 0) {
                for (Sense s : sense) {
                    if (s.definition != null) {
                        for (Sense.Definition d : s.definition) {
                            for (Form f : d.form) {
                                if (f.lang.equals(translation)) {
                                    defs.add(f.text.text);
                                }
                            }
                        }
                    }
                }
            }
            return defs;
        }

        // Returns a list of Examples
        public List<Example> getExamples(String translation) {
            List<Example> ex = new ArrayList<Example>();
            if (sense.size() > 0) {
                for (Sense s : sense) {
                    if (s.example != null) {
                        for (Sense.Example e : s.example) {
                            if (e != null && e.form != null) {
                                if (e.translation == null) {
                                    // Some examples don't have a translation
                                    ex.add(new Lift.Example(e.form.text.text, ""));
                                } else {
                                    for (Form f : e.translation.form) {
                                        if (f.lang.equals(translation)) {
                                            ex.add(new Example(e.form.text.text, f.text.text));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return ex;
        }

        // Returns a field
        public String getField(String field) {
            if (sense.size() > 0) {
                for (Sense s : sense) {
                    if (s.field != null) {
                        for (Field f : s.field) {
                            if (f.type.equals(field)) {
                                return f.form.text.text;
                            }
                        }
                    }
                }
            }
            return null;
        }

        // Returns the synonym or null if none
        public String getSynonym() {
            return getField("syn");
        }

        // Returns the antonym or null if none
        public String getAntonym() {
            return getField("ant");
        }

        // Returns the cross-references or null if none
        public List<String> getCross(Lift lift){
            List<String> defs = new ArrayList<String>();
            if (relation != null && relation.size() > 0) {
                for (Relation r : relation) {
                    if (r.ref != null) {
                        Entry rel = lift.findById(r.ref);
                        if (rel != null) {
                            defs.add(rel.getOriginal());
                        }
                    }
                }
            }
            return defs;
        }

        @Root
        public static class LexicalUnit {
            @Element
            public Form form;
        }

        @Root
        public static class Trait {
            @Attribute
            public String name;
            @Attribute
            public String value;
        }

        @Root
        public static class Pronunciation {
            @Element
            public Form form;
        }

        @Root
        public static class Sense {
            @Attribute
            public String id;
            @ElementList(inline = true, required = false)
            public List<Gloss> gloss;
            @ElementList(inline = true, required = false)
            public List<Definition> definition;
            @ElementList(inline = true, required = false)
            public List<Example> example;
            @ElementList(inline = true, required = false)
            public List<Field> field;
            @Element(required = false, name = "grammatical-info")
            public GrammaticalInfo grammaticalInfo;

            @Root
            public static class Gloss {
                @Attribute
                public String lang;
                @Element
                public TextC text;
            }

            @Root
            public static class Definition {
                @ElementList(inline = true)
                public List<Form> form;
            }

            @Root
            public static class Example {
                @Element(required = false)
                public Form form;
                //@ElementList(inline = true, required = false)
                @Element(required = false)
                public Translation translation;

                @Root
                public static class Translation {
                    @ElementList(inline = true)
                    public List<Form> form;
                }
            }

            @Root
            public static class GrammaticalInfo {
                @Attribute
                public String value;
            }
        }

        @Root
        public static class Relation {
            @Attribute
            public String type;
            @Attribute
            public String ref;
        }
    }


    @Root
    public static class Form {
        @Element
        public TextC text;
        @Attribute(required = false)
        public String lang;
        @Element(required = false)
        public Annotation annotation;

        @Root
        public static class Annotation {
            @Attribute
            public String name;

            @Attribute
            public String value;
        }
    }

    @Root
    public static class TextC {
        @Text
        public String text;
    }

    @Root
    public static class Field {
        @Attribute(required = false)
        public String tag;
        @Attribute(required = false)
        public String type;
        @Element
        public Form form;
    }

    public static class Example  implements Serializable {
        public String Example;
        public String Translation;

        public Example(String ex, String tr) {
            this.Example = ex;
            this.Translation = tr;
        }

        private void writeObject(java.io.ObjectOutputStream out)
                throws IOException {
            out.writeObject(Example);
            out.writeObject(Translation);
        }

        private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            Example = (String)in.readObject();
            Translation = (String)in.readObject();
        }

        private void readObjectNoData()
                throws ObjectStreamException {
        }

    }
}
