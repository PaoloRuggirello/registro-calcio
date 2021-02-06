package com.elis.registrocalcio.other;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender mailSender;

    private final String mailFrom = "registro.calcio.elis@yandex.com";
    private final String footer = "Buon divertimento,\n Registro calcio ELIS.\n\n" +
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
                "Puoi scegliere se continuare ad utilizzare questa o cambiarla con una a tuo piacimento, nel caso tu volessi cambiarla recati nella schermata di login e clicca sul pulsante 'Cambia password' e inserisci i dati richiesti.\n\n" +
                footer);
        mailSender.send(message);
    }
}
