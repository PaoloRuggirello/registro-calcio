package com.elis.registrocalcio.other;


import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {

    public static final String format = "yyyy-MM-dd HH:mm:ss";
    //Regex used to validate email address
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static SimpleDateFormat formatter;

    public static SimpleDateFormat getFormatter(){
        if(formatter == null){
            formatter = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss", Locale.ENGLISH);
        }
        return formatter;
    }
}
