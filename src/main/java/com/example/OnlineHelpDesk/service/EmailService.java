package com.example.OnlineHelpDesk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@onlineHelpdesk.com");
            message.setReplyTo("admin@onlineHelpdesk.com");
            mailSender.send(message);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Error while sending email: " + e.getMessage();
        }
    }
}
