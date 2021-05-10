package com.elis.registrocalcio.other;


import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateUtils {
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final String datePattern = "dd/MM/yyyy";
    public static final String hourPattern = "HH:mm";
    private static SimpleDateFormat completeDateFormatter;
    private static DateTimeFormatter hourDateTimeFormatter = DateTimeFormatter.ofPattern(hourPattern);
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);

    public static SimpleDateFormat getCompleteDateFormatter(){
        if(completeDateFormatter == null){
            completeDateFormatter = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        }
        return completeDateFormatter;
    }

    public static Instant StringToInstantConverter(String date){
        try{
            return DateUtils.getCompleteDateFormatter().parse(date).toInstant();
        } catch (Exception e){
            try{
                return Instant.parse(date);
            }catch (Exception e1){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.WRONG_DATE_FORMAT.toString());
            }
        }
    }

    public static String getDateFromInstant(Instant date){
        return date.atZone(ZoneId.of("GMT+2")).format(dateTimeFormatter);
    }
    public static String getHourFromInstant(Instant date){
        return date.atZone(ZoneId.of("GMT+2")).format(hourDateTimeFormatter);
    }

    public static boolean areInTheSameWeek(Instant date1, Instant date2){ //Date2 is Always after date1
        LocalDate date1L = LocalDate.ofInstant(date1, ZoneId.of("GMT+2"));
        LocalDate date2L = LocalDate.ofInstant(date2, ZoneId.of("GMT+2"));
        if(date2L.getDayOfWeek().getValue() < date1L.getDayOfWeek().getValue()){
            LocalDate temp = date2L;
            date2L = date1L;
            date1L = temp;
        }
        int result = date2L.getDayOfMonth() - date1L.getDayOfMonth();
        if(date1L.getYear() == date2L.getYear() && date1L.getMonth().getValue() == date2L.getMonth().getValue() && result > 0 &&  result < 7)
            return true;
        else
            return false;
    }
}
