package org.profeda.dictionary;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by ineiti on 13/11/2015.
 */
public class Language {
    // Maps the different entries to translations for the UI
    // Defines the translations for the user-interface
    public static HashMap<String, HashMap<String, String>> EntryToLanguage(){
        final HashMap<String, String> trEnglish = new HashMap<String, String>() {{
            put("Gloss", "Gloss");
            put("Pronunciation", "Pronunciation");
            put("Definitions", "Definitions");
            put("Examples", "Examples");
            put("Synonym", "Synonym");
            put("Antonym", "Antonym");
            put("CrossRef", "Cross reference");
            put("Usage", "Usage");
        }};
        final HashMap<String, String> trFrench = new HashMap<String, String>() {{
            put("Gloss", "Courte");
            put("Pronunciation", "Prononciation");
            put("Definitions", "Définitions");
            put("Examples", "Exemples");
            put("Synonym", "Synonym");
            put("Antonym", "Antonym");
            put("CrossRef", "Référence\ncroisée");
            put("Usage", "Utilisation");
        }};
        return new HashMap<String, HashMap<String, String>>() {{
            put("en", trEnglish);
            put("fr", trFrench);
            put("tuq", trEnglish);
            put("ayl", trEnglish);
        }};
    }

    // Returns the translation of an entry for the ui
    public static HashMap<String, String> uiTranslations(String lang) {
        return Language.EntryToLanguage().get(lang);
    }

    // Returns the translation of an entry for the ui
    public static String uiTranslation(String lang, String entry){
        return Language.uiTranslations(lang).get(entry);
    }

    // Returns the full name of the language
    public static String langToFull(String lang) {
        switch (lang) {
            case "tuq":
                return "Tudaga";
            case "ayl":
                return "عربي";
            case "fr":
                return "Français";
            case "en":
                return "English";
            default:
                return lang;
        }
    }

    public static String deAccent(String str) {
        if (str == null){
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
