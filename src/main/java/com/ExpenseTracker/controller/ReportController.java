package com.ExpenseTracker.controller;

import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategorySummary;
import com.ExpenseTracker.service.AnalyticsService;
import com.ExpenseTracker.service.ReportingService;

import com.itextpdf.io.source.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AnalyticsService analyticsService;
    private final ReportingService reportingService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReportController(
            AnalyticsService analyticsService,
            ReportingService reportingService,
            TransactionRepository transactionRepository,
            UserRepository userRepository
    ) {
        this.analyticsService = analyticsService;
        this.reportingService = reportingService;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /* ✅ Helper: Get logged-in User entity from JWT */
    private User getLoggedInUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    /* ==========================================================
       ✅ CATEGORY SUMMARY PDF (date range)
       Example:
       /api/reports/pdf?startDate=2024-01-01&endDate=2024-12-31
       ========================================================== */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdfReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication auth
    ) throws IOException {

        User user = getLoggedInUser(auth);

        List<CategorySummary> summaryData =
                analyticsService.getExpenseSummaryByCategory(
                        user.getId(), startDate, endDate);

        byte[] pdfBytes = reportingService.generatePdfReport(
                user.getId(),
                "From " + startDate + " to " + endDate,
                summaryData
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=expense_report_" + LocalDate.now() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /* ==========================================================
       ✅ TRANSACTIONS CSV
       /api/reports/transactions/csv
       ========================================================== */
    @GetMapping("/transactions/csv")
    public ResponseEntity<byte[]> exportTransactionsCsv(Authentication auth) {

        User user = getLoggedInUser(auth);

        List<Transaction> transactions = transactionRepository.findByUser(user);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Type,Category,Amount,Description\n");

        for (Transaction t : transactions) {
            csv.append(t.getDate()).append(",")
                    .append(t.getType()).append(",")
                    .append(t.getCategory().getName()).append(",")
                    .append(t.getAmount()).append(",")
                    .append(t.getDescription()).append("\n");
        }

        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvBytes);
    }

    /* ==========================================================
       ✅ TRANSACTIONS EXCEL
       /api/reports/transactions/excel
       ========================================================== */
    @GetMapping("/transactions/excel")
    public ResponseEntity<byte[]> exportTransactionsExcel(Authentication auth) throws IOException {

        User user = getLoggedInUser(auth);

        List<Transaction> transactions = transactionRepository.findByUser(user);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Type");
        header.createCell(2).setCellValue("Category");
        header.createCell(3).setCellValue("Amount");
        header.createCell(4).setCellValue("Description");

        int rowIdx = 1;
        for (Transaction t : transactions) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(t.getDate().toString());
            row.createCell(1).setCellValue(t.getType().toString());
            row.createCell(2).setCellValue(t.getCategory().getName());
            row.createCell(3).setCellValue(t.getAmount().doubleValue());
            row.createCell(4).setCellValue(t.getDescription());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    /* ==========================================================
       ✅ TRANSACTIONS PDF
       /api/reports/transactions/pdf
       ========================================================== */
    @GetMapping("/transactions/pdf")
    public ResponseEntity<byte[]> downloadTransactionsPdf(Authentication auth) {

        User user = getLoggedInUser(auth);

        byte[] pdf = reportingService.generateTransactionsPdf(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /* ==========================================================
       ✅ MONTHLY REPORT PDF (current month)
       /api/reports/monthly/pdf
       ========================================================== */
    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> downloadMonthlyReport(Authentication auth) {

        User user = getLoggedInUser(auth);

        byte[] pdf = reportingService.generateMonthlyReportPdf(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Monthly_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /* ==========================================================
       ✅ SEND MONTHLY REPORT EMAIL (with chart attachment)
       POST /api/reports/send-monthly-report
       ========================================================== */
    @PostMapping("/send-monthly-report")
    public ResponseEntity<?> sendMonthlyReport(Authentication auth) throws Exception {

        User user = getLoggedInUser(auth);

        reportingService.sendMonthlyReportToUser(user);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "✅ Monthly report email sent!"
        ));
    }
}
