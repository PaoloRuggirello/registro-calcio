package com.elis.registrocalcio.other;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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


    public void sendEmail(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo("ruggirello99@live.it");
        message.setSubject("Test invio mail from springboot");
        message.setText("Questo è il contenuto della mail");
        mailSender.send(message);
    }

    public void passwordRecovery(String userName, String userEmail ,String tempPassword){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(userEmail);
        message.setSubject("Procedura recupero password");
        message.setText("Gentile " + userName + ", è stata avviata la procedura di recupero password per il tuo accont. \n" +
                "La tua password è stata resettata, la tua attuale password è: " + tempPassword + ". \n" +
                "Puoi scegliere se continuare ad utilizzare questa o cambiarla con una a tuo piacimento, nel caso tu volessi cambiarla recati nella schermata di login e clicca sul pulsante 'Cambia password' e inserisci i dati richiesti." +
                footer);
        mailSender.send(message);
    }

    public void comunicateNewEventToMailList(List<String> mailList, String category, Instant eventDate){
        String antDate = getAndDate(eventDate);
        String postDate = getPostDate(eventDate);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(convertMailList(mailList));
        message.setSubject(category + " "+ antDate);
        message.setText("Gentile utente,\n registro calcio ELIS è felice di comunicarti che è stato creato un nuovo evento.\n\n Dettagli: " +
                category + " - " + antDate + " ore " + postDate + footer);
        mailSender.send(message);
    }

    public void comunicateTeamToMailList(List<String> mailList, String team, String category, Instant eventDate){
        String antDate = getAndDate(eventDate);
        String postDate = getPostDate(eventDate);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(convertMailList(mailList));
        message.setSubject("Assegnazione team " + category + " "+ antDate);
        message.setText("Gentile utente,\n registro calcio ELIS è felice di comunicarti che nella partita di " + category + " che si terrà " + antDate + " alle " + postDate +
                " farai parte del " + team + " team!" + footer);
        mailSender.send(message);
    }

    private String getAndDate(Instant eventDate){
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
