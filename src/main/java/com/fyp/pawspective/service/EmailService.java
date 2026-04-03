package com.fyp.pawspective.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("name", name);

            String htmlContent = processTemplate("welcome-email", templateModel);
            sendHtmlEmail(toEmail, "Welcome to pawspective!", htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    public void sendResetPasswordOtpEmail(String toEmail, String otp, String name) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("otp", otp);
            templateModel.put("name", name);

            String htmlContent = processTemplate("password-reset-email", templateModel);
            sendHtmlEmail(toEmail, "Password Reset OTP", htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendOtpEmail(String toEmail, String otp, String name) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("otp", otp);
            templateModel.put("name", name);

            String htmlContent = processTemplate("verify-account-email", templateModel);
            sendHtmlEmail(toEmail, "Account Verification OTP", htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String processTemplate(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process(templateName, context);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        helper.addInline("logo", new ClassPathResource("static/images/logo.png"));
        
        mailSender.send(message);
    }
}