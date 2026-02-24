package com.ecommerce.lab.service;

import java.io.ByteArrayOutputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.ecommerce.lab.model.Order;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Async
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final InvoiceService invoiceService;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("42pongos@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("42pongos@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendOrderConfirmation(Order order) {
        try {
            // Prepare the Thymeleaf Context (Variables)
            Context context = new Context();
            context.setVariable("name", order.getUser().getName());
            context.setVariable("orderId", order.getId());
            context.setVariable("total", order.getTotalAmount());
            context.setVariable("items", order.getItems());

            // Generate HTML string from template
            String htmlContent = templateEngine.process("order-confirmation", context);
            String to = order.getUser().getEmail();
            String subject = "Your Order Confirmation #" + order.getId();

            // Send the Email
            this.sendHtmlEmail(to, subject, htmlContent);

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

            helper.setFrom("42pongos@gmail.com");
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Your Order Confirmation #" + order.getId());
            helper.setText(htmlContent, true); // Set HTML body

            // Generate PDF and Attach
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // You'll need to update your InvoiceService to accept an OutputStream
            invoiceService.generateInvoice(order, outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            helper.addAttachment("Invoice_Order_" + order.getId() + ".pdf",
                    new ByteArrayResource(pdfBytes));

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Email/PDF Generation Failed: " + e.getMessage());
        }
    }
}