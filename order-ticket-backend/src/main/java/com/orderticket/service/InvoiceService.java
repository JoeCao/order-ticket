package com.orderticket.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.orderticket.entity.Order;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public byte[] generatePdfInvoice(Order order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Title
            document.add(new Paragraph("订单发票 / Order Invoice")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            
            document.add(new Paragraph("\n"));
            
            // Order information table
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            
            addTableRow(infoTable, "订单号 / Order Number:", order.getOrderNumber());
            addTableRow(infoTable, "客户姓名 / Customer Name:", order.getCustomerName());
            addTableRow(infoTable, "客户邮箱 / Email:", order.getCustomerEmail());
            addTableRow(infoTable, "客户电话 / Phone:", order.getCustomerPhone());
            addTableRow(infoTable, "订单状态 / Status:", order.getStatus().toString());
            addTableRow(infoTable, "订单日期 / Order Date:", order.getOrderDate().format(DATE_FORMATTER));
            addTableRow(infoTable, "总金额 / Total Amount:", "¥" + order.getTotalAmount().toString());
            
            document.add(infoTable);
            document.add(new Paragraph("\n"));
            
            // Product details
            if (order.getProductDetails() != null && !order.getProductDetails().isEmpty()) {
                document.add(new Paragraph("产品详情 / Product Details:")
                        .setBold()
                        .setFontSize(14));
                document.add(new Paragraph(order.getProductDetails()));
                document.add(new Paragraph("\n"));
            }
            
            // Description
            if (order.getDescription() != null && !order.getDescription().isEmpty()) {
                document.add(new Paragraph("订单描述 / Description:")
                        .setBold()
                        .setFontSize(14));
                document.add(new Paragraph(order.getDescription()));
                document.add(new Paragraph("\n"));
            }
            
            // Footer
            document.add(new Paragraph("感谢您的订购！/ Thank you for your order!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());
            
            document.add(new Paragraph("生成时间 / Generated at: " + 
                    java.time.LocalDateTime.now().format(DATE_FORMATTER))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10));
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
    }
    
    public byte[] generateBatchPdfInvoices(List<Order> orders) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Title
            document.add(new Paragraph("批量订单发票 / Batch Order Invoices")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            
            document.add(new Paragraph("共 " + orders.size() + " 个订单 / Total " + orders.size() + " orders")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
            
            document.add(new Paragraph("\n"));
            
            // Create summary table
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{20, 25, 20, 15, 20}));
            summaryTable.setWidth(UnitValue.createPercentValue(100));
            
            // Header
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("订单号").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("客户姓名").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("状态").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("金额").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("日期").setBold()));
            
            // Data rows
            for (Order order : orders) {
                summaryTable.addCell(new Cell().add(new Paragraph(order.getOrderNumber())));
                summaryTable.addCell(new Cell().add(new Paragraph(order.getCustomerName())));
                summaryTable.addCell(new Cell().add(new Paragraph(order.getStatus().toString())));
                summaryTable.addCell(new Cell().add(new Paragraph("¥" + order.getTotalAmount().toString())));
                summaryTable.addCell(new Cell().add(new Paragraph(order.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")))));
            }
            
            document.add(summaryTable);
            
            // Footer
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("生成时间 / Generated at: " + 
                    java.time.LocalDateTime.now().format(DATE_FORMATTER))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10));
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating batch PDF invoices", e);
        }
    }
    
    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")));
    }
}