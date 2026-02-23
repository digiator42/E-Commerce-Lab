package com.ecommerce.lab.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class InvoiceService {

    public void generateInvoice(Order order, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Add Header
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        Paragraph title = new Paragraph("INVOICE", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        // Add Order Info
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("Customer: " + order.getUser().getEmail()));
        document.add(new Paragraph("Address: " + order.getUser().getAddress().toString()));
        document.add(new Paragraph("Date: " + order.getOrderDate().toString()));
        document.add(new Paragraph(" ")); // Spacer
        document.add(new Paragraph("Payment_Transaction_Id: " + order.getPaymentTransactionId().toString()));
        document.add(new Paragraph("Payment_Status: " + order.getPaymentStatus().toString()));
        document.add(new Paragraph("Status: " + order.getStatus().toString()));
        document.add(new Paragraph(" ")); // Spacer
        document.add(new Paragraph(" ")); // Spacer

        // Create Table for Products
        PdfPTable table = new PdfPTable(4); // 4 columns
        table.setWidthPercentage(100);

        table.addCell("Product");
        table.addCell("Category");
        table.addCell("Quantity");
        table.addCell("Price");

        for (OrderItem item : order.getItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(item.getProduct().getCategory().getName().toString());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("$" + item.getProduct().getPrice());
        }

        document.add(table);

        // Total
        Paragraph total = new Paragraph("Total: $" + order.getTotalAmount(), fontTitle);
        total.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(total);

        document.close();
    }
}