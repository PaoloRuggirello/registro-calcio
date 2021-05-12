package com.elis.registrocalcio.other;

import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExceptionUtils {

    public static void throwResponseStatus(Class fromClass, HttpStatus httpStatus, FootballRegisterException reason, String moreInfo){
        Logger log = LogManager.getLogger(fromClass);
        log.error(reason +  moreInfo);
        throw new ResponseStatusException(httpStatus, reason.name());
    }

    public static void throwResponseStatus(Class fromClass, HttpStatus httpStatus, FootballRegisterException reason){
        throwResponseStatus(fromClass, httpStatus, reason, "");
    }
}
