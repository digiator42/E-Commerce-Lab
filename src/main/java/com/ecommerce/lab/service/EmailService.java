package com.ecommerce.lab.service;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.User;

import jakarta.mail.internet.MimeMessage;

@Service
@Async
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final InvoiceService invoiceService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(
        JavaMailSender mailSender,
        TemplateEngine templateEngine,
        InvoiceService invoiceService
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.invoiceService = invoiceService;
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(this.fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }

    private void sendTemplateEmail(
        String to,
        String subject,
        String templateName,
        Context context
    ) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(this.fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send " + templateName + " email: " + e.getMessage());
        }
    }

    public void sendOrderConfirmation(Order order) {
        try {
            // Prepare the Thymeleaf Context (Variables)
            Context context = new Context();
            context.setVariable("name", order.getUser().getName());
            context.setVariable("orderId", order.getId());
            context.setVariable("total", order.getTotalAmount());
            context.setVariable("items", order.getItems());

            String to = order.getUser().getEmail();
            String subject = "Your Order Confirmation #" + order.getId();

            // Send the Email
            this.sendTemplateEmail(to, subject, "order-confirmation", context);

        } catch (Exception e) {
            System.err.println("Template Email Failed: " + e.getMessage());
        }
    }

    public void sendOrderConfirmationWithInvoice(Order order) {
        try {
            // Prepare HTML Content
            Context context = new Context();
            context.setVariable("name", order.getUser().getName());
            context.setVariable("orderId", order.getId());
            context.setVariable("total", order.getTotalAmount());
            context.setVariable("items", order.getItems());
            String htmlContent = templateEngine.process("order-confirmation", context);

            // Setup MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(this.fromEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Your Order Confirmation #" + order.getId());
            helper.setText(htmlContent, true); // Set HTML body

            // Generate PDF and Attach
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            invoiceService.generateInvoice(order.getId(), outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            helper.addAttachment(
                "Invoice_Order_" + order.getId() + ".pdf",
                new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Email/PDF Generation Failed: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        sendTemplateEmail(user.getEmail(), "Welcome to Commerce Lab!", "welcome-email", context);
    }

    public void sendGiftCardCode(String email, String code, double amount, String name) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("code", code);
        context.setVariable("amount", amount);
        sendTemplateEmail(
            email, "Thanks for purchasing Gift Card", "giftcard-purchase", context
        );
    }

    public void sendPasswordResetEmail(String email, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        System.out.println("======> " + resetLink);
        sendTemplateEmail(
            email, "Reset your password through this link", "password-reset", context
        );

    }

    public void send2FACode(String email, String code) {
        Context context = new Context();
        context.setVariable("code", code);
        sendTemplateEmail(email, "Your Verification Code", "2fa-code", context);
    }
}