package com.unicycle.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private JavaMailSender mockEmailSender;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL = "john.doe@tufts.edu";
    private static final String TEST_SUBJECT = "Email Verification";
    private static final String TEST_TEXT = "<html><body><h1>Verification Code: 123ABC</h1></body></html>";

    @Test
    void sendVerificationEmail_success() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(TEST_EMAIL, TEST_SUBJECT, TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_withPlainText() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);
        String plainText = "Your verification code is: 123ABC";

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(TEST_EMAIL, TEST_SUBJECT, plainText);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_mailSendException_throwsException() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doThrow(new MailSendException("Failed to send email"))
            .when(mockEmailSender).send(any(MimeMessage.class));

        assertThrows(MailSendException.class, () -> {
            emailService.sendVerificationEmail(TEST_EMAIL, TEST_SUBJECT, TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_nullEmail_throwsException() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);

        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendVerificationEmail(null, TEST_SUBJECT, TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
    }

    @Test
    void sendVerificationEmail_emptyEmail_throwsException() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);

        assertThrows(AddressException.class, () -> {
            emailService.sendVerificationEmail("", TEST_SUBJECT, TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
    }

    @Test
    void sendVerificationEmail_invalidEmail_doesNotThrow() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        // Jakarta Mail accepts simple strings as local addresses
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail("invalid-email", TEST_SUBJECT, TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_multipleRecipients_throwsException() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);
        String multipleEmails = "john.doe@tufts.edu,jane.smith@tufts.edu";
        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        assertThrows(AddressException.class, () -> {
            emailService.sendVerificationEmail(multipleEmails, TEST_SUBJECT, TEST_TEXT);
        });
        verify(mockEmailSender, times(1)).createMimeMessage();
    }

    @Test
    void sendVerificationEmail_emptySubject() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(TEST_EMAIL, "", TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_emptyText() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(TEST_EMAIL, TEST_SUBJECT, "");
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_longSubject() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);
        String longSubject = "A".repeat(500);

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(TEST_EMAIL, longSubject, TEST_TEXT);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_longText() throws MessagingException {
        MimeMessage mockMessage = new MimeMessage((Session) null);
        String longText = "<html><body>" + "A".repeat(10000) + "</body></html>";

        when(mockEmailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(mockEmailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(TEST_EMAIL, TEST_SUBJECT, longText);
        });

        verify(mockEmailSender, times(1)).createMimeMessage();
        verify(mockEmailSender, times(1)).send(any(MimeMessage.class));
    }
}
