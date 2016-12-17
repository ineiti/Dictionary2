package org.profeda.dictionary;

import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;
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
    public Entry findById(String id) {
        for (Entry e : entry) {
            if (e.id.equals(id)) {
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
        @ElementList(inline = true, required = false)
        public List<Trait> trait;
        @Element(required = false)
        public Field field;
        @Element(required = false)
        public RefArab refArab;
        @ElementList(inline = true, required = false)
        public List<Pronunciation> pronunciation;
        @ElementList(inline = true, required = false)
        public List<Sense> sense;
        @ElementList(inline = true, required = false)
        public List<Relation> relation;
        @Element(required = false)
        public Note note;
        @Element(required = false)
        public Etymology etymology;
        @Element(required = false)
        public Variant variant;


        // Returns the original string
        public String getOriginal() {
            return lexicalUnit.form.text.text;
        }

        // Searches for the pronunciation
        public String getPronunciation() {
            if (pronunciation != null) {
                if (pronunciation.size() > 0 && pronunciation.get(0) != null) {
                    Pronunciation p = pronunciation.get(0);
                    if (p.form != null && p.form.text != null) {
                        return p.form.text.text;
                    }
                }
            }
            return null;
        }

        // Returns the cross-references or null if none. The cross-reference
        // can be found by calling lift.findById(entry.getCross().get(0))
        public List<String> getCross() {
            List<String> defs = new ArrayList<String>();
            if (relation != null && relation.size() > 0) {
                for (Relation r : relation) {
                    if (r.ref != null) {
                        defs.add(r.ref);
                    }
                }
            }
            return defs;
        }

        // Returns the senses defined for this entry
        public List<Sense> getSenses() {
            return sense;
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
            @Element(required = false)
            public Form form;
        }

        @Root
        public static class Sense {
            @Attribute
            public String id;
            @Attribute(required = false)
            public String order;
            @ElementList(inline = true, required = false)
            public List<Gloss> gloss;
            @ElementList(inline = true, required = false)
            public List<Definition> definition;
            @ElementList(inline = true, required = false)
            public List<Example> example;
            @ElementList(inline = true, required = false)
            public List<Field> field;
            @ElementList(inline = true, required = false)
            public List<Relation> relation;
            @ElementList(inline = true, required = false)
            public List<Reversal> reversal;
            @Element(required = false, name = "grammatical-info")
            public GrammaticalInfo grammaticalInfo;
            @Element(required = false)
            public Illustration illustration;
            @Element(required = false)
            public Trait trait;

            // Searches for the gloss
            public String getGloss(String translation) {
                if (gloss != null) {
                    for (Gloss g : gloss) {
                        if (g.lang.equals(translation)) {
                            return g.text.text;
                        }
                    }
                }
                //System.out.println("Get gloss missing: " + translation + ":" + lexicalUnit.form.text.text);
                return "";
            }

            // Returns the definition
            public String getDefinition(String translation) {
                if (definition != null) {
                    for (Definition d : definition) {
                        for (Form f : d.form) {
                            if (f.lang.equals(translation)) {
                                return f.text.text;
                            }
                        }
                    }
                }
                return null;
            }

            // Returns a list of Examples
            public List<ExampleStr> getExamples(String translation) {
                List<ExampleStr> ex = new ArrayList<ExampleStr>();
                if (example != null) {
                    for (Example e : example) {
                        if (e != null && e.form != null) {
                            if (e.translation == null || e.translation.form == null) {
                                // Some examples don't have a translation
                                ex.add(new ExampleStr(e.form.text.text, ""));
                            } else {
                                for (Form f : e.translation.form) {
                                    if (f.lang.equals(translation)) {
                                        ex.add(new ExampleStr(e.form.text.text, f.text.text));
                                    }
                                }
                            }
                        }
                    }
                }
                return ex;
            }

            // Returns a field
            public String getField(String fi) {
                if (field != null) {
                    for (Field f : field) {
                        if (f.type.equals(fi)) {
                            return f.form.get(0).text.text;
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


            @Root
            public static class Gloss {
                @Attribute
                public String lang;
                @Element
                public TextC text;
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
            public static class Relation{
                @Attribute
                public String ref;
                @Attribute
                public String type;
            }

            @Root
            public static class Reversal{
                @Attribute
                public String type;
                @ElementList(inline = true)
                public List<Form> form;
                @Element(required = false)
                public Main main;

                @Root
                public static class Main{
                    @Element
                    public Form form;
                }
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
                    @ElementList(required = false, inline = true)
                    public List<Form> form;
                    @Attribute(required = false)
                    public String type;
                }
            }

            @Root
            public static class GrammaticalInfo {
                @Attribute
                public String value;
                @ElementList(inline = true, required = false)
                public List<Trait> trait;
            }

            @Root
            public static class Illustration{
                @Attribute
                public String href;
                @Element(required = false)
                public Label label;

                @Root
                public static class Label{
                    @Element
                    public Form form;
                }
            }

        }

        @Root
        public static class Note{
            @ElementList(inline=true)
            public List<Form> form;
        }

        @Root
        public static class Relation {
            @Attribute
            public String type;
            @Attribute
            public String ref;
            @Attribute(required = false)
            public String order;
            @ElementList(inline = true, required = false)
            public List<Trait> trait;
        }

        @Root
        public static class RefArab {
            @Attribute
            public String value;
        }

        @Root
        public static class Etymology{
            @Attribute
            public String type;
            @Attribute
            public String source;
            @ElementList(inline = true, required = false)
            public List<Form> form;
        }

        @Root
        public static class Variant{
            @Element(required = false)
            public Form form;
            @Element
            public Trait trait;
        }
    }

    @Root
    public static class Form {
        @Element(required = false)
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
        @Text(required = false)
        public String text;
    }

    @Root
    public static class Field {
        @Attribute(required = false)
        public String tag;
        @Attribute(required = false)
        public String type;
        @ElementList(inline = true)
        public List<Form> form;
    }

    public static class ExampleStr implements Serializable {
        private static final long serialVersionUID = 3141592657316229L;
        public String Example;
        public String Translation;

        public ExampleStr(String ex, String tr) {
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
            Example = (String) in.readObject();
            Translation = (String) in.readObject();
        }

        private void readObjectNoData()
                throws ObjectStreamException {
        }

    }
}
