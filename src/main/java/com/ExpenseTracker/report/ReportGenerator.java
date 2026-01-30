package com.ExpenseTracker.report;

import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.service.ExpenseService;

// iText Imports
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    private final ExpenseService expenseService;

    // Constructor for dependency injection
    public ReportGenerator(ExpenseService service) {
        this.expenseService = service;
    }

    /**
     * Generates a PDF report with summary, monthly trend, and detailed transactions.
     * @param destPath The file path where the PDF should be saved.
     * @throws FileNotFoundException If the destination path is invalid or access is denied.
     */
    public void generateFullReport(String destPath) throws FileNotFoundException {
        
        try (PdfWriter writer = new PdfWriter(destPath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // 1. Fetch all necessary data from the Service layer (All use BigDecimal)
            List<Transaction> allTransactions = expenseService.getAllTransactions();
            BigDecimal netBalance = expenseService.calculateNetBalance(); 
            Map<String, BigDecimal> summaryByCategory = expenseService.getExpenseSummaryByCategory(); 
            Map<String, BigDecimal> monthlyTrend = expenseService.getMonthlyExpenseTrend(); 

            // 2. Report Header and Title
            document.add(new Paragraph("Expense Tracker Financial Report")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Generated on: " + java.time.LocalDate.now())
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // 3. Overall Summary section
            document.add(new Paragraph("--- Financial Summary ---")
                    .setBold().setFontSize(14));
            // Use .doubleValue() only for feeding the BigDecimal into String.format
            document.add(new Paragraph("Net Balance: " + String.format("$%,.2f", netBalance.doubleValue()))); 
            document.add(new Paragraph("\n"));

            // 4. Expense Breakdown by Category
            document.add(new Paragraph("Expense Breakdown by Category").setBold());
            addCategorySummaryTable(document, summaryByCategory); 
            document.add(new Paragraph("\n"));

            // 5. Monthly Expense Trend
            document.add(new Paragraph("Monthly Expense Trend").setBold());
            addMonthlyTrendTable(document, monthlyTrend); 
            document.add(new Paragraph("\n"));

            // 6. Detailed transaction table
            document.add(new Paragraph("--- Detailed Transactions ---")
                    .setBold().setFontSize(14));
            addTransactionTable(document, allTransactions);

            System.out.println("PDF Report generated successfully at: " + destPath);

        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            System.err.println("An I/O error occurred during PDF generation: " + e.getMessage());
        }
    }
    
    // -------------------------
    // Helper Methods
    // -------------------------

    private void addTransactionTable(Document document, List<Transaction> transactions) {

        // Defined column widths (1 ID, 2 Date, 1 Type, 2 Category, 3 Description, 2 Amount)
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1.5f, 2, 3, 2}))
                .setWidth(UnitValue.createPercentValue(100));

        // Table Headers
        table.addHeaderCell(new Paragraph("ID").setBold())
             .addHeaderCell(new Paragraph("Date").setBold())
             .addHeaderCell(new Paragraph("Type").setBold())
             .addHeaderCell(new Paragraph("Category").setBold())
             .addHeaderCell(new Paragraph("Description").setBold())
             .addHeaderCell(new Paragraph("Amount").setBold());

        // Table Rows
        for (Transaction t : transactions) {
            
            table.addCell(String.valueOf(t.getId())); 
            table.addCell(t.getDate().toString());
            
            // FIX: Convert the Transaction.Type Enum to a String
            table.addCell(t.getType().toString()); 
            
            // FIX: Convert the Category Entity to its name/label
            table.addCell(t.getCategory().getName());
            
            table.addCell(t.getDescription());
            
            // FIX: Use BigDecimal's string representation for maximum precision, set scale to 2
            table.addCell(t.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        }
        document.add(table);
    }

    private void addCategorySummaryTable(Document document, Map<String, BigDecimal> summary) {
        
        // FIX 1: Corrected Map value type to BigDecimal
        // Convert map to list and sort by total spent (descending)
        List<Map.Entry<String, BigDecimal>> sortedSummary = summary.entrySet().stream()
            // FIX 2: Use BigDecimal::compareTo for accurate sorting
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue(BigDecimal::compareTo).reversed()) 
            .collect(Collectors.toList());

        // Table for Category Summary
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                .setWidth(UnitValue.createPercentValue(50));

        table.addHeaderCell(new Paragraph("Category").setBold())
             .addHeaderCell(new Paragraph("Total Spent").setBold());

        sortedSummary.forEach(entry -> {
            table.addCell(entry.getKey());
            // FIX 3: Use .doubleValue() only for the final String format display
            table.addCell(String.format("$%,.2f", entry.getValue().doubleValue()).trim()); 
        });

        document.add(table);
    }
    
    private void addMonthlyTrendTable(Document document, Map<String, BigDecimal> monthlyTrend) {
        
        // FIX 1: Corrected Map value type to BigDecimal
        // Convert map to list and sort by month string (YYYY-MM)
        List<Map.Entry<String, BigDecimal>> sortedTrend = monthlyTrend.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toList());
            
        // Table for Monthly Trend
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                .setWidth(UnitValue.createPercentValue(50));

        table.addHeaderCell(new Paragraph("Month (YYYY-MM)").setBold())
             .addHeaderCell(new Paragraph("Total Expense").setBold());

        sortedTrend.forEach(entry -> {
            table.addCell(entry.getKey());
            // FIX 2: Use .doubleValue() only for the final String format display
            table.addCell(String.format("$%,.2f", entry.getValue().doubleValue()).trim());
        });

        document.add(table);
    }
}