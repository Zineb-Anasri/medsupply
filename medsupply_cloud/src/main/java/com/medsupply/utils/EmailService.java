package com.medsupply.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * EmailService - Utility class for sending emails
 * Uses JavaMail API with SMTP
 */
public class EmailService {

    private static Properties mailProperties;
    private static String smtpHost;
    private static String smtpPort;
    private static String smtpUsername;
    private static String smtpPassword;
    private static String fromEmail;

    static {
        loadEmailProperties();
    }

    /**
     * Load email configuration from email.properties file
     */
    private static void loadEmailProperties() {
        try {
            InputStream input = EmailService.class.getClassLoader().getResourceAsStream("email.properties");
            
            if (input == null) {
                System.err.println("Warning: email.properties not found. Email sending will be disabled.");
                return;
            }

            Properties props = new Properties();
            props.load(input);

            smtpHost = props.getProperty("smtp.host");
            smtpPort = props.getProperty("smtp.port", "587");
            smtpUsername = props.getProperty("smtp.username");
            smtpPassword = props.getProperty("smtp.password");
            fromEmail = props.getProperty("smtp.from", "noreply@medsupply.com");

            mailProperties = new Properties();
            mailProperties.put("mail.smtp.host", smtpHost);
            mailProperties.put("mail.smtp.port", smtpPort);
            mailProperties.put("mail.smtp.auth", "true");
            mailProperties.put("mail.smtp.starttls.enable", "true");
            mailProperties.put("mail.smtp.starttls.required", "true");
            mailProperties.put("mail.smtp.ssl.trust", smtpHost);

        } catch (IOException e) {
            System.err.println("Error loading email.properties: " + e.getMessage());
        }
    }

    /**
     * Send verification email to user
     * @param toEmail Recipient email
     * @param verificationToken Verification token
     * @return true if email sent successfully
     */
    public static boolean sendVerificationEmail(String toEmail, String verificationToken) {
        if (mailProperties == null) {
            System.err.println("Email service not configured. Skipping email sending.");
            return false;
        }

        try {
            String verificationLink = "http://localhost:8080/medsupply-cloud/api/auth/verify?token=" + verificationToken;
            
            String subject = "Verify Your Email - MedSupply Cloud";
            String body = "Hello,\n\n" +
                          "Thank you for registering with MedSupply Cloud.\n\n" +
                          "Please verify your email address by clicking the link below:\n\n" +
                          verificationLink + "\n\n" +
                          "This link will expire in 24 hours.\n\n" +
                          "If you did not create an account, please ignore this email.\n\n" +
                          "Best regards,\n" +
                          "MedSupply Cloud Team";

            return sendEmail(toEmail, subject, body);

        } catch (Exception e) {
            System.err.println("Error sending verification email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send email
     * @param toEmail Recipient email
     * @param subject Email subject
     * @param body Email body
     * @return true if email sent successfully
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        Session session = Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if email service is configured
     * @return true if email service is ready to use
     */
    public static boolean isConfigured() {
        return mailProperties != null && smtpHost != null && smtpUsername != null;
    }
}
