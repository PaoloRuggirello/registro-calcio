package com.elis.registrocalcio.dto;

import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.other.EncryptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import static com.elis.registrocalcio.model.security.SecurityToken.getTokenExpirationDateAsString;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenIdAsString;

public class Token {
    public String token;

    public Token(){
    }
    public Token(String token){
        this.token = token;
    }
    public Token(SecurityToken token){
        String tokenId = EncryptionUtils.encrypt(token.getId().toString());
        String partialToken = tokenId + "-%$-" + token.getExpirationDate();
        this.token = EncryptionUtils.encrypt(partialToken);
    }

    public void decrypt(){
        this.token = EncryptionUtils.decrypt(token);
        if(StringUtils.isEmpty(this.token))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_TOKEN.toString());
        String decryptedTokenId = EncryptionUtils.decrypt(getTokenIdAsString(this));
        if(StringUtils.isEmpty(decryptedTokenId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_TOKEN.toString());
        String decryptedExpirationDate = getTokenExpirationDateAsString(this);
        this.token = decryptedTokenId + "-%$-" +decryptedExpirationDate;
    }

    public void encrypt(){
        String id = getTokenIdAsString(this);
        String expirationDate = getTokenExpirationDateAsString(this);
        this.token = EncryptionUtils.encrypt(EncryptionUtils.encrypt(id) + "-%$-" + expirationDate);
    }
}
