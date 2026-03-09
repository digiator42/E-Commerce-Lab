package com.ecommerce.lab.service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.Address;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.lowagie.text.*;
import java.awt.Color;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import tools.jackson.databind.ObjectMapper;

@Service
public class InvoiceService {

    private static final String LOGO_PATH = "/images/logo.png";
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color SECONDARY_COLOR = new Color(245, 245, 245); // Light Gray
    private static final Color ACCENT_COLOR = new Color(255, 99, 71); // Tomato Red

    public void generateInvoice(Order order, OutputStream outputStream)
        throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        document.open();

        // Add all invoice components
        addLogoAndHeader(document);
        addInvoiceTitle(document, order);
        addCompanyInfo(document);
        addCustomerInfo(document, order);
        addOrderInfo(document, order);
        addProductsTable(document, order);
        addTotals(document, order);
        addFooter(document);

        document.close();
    }

    private void addLogoAndHeader(Document document) throws DocumentException {
        try {
            // Create a table for logo and header
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new int[] {
                    1, 3
            });

            // Add logo
            String imagePath = getClass().getResource(LOGO_PATH).getPath();
            Image logo = Image.getInstance(imagePath);
            logo.scaleToFit(100, 60);
            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);

            // Add "MASTERSHOP" text
            Font shopFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, PRIMARY_COLOR);
            Paragraph shopName = new Paragraph("MASTERSHOP", shopFont);
            shopName.setAlignment(Element.ALIGN_RIGHT);

            PdfPCell shopCell = new PdfPCell(shopName);
            shopCell.setBorder(Rectangle.NO_BORDER);
            shopCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            shopCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(shopCell);

            document.add(headerTable);
            document.add(new Paragraph(" ")); // Spacer
        } catch (Exception e) {
            // If logo not found, just add text
            Font shopFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, PRIMARY_COLOR);
            Paragraph shopName = new Paragraph("MASTERSHOP", shopFont);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);
        }
    }

    private void addInvoiceTitle(Document document, Order order) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, PRIMARY_COLOR);
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add invoice number and date in a table
        PdfPTable invoiceInfoTable = new PdfPTable(2);
        invoiceInfoTable.setWidthPercentage(100);

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell invoiceNoCell = new PdfPCell(
            new Phrase("Invoice #: INV-" + order.getId(), infoFont)
        );
        invoiceNoCell.setBorder(Rectangle.NO_BORDER);
        invoiceNoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        invoiceInfoTable.addCell(invoiceNoCell);

        PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + LocalDateTime.now(), infoFont));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        invoiceInfoTable.addCell(dateCell);

        document.add(invoiceInfoTable);
        document.add(new Paragraph(" "));
    }

    private void addCompanyInfo(Document document) throws DocumentException {
        PdfPTable companyTable = new PdfPTable(1);
        companyTable.setWidthPercentage(100);

        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setBackgroundColor(SECONDARY_COLOR);
        companyCell.setPadding(10);

        Paragraph companyInfo = new Paragraph();
        companyInfo.add(
            new Chunk("FAKEMASTERSHOP Inc.\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))
        );
        companyInfo.add(new Chunk("123 STREET\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        companyInfo.add(new Chunk("Suite 100\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        companyInfo
            .add(new Chunk("New York, NY 10001\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        companyInfo.add(
            new Chunk("Phone: (999) 999-9999\n", FontFactory.getFont(FontFactory.HELVETICA, 10))
        );
        companyInfo.add(
            new Chunk(
                "Email: billing@fakemastershop.com", FontFactory.getFont(FontFactory.HELVETICA, 10)
            )
        );

        companyCell.addElement(companyInfo);
        companyTable.addCell(companyCell);

        document.add(companyTable);
        document.add(new Paragraph(" "));
    }

    private void addCustomerInfo(Document document, Order order)
        throws IOException, DocumentException {
        ObjectMapper mapper = new ObjectMapper();
        Address address = mapper.readValue(order.getUser().getAddress(), Address.class);

        PdfPTable customerTable = new PdfPTable(2);
        customerTable.setWidthPercentage(100);

        // Bill To section
        PdfPCell billToCell = new PdfPCell();
        billToCell.setBorder(Rectangle.BOX);
        billToCell.setPadding(10);
        billToCell.setBackgroundColor(SECONDARY_COLOR);

        Paragraph billTo = new Paragraph();
        billTo.add(
            new Chunk(
                "BILL TO:\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR)
            )
        );
        billTo.add(
            new Chunk(
                order.getUser().getName() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 11)
            )
        );
        billTo.add(
            new Chunk(address.getStreet() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 10))
        );
        billTo
            .add(
                new Chunk(
                    address.getCity() + ", " + address.getState() + " " + address.getZipCode()
                        + "\n",
                    FontFactory.getFont(FontFactory.HELVETICA, 10)
                )
            );
        billTo.add(new Chunk(address.getCountry(), FontFactory.getFont(FontFactory.HELVETICA, 10)));

        billToCell.addElement(billTo);
        customerTable.addCell(billToCell);

        // Order Summary section
        PdfPCell summaryCell = new PdfPCell();
        summaryCell.setBorder(Rectangle.BOX);
        summaryCell.setPadding(10);
        summaryCell.setBackgroundColor(SECONDARY_COLOR);

        Paragraph summary = new Paragraph();
        summary.add(
            new Chunk(
                "ORDER SUMMARY:\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR)
            )
        );
        summary.add(
            new Chunk(
                "Order ID: " + order.getId() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 10)
            )
        );
        summary.add(
            new Chunk(
                "Payment Transaction: " + order.getPaymentTransactionId().toString() + "\n",
                FontFactory.getFont(FontFactory.HELVETICA, 10)
            )
        );
        summary.add(
            new Chunk(
                "Payment Status: " + order.getPaymentStatus().toString() + "\n",
                FontFactory.getFont(FontFactory.HELVETICA, 10)
            )
        );
        summary.add(
            new Chunk(
                "Order Status: " + order.getStatus().toString(),
                FontFactory.getFont(FontFactory.HELVETICA, 10)
            )
        );

        summaryCell.addElement(summary);
        customerTable.addCell(summaryCell);

        document.add(customerTable);
        document.add(new Paragraph(" "));
    }

    private void addOrderInfo(Document document, Order order) throws DocumentException {
        // Add any additional order information
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(10);

        addInfoRow(infoTable, "Payment Method:", "Credit Card");
        addInfoRow(infoTable, "Order Date:", order.getOrderDate().toString());

        document.add(infoTable);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(10);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    private void addProductsTable(Document document, Order order) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[] {
                3, 2, 1, 1
        }); // Column width ratios

        // Table header
        String[] headers = {
                "Product", "Category", "Qty", "Price"
        };
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(
                new Phrase(
                    header,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)
                )
            );
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Table content
        for (OrderItem item : order.getItems()) {
            String categoryName = item.getProduct() != null
                ? item.getProduct().getCategory().getName().toString()
                : "Store Item";

            addProductCell(table, item.getProductName(), Element.ALIGN_LEFT);
            addProductCell(table, categoryName, Element.ALIGN_LEFT);
            addProductCell(table, String.valueOf(item.getQuantity()), Element.ALIGN_CENTER);
            addProductCell(
                table, "$" + String.format("%.2f", item.getPriceAtPurchase()), Element.ALIGN_RIGHT
            );
        }

        document.add(table);
    }

    private void addProductCell(PdfPTable table, String content, int alignment) {
        PdfPCell cell = new PdfPCell(
            new Phrase(content, FontFactory.getFont(FontFactory.HELVETICA, 10))
        );
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(PRIMARY_COLOR);
        table.addCell(cell);
    }

    private void addTotals(Document document, Order order) throws DocumentException {
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingBefore(20);

        // Calculate subtotal
        double subtotal = order.getItems().stream()
            .mapToDouble(item -> item.getQuantity() * item.getPriceAtPurchase())
            .sum();

        double total = order.getTotalAmount();

        // Add rows
        addTotalRow(totalTable, "Subtotal:", "$" + String.format("%.2f", subtotal), false);
        addTotalRow(totalTable, "Total:", "$" + String.format("%.2f", total), true);

        document.add(totalTable);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean isTotal) {
        Font labelFont = isTotal
            ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR)
            : FontFactory.getFont(FontFactory.HELVETICA, 11);

        Font valueFont = isTotal
            ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR)
            : FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorder(Rectangle.TOP);
        footerCell.setBorderColor(PRIMARY_COLOR);
        footerCell.setPaddingTop(10);
        footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph footer = new Paragraph();
        footer.add(
            new Chunk(
                "Thank you for shopping with MASTERSHOP!\n",
                FontFactory.getFont(FontFactory.HELVETICA, 10, PRIMARY_COLOR)
            )
        );
        footer.add(
            new Chunk(
                "For any queries, please contact us at support@fakemastershop.com",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)
            )
        );

        footerCell.addElement(footer);
        footerTable.addCell(footerCell);

        document.add(footerTable);
    }

}