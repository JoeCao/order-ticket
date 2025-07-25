package com.orderticket.controller;

import com.orderticket.entity.Order;
import com.orderticket.service.ExportService;
import com.orderticket.service.InvoiceService;
import com.orderticket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "http://localhost:5173")
public class ExportController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private ExportService exportService;
    
    // Export single order as PDF invoice
    @GetMapping("/invoice/pdf/{orderId}")
    public ResponseEntity<byte[]> exportSingleInvoicePdf(@PathVariable Long orderId) {
        Optional<Order> orderOpt = orderService.findOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Order order = orderOpt.get();
        byte[] pdfData = invoiceService.generatePdfInvoice(order);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "invoice_" + order.getOrderNumber() + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }
    
    // Export filtered orders as batch PDF invoices
    @GetMapping("/invoice/pdf/batch")
    public ResponseEntity<byte[]> exportBatchInvoicesPdf(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Order> orders = orderService.getOrdersForExport(orderNumber, customerName, status, startDate, endDate);
        
        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        byte[] pdfData = invoiceService.generateBatchPdfInvoices(orders);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "batch_invoices_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }
    
    // Export filtered orders to Excel
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportOrdersToExcel(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Order> orders = orderService.getOrdersForExport(orderNumber, customerName, status, startDate, endDate);
        
        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        byte[] excelData = exportService.exportOrdersToExcel(orders);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
            "orders_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
    
    // Get export preview (first 10 orders that would be exported)
    @GetMapping("/preview")
    public ResponseEntity<List<Order>> getExportPreview(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Order> orders = orderService.getOrdersForExport(orderNumber, customerName, status, startDate, endDate);
        
        // Return first 10 orders for preview
        List<Order> preview = orders.stream().limit(10).toList();
        return ResponseEntity.ok(preview);
    }
}