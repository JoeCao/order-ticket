package com.orderticket.service;

import com.orderticket.entity.Order;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public byte[] exportOrdersToExcel(List<Order> orders) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单数据");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "订单号", "客户姓名", "客户邮箱", "客户电话", 
                "总金额", "订单状态", "订单日期", "描述", "产品详情", "创建时间"
            };
            
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getCustomerName());
                row.createCell(2).setCellValue(order.getCustomerEmail() != null ? order.getCustomerEmail() : "");
                row.createCell(3).setCellValue(order.getCustomerPhone() != null ? order.getCustomerPhone() : "");
                row.createCell(4).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(5).setCellValue(order.getStatus().toString());
                row.createCell(6).setCellValue(order.getOrderDate().format(DATE_FORMATTER));
                row.createCell(7).setCellValue(order.getDescription() != null ? order.getDescription() : "");
                row.createCell(8).setCellValue(order.getProductDetails() != null ? order.getProductDetails() : "");
                row.createCell(9).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMATTER) : "");
                
                // Apply data style to all cells in the row
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                // Set maximum width to prevent very wide columns
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }
            
            // Create summary sheet
            createSummarySheet(workbook, orders);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error exporting orders to Excel", e);
        }
    }
    
    private void createSummarySheet(Workbook workbook, List<Order> orders) {
        Sheet summarySheet = workbook.createSheet("订单统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Calculate statistics
        long totalOrders = orders.size();
        long pendingOrders = orders.stream().mapToLong(o -> o.getStatus() == Order.OrderStatus.PENDING ? 1 : 0).sum();
        long processingOrders = orders.stream().mapToLong(o -> o.getStatus() == Order.OrderStatus.PROCESSING ? 1 : 0).sum();
        long deliveredOrders = orders.stream().mapToLong(o -> o.getStatus() == Order.OrderStatus.DELIVERED ? 1 : 0).sum();
        long cancelledOrders = orders.stream().mapToLong(o -> o.getStatus() == Order.OrderStatus.CANCELLED ? 1 : 0).sum();
        double totalAmount = orders.stream().mapToDouble(o -> o.getTotalAmount().doubleValue()).sum();
        
        int rowNum = 0;
        
        // Title
        Row titleRow = summarySheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("订单统计报告");
        titleRow.getCell(0).setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        // Statistics
        addSummaryRow(summarySheet, rowNum++, "总订单数", String.valueOf(totalOrders), headerStyle, dataStyle);
        addSummaryRow(summarySheet, rowNum++, "待处理订单", String.valueOf(pendingOrders), headerStyle, dataStyle);
        addSummaryRow(summarySheet, rowNum++, "处理中订单", String.valueOf(processingOrders), headerStyle, dataStyle);
        addSummaryRow(summarySheet, rowNum++, "已送达订单", String.valueOf(deliveredOrders), headerStyle, dataStyle);
        addSummaryRow(summarySheet, rowNum++, "已取消订单", String.valueOf(cancelledOrders), headerStyle, dataStyle);
        addSummaryRow(summarySheet, rowNum++, "总金额", String.format("¥%.2f", totalAmount), headerStyle, dataStyle);
        
        rowNum++; // Empty row
        
        // Generation time
        Row timeRow = summarySheet.createRow(rowNum);
        timeRow.createCell(0).setCellValue("生成时间");
        timeRow.createCell(1).setCellValue(java.time.LocalDateTime.now().format(DATE_FORMATTER));
        timeRow.getCell(0).setCellStyle(headerStyle);
        timeRow.getCell(1).setCellStyle(dataStyle);
        
        // Auto-size columns
        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);
    }
    
    private void addSummaryRow(Sheet sheet, int rowNum, String label, String value, CellStyle headerStyle, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(dataStyle);
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
}