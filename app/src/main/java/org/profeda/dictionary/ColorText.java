package org.profeda.dictionary;

import android.text.Html;
import android.widget.TextView;

/**
 * Created by ineiti on 29/03/2017.
 */

public class ColorText {
    public String result;

    public ColorText(String str){
        result = str;
    }

    public ColorText(){
        result = "";
    }

    public ColorText addText(String str){
        result += str;
        return this;
    }

    public ColorText addTextColor(String str, String color){
        result += String.format("<font color='%s'>%s</font>", color, str);
        return this;
    }

    public void setText(TextView tv){
        tv.setText(Html.fromHtml(result), TextView.BufferType.SPANNABLE);
    }
}
