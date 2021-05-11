package com.elis.registrocalcio.other;

import com.elis.registrocalcio.enumPackage.ChangeType;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
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
    private final String antPattern = "EEEEE dd MMMMM";
    private final String postPattern = "HH:mm";

    private final String mailFrom = "registro.calcio.elis@yandex.com";
    private final String footer = "\n\nBuon divertimento,\n Registro calcio ELIS.\n\n" +
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
    public void comunicateNewEventToMailList(List<String> mailList, String category, Instant eventDate){
        try {
            String antDate = getAntDate(eventDate);
            String postDate = getPostDate(eventDate);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(convertMailList(mailList));
            message.setSubject(category + " " + antDate);
            message.setText("Gentile utente,\n registro calcio ELIS è felice di comunicarti che è stato creato un nuovo evento.\n\n Dettagli: " +
                    category + " - " + antDate + " ore " + postDate + footer);
            mailSender.send(message);
        }catch (Exception e ){
            System.out.println("Cannot send email");
        }
    }

    @Async
    public void comunicateTeamToMailList(List<String> mailList, String team, String category, Instant eventDate){
        try{
            String antDate = getAntDate(eventDate);
            String postDate = getPostDate(eventDate);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(convertMailList(mailList));
            message.setSubject("Assegnazione team " + category + " "+ antDate);
            message.setText("Gentile utente,\n registro calcio ELIS è felice di comunicarti che nella partita di " + category + " che si terrà " + antDate + " alle " + postDate +
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
            String postDate = getPostDate(eventDate);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(convertMailList(mailList));
            message.setSubject("Modifica Evento " + category + " " + antDate + " " + postDate);
            message.setText("Gentile utente,\nregistro calcio ELIS ti comunica che l'evento a cui ti sei iscritto (" + category + " - " + antDate + " " + postDate +") è stato "
                    + changeType + ".");
            if(changeType.equals(ChangeType.MODIFY)){
                message.setText(message.getText() + "\nIl nuovo evento si terrà sul campo di " + newEvent.getCategory() + " alle " + getAntDate(newEvent.getDate()) + " " + getPostDate(newEvent.getDate()) + ".");
            }
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
            message.setText("Ciao " + user.getName() + ", \n registro calcio ELIS è lieto di darti il benvenuto.\nIl tuo username per l'accesso al portale è: " + user.getUsername() + "." +
                    footer);
            mailSender.send(message);
        }catch (Exception e ){
            System.out.println("Cannot send email");
        }
    }

    private String getAntDate(Instant eventDate){
        SimpleDateFormat antPatternFormat = new SimpleDateFormat(antPattern, new Locale("it", "IT"));
        return antPatternFormat.format(Date.from(eventDate));
    }
    private String getPostDate(Instant eventDate){
        SimpleDateFormat postPatternFormat = new SimpleDateFormat(postPattern, new Locale("it", "IT"));
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
