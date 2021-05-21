package com.elis.registrocalcio.other;

import com.elis.registrocalcio.enumPackage.ChangeType;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender mailSender;
    private static Logger log = LogManager.getLogger(EmailServiceImpl.class);


    private final String mailFrom = "registro.calcio.elis@yandex.com";
    private final String footer = "\n\nBuon divertimento,\nRegistro calcio ELIS.\n\n" +
            "Email generata automaticamente, non rispondere a questa email, se hai bisogno di ulteriore supporto contatta uno degli incaricati.";

    @Async
    public void passwordRecovery(String userName, String userEmail ,String tempPassword){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(userEmail);
        message.setSubject("Procedura recupero password");
        message.setText("Gentile " + userName + ", è stata avviata la procedura di recupero credenziali. \n" +
                "La tua attuale password è: " + tempPassword + ". \n" +
                "Puoi scegliere se continuare ad utilizzare questa o cambiarla con una a tuo piacimento, nel caso tu volessi cambiarla recati nella sezione 'profilo' della tua homePage." +
                footer);
        mailSender.send(message);
    }

    @Async
    public void communicateNewEventToMailList(List<String> mailList, String category, Instant eventDate){
        try {
            String antDate = getAntDate(eventDate);
            String postDate = getPostDate(eventDate);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(convertMailList(mailList));
            log.info("Sendin email to mail list: {}", mailList);
            message.setSubject(category + " " + antDate);
            message.setText("Gentile utente,\nregistro calcio ELIS è felice di comunicarti che è stato creato un nuovo evento.\n\nDettagli: " +
                    category + " - " + antDate + " ore " + postDate + footer);
            mailSender.send(message);
        }catch (Exception e){
            System.out.println("Cannot send email");
            log.error("Cannot sand email to mail List. Mail list: {} {}", mailList, e);
        }
    }

    @Async
    public void communicateTeamToMailList(List<String> mailList, String team, String category, Instant eventDate){
        try{
            String antDate = getAntDate(eventDate);
            String postDate = getPostDate(eventDate);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(convertMailList(mailList));
            message.setSubject("Assegnazione team " + category + " "+ antDate);
            message.setText("Gentile utente,\nregistro calcio ELIS è felice di comunicarti che nella partita di " + category + " che si terrà " + antDate + " alle " + postDate +
                    " farai parte del " + team + " team!" + footer);
            mailSender.send(message);
        }catch (Exception e ){
            System.out.println("Cannot send email");
        }
    }

    @Async
    public void communicateChangeToMailList(List<String> mailList, ChangeType changeType, String category, Instant eventDate, Event newEvent){
        try{
            String antDate = getAntDate(eventDate);
            String postDate = DateUtils.getHourFromInstant(eventDate);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(convertMailList(mailList));
            message.setSubject(ChangeType.abstractType(changeType) + " evento " + category + " " + antDate + " " + postDate);
            message.setText("Gentile utente,\nregistro calcio ELIS ti comunica che l'evento a cui ti sei iscritto (" + category + " - " + antDate + " " + postDate +") è stato "
                    + changeType + ".");
            if(changeType.equals(ChangeType.MODIFY)){
                message.setText(message.getText() + "\nIl nuovo evento si terrà sul campo di " + newEvent.getCategory() + " " + getAntDate(newEvent.getDate()) + " alle " + DateUtils.getHourFromInstant(newEvent.getDate()) + ".");
            }
            message.setText(message.getText() + footer);
            mailSender.send(message);
        }catch (Exception e ){
            System.out.println("Cannot send email");
        }
    }

    public void communicateRemoval(String appointed, String email, Event event) {
        try{
            String antDate = getAntDate(event.getDate());
            String postDate = DateUtils.getHourFromInstant(event.getDate());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("Iscrizione cancellata da " + event.getCategory() + " " + antDate + " " + postDate);
            message.setText("Gentile utente,\nregistro calcio ELIS ti comunica che la tua iscrizione all'evento di " + event.getCategory() + " - " + antDate + " " + postDate +") è stata cancellata da " + appointed + ".");
            message.setText(message.getText() + footer);
            mailSender.send(message);
        }catch (Exception e ){
            System.out.println("Cannot send email");
        }
    }

    @Async
    public void welcomeUser(User user){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.getEmail());
            message.setSubject("Registro calcio ELIS ti dà il benvenuto");
            message.setText("Ciao " + user.getName() + ",\nregistro calcio ELIS è lieto di darti il benvenuto.\nIl tuo username per l'accesso al portale è: " + user.getUsername() + "." +
                    footer);
            mailSender.send(message);
        }catch (Exception e ){
            System.out.println("Cannot send email");
        }
    }

    private String getAntDate(Instant eventDate){
        SimpleDateFormat antPatternFormat = new SimpleDateFormat(DateUtils.antPattern, new Locale("it", "IT"));
        return antPatternFormat.format(Date.from(eventDate));
    }
    private String getPostDate(Instant eventDate){
        SimpleDateFormat postPatternFormat = new SimpleDateFormat(DateUtils.hourPattern, new Locale("it", "IT"));
        return postPatternFormat.format(Date.from(eventDate));
    }
    private String[] convertMailList(List<String> mailList){
        String[] result = new String[mailList.size()];
        for(int i = 0; i < mailList.size(); i++){
            result[i] = mailList.get(i);
        }
        return result;
    }


}
