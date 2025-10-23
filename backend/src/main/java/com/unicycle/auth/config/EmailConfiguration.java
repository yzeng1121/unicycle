package com.unicycle.auth.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfiguration {
    @Value("${spring.mail.username}")
    private String emailUsername;
    @Value("${spring.mail.password}")
    private String password;

    // creates + configures an email sending service
    @Bean
    public JavaMailSender javaMainSender() {

        // create a new mail sender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // tell it which email service to use
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // provide login credentials
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(password);

        // configure security & protocol settings
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        // return the configured mail sender
        return mailSender;
    }
    
}
