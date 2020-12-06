package com.ing.fmjavaguild.batch.infrastructure.emails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SimpleEmailService {

    private final JavaMailSender emailSender;
    private static final String LUCKY_SUPPORT_MAILING_GROUP = "cristian-matei.toader@ing.com";

    @Autowired
    public SimpleEmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSimpleMessage(String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("java-fm-guild.support@noreply.com");
        message.setTo(LUCKY_SUPPORT_MAILING_GROUP);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
