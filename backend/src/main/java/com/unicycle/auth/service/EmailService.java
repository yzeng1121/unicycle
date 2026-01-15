package com.unicycle.auth.service;

import com.unicycle.exception.InvalidEmailException;

import jakarta.validation.constraints.Email;
import jakarta.validation.Validator;
import jakarta.validation.Validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;

    public void sendVerificationEmail(String to, String subject, String text) 
        throws MessagingException 
    {
        if (!isValidEmail(to)) throw new InvalidEmailException("Invalid email format.");

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        emailSender.send(message);
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.length() == 0) return false;
        email = email.replaceAll("\\s+", "");
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return validator.validateValue(
                EmailHolder.class, 
                "email", 
                email)
            .isEmpty() && 
            email.toLowerCase().endsWith("@tufts.edu");
    }

    private static class EmailHolder {
        @Email
        private String email;
    }
}
