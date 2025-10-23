package com.unicycle.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired // (from EmailConfiguration.java)
    private JavaMailSender emailSender;

    public void sendVerificationEmail(String to, String subject, String text) 
        throws MessagingException 
    {
        MimeMessage message = emailSender.createMimeMessage();

        // "true" allows for message to have multiple parts (attachments)
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);

        // true sets contents as written in HTML (nice design)
        helper.setText(text, true);

        emailSender.send(message);
    }
}
