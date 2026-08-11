package com.learningsp.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.learningsp.entity.Expense;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExpenseService expenseService;

    public byte[] exportToExcel(Long userId) throws IOException {
        List<Expense> expenses = expenseService.allForUser(userId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Expenses");
            String[] headers = {"Date", "Title", "Category", "Amount", "Payment Method", "Description", "Notes"};

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                createHeaderCell(headerRow, i, headers[i], headerStyle);
            }

            int rowIdx = 1;
            for (Expense e : expenses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getExpenseDate().toString());
                row.createCell(1).setCellValue(e.getTitle());
                row.createCell(2).setCellValue(e.getCategory().getCategoryName());
                row.createCell(3).setCellValue(e.getAmount().doubleValue());
                row.createCell(4).setCellValue(e.getPaymentMethod().name());
                row.createCell(5).setCellValue(e.getDescription() != null ? e.getDescription() : "");
                row.createCell(6).setCellValue(e.getNotes() != null ? e.getNotes() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createHeaderCell(Row row, int idx, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(idx);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public byte[] exportToPdf(Long userId) throws IOException {
        List<Expense> expenses = expenseService.allForUser(userId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new com.itextpdf.layout.element.Paragraph("Expense Report")
                    .setBold().setFontSize(18));

            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 25, 15, 12, 15, 18}))
                    .useAllAvailableWidth();

            for (String header : new String[]{"Date", "Title", "Category", "Amount", "Payment", "Description"}) {
                table.addHeaderCell(new Cell().add(new com.itextpdf.layout.element.Paragraph(header).setBold()));
            }

            for (Expense e : expenses) {
                table.addCell(e.getExpenseDate().toString());
                table.addCell(e.getTitle());
                table.addCell(e.getCategory().getCategoryName());
                table.addCell(e.getAmount().toString());
                table.addCell(e.getPaymentMethod().name());
                table.addCell(e.getDescription() != null ? e.getDescription() : "");
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    public byte[] exportToCsv(Long userId) {
        List<Expense> expenses = expenseService.allForUser(userId);
        StringBuilder sb = new StringBuilder("Date,Title,Category,Amount,Payment Method,Description,Notes\n");

        for (Expense e : expenses) {
            sb.append(csvEscape(e.getExpenseDate().toString())).append(",")
              .append(csvEscape(e.getTitle())).append(",")
              .append(csvEscape(e.getCategory().getCategoryName())).append(",")
              .append(e.getAmount()).append(",")
              .append(csvEscape(e.getPaymentMethod().name())).append(",")
              .append(csvEscape(e.getDescription())).append(",")
              .append(csvEscape(e.getNotes())).append("\n");
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
