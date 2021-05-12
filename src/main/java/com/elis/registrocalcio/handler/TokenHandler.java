package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.other.ExceptionUtils;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenExpirationDate;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenId;

@Service
public class TokenHandler {

    @Autowired
    SecurityTokenRepository securityTokenRepository;
    private static final Logger log = LogManager.getLogger(TokenHandler.class);

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
            log.info("Logging out user. {}", toDelete);
            securityTokenRepository.deleteById(getTokenId(toDelete));
        }catch (EmptyResultDataAccessException e){
            log.warn("Token not found, cannot delete {}", toDelete);
        }
    }

    public SecurityToken checkToken(Token token){
        token.decrypt();
        log.info("Checking token: {}", token);
        if(StringUtils.isEmpty(token.token) || getTokenExpirationDate(token).isBefore(Instant.now()))
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_TOKEN);
        Optional<SecurityToken> securityToken = securityTokenRepository.findById(getTokenId(token));
        if(securityToken.isEmpty())
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_TOKEN);
        return securityToken.get();
    }
    public SecurityToken checkToken(Token token, Role permissionLevel){
        log.info("Checking token");
        SecurityToken securityToken = checkToken(token);
        log.info("Needed [{}] - token info: {}", permissionLevel, securityToken);
        Role role = securityToken.getRole();
        if(role == Role.USER && permissionLevel == Role.ADMIN)
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED);
        return securityToken;
    }
    public void checkIfAreTheSameUser(Token token, String username){
        SecurityToken securityToken = checkToken(token);
        if(!securityToken.getUsername().equals(username))
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED);
    }
    public void checkIfAreTheSameUser(Token token, String username, Role role){
        SecurityToken securityToken = checkToken(token, role);
        if(!securityToken.getUsername().equals(username))
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED);
    }

    private Instant newExpirationDate(){
        return Instant.now().plus(15, ChronoUnit.MINUTES);
    }

    public void save(SecurityToken securityToken){
        securityTokenRepository.save(securityToken);
    }
}
