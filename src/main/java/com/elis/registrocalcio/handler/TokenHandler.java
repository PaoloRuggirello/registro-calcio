package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenExpirationDate;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenId;

@Service
public class TokenHandler {

    @Autowired
    SecurityTokenRepository securityTokenRepository;

    public Token createToken(User whoNeedToken){
        Optional<SecurityToken> currentToken = securityTokenRepository.findByUsername(whoNeedToken.getUsername());
        if(currentToken.isEmpty()) {
            SecurityToken token = new SecurityToken(whoNeedToken.getUsername(), whoNeedToken.getRole(), newExpirationDate());
            return new Token(securityTokenRepository.save(token));
        } else if (currentToken.get().getExpirationDate().isBefore(Instant.now())){ //If token expired update token expiration date
            currentToken.get().setExpirationDate(newExpirationDate());
            return new Token(securityTokenRepository.save(currentToken.get()));
        }
        return new Token(currentToken.get());
    }

    public void deleteToken(Token toDelete){
        try {
            if(!StringUtils.isEmpty(toDelete.token))
                toDelete.decrypt();
                securityTokenRepository.deleteById(getTokenId(toDelete));
        }catch (EmptyResultDataAccessException e){
            System.out.println("Token not found, cannot delete");
        }
    }

    public SecurityToken checkToken(Token token){
        token.decrypt();
        if(StringUtils.isEmpty(token.token) || getTokenExpirationDate(token).isBefore(Instant.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_TOKEN.toString());
        Optional<SecurityToken> securityToken = securityTokenRepository.findById(getTokenId(token));
        if(securityToken.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_TOKEN.toString());
        return securityToken.get();
    }
    public SecurityToken checkToken(Token token, Role permissionLevel){
        SecurityToken securityToken = checkToken(token);
        Role role = securityToken.getRole();
        if(role == Role.USER && permissionLevel == Role.ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED.toString());
        return securityToken;
    }
    public void checkIfAreTheSameUser(Token token, String username){
        SecurityToken securityToken = checkToken(token);
        if(!securityToken.getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED.toString());
    }
    public void checkIfAreTheSameUser(Token token, String username, Role role){
        SecurityToken securityToken = checkToken(token, role);
        if(!securityToken.getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED.toString());
    }

    private Instant newExpirationDate(){
        return Instant.now().plus(15, ChronoUnit.MINUTES);
//        return Instant.now();
    }
}
