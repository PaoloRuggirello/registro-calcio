package com.elis.registrocalcio.other;


import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtils {

    private static Key key;
    private static Logger log = LogManager.getLogger(EncryptionUtils.class);

    public static String encrypt(String toEncrypt){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            return Base64Utils.encodeToString(cipher.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.warn("Cannot Encrypt: {}", toEncrypt);
        }
        return null;
    }

    public static String decrypt(String toDecrypt){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return new String(cipher.doFinal(Base64Utils.decodeFromString(toDecrypt)));
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.INVALID_TOKEN.toString());
        }
    }

    private static Key getKey() throws NoSuchAlgorithmException {
        if(key == null) {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            key = generator.generateKey();
        }
        return key;
    }

}
