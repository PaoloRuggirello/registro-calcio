package com.example.registrocalcio.other;

import java.util.regex.Pattern;

public class Utils {

    public static final String format = "yyyy-MM-dd HH:mm:ss";
    //Regex used to validate email address
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
}
