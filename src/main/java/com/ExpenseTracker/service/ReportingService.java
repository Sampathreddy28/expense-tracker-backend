package com.ExpenseTracker.service;

import com.ExpenseTracker.dao.TransactionRepository;

import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategorySummary;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import jakarta.mail.internet.MimeMessage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    private final TransactionRepository transactionRepository;
    private final SpendingInsightService insightService;

    private final EmailAlertService emailAlertService;
    private final WhatsAppService whatsAppService;
    private final SmsAlertService smsAlertService;
    private final JavaMailSender mailSender;

    public ReportingService(
            TransactionRepository transactionRepository,
            SpendingInsightService insightService,
            EmailAlertService emailAlertService,
          
            WhatsAppService whatsAppService, SmsAlertService smsAlertService, JavaMailSender mailSender
    ) {
        this.transactionRepository = transactionRepository;
        this.insightService = insightService;
        this.emailAlertService = emailAlertService;
       
        this.whatsAppService = whatsAppService;
		this.smsAlertService = smsAlertService;
		this.mailSender = mailSender;
    }

    /* ==============================
       TRANSACTION PDF (TABLE)
       ============================== */
    public byte[] generateTransactionsPdf(User user) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Transaction Report")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("User: " + user.getUsername()));
            document.add(new Paragraph("Generated on: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            Table table = new Table(new float[]{3, 3, 2, 2});
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("Date");
            table.addHeaderCell("Category");
            table.addHeaderCell("Amount");
            table.addHeaderCell("Type");

            List<Transaction> transactions = transactionRepository.findByUserId(user.getId());

            for (Transaction tx : transactions) {
                table.addCell(tx.getDate().toString());
                table.addCell(tx.getCategory().getName());
                table.addCell(tx.getAmount().toString());
                table.addCell(tx.getType().name());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    /* ==============================
       MONTHLY REPORT PDF WITH CHARTS
       ============================== */
    public byte[] generateMonthlyReportPdf(User user) {

        Map<YearMonth, BigDecimal> monthly = transactionRepository.findMonthlyExpense(user.getId());
        List<Object[]> rawCategories = transactionRepository.findCategoryTotals(user.getId());
        Map<String, BigDecimal> categories = toCategoryMap(rawCategories);

        List<String> insights = insightService.generateInsights(monthly, categories);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("📊 Monthly Expense Report")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("User: " + user.getUsername()));
            document.add(new Paragraph("\n"));

            // ✅ Monthly Bar Chart
            Image barChart = new Image(ImageDataFactory.create(createMonthlyBarChart(monthly)));
            barChart.setAutoScale(true);
            document.add(barChart);

            document.add(new Paragraph("\n"));

            // ✅ Category Pie Chart
            Image pieChart = new Image(ImageDataFactory.create(createCategoryPieChart(categories)));
            pieChart.setAutoScale(true);
            document.add(pieChart);

            document.add(new Paragraph("\n"));

            // ✅ Insights
            document.add(new Paragraph("🧠 Spending Insights").setBold());
            for (String insight : insights) {
                document.add(new Paragraph("• " + insight));
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Monthly PDF failed", e);
        }
    }

    private Map<String, BigDecimal> toCategoryMap(List<Object[]> rawData) {
        return rawData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    /* ==============================
       CATEGORY SUMMARY PDF
       ============================== */
    public byte[] generatePdfReport(Long userId, String periodLabel, List<CategorySummary> summaryData) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("📄 Expense Summary Report")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("User ID: " + userId));
            document.add(new Paragraph("Period: " + periodLabel));
            document.add(new Paragraph("Generated on: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            Table table = new Table(new float[]{4, 3});
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("Category");
            table.addHeaderCell("Total Amount");

            for (CategorySummary cs : summaryData) {
                table.addCell(cs.getCategoryName());
                table.addCell(cs.getTotalAmount().toString());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Category PDF generation failed", e);
        }
    }

    private byte[] createMonthlyBarChart(Map<YearMonth, BigDecimal> data) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e ->
                        dataset.addValue(
                                e.getValue().doubleValue(),
                                "Expenses",
                                e.getKey().toString()
                        )
                );

        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Expenses",
                "Month",
                "Amount",
                dataset
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(out, chart, 600, 400);
        return out.toByteArray();
    }

    private byte[] createCategoryPieChart(Map<String, BigDecimal> data) throws Exception {

        DefaultPieDataset dataset = new DefaultPieDataset();

        data.forEach((k, v) -> dataset.setValue(k, v.doubleValue()));

        JFreeChart chart = ChartFactory.createPieChart(
                "Category Breakdown",
                dataset,
                true,
                true,
                false
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(out, chart, 600, 400);
        return out.toByteArray();
    }

    /* ==============================
       ✅ MONTHLY TREND CHART + EMAIL/TELEGRAM/WHATSAPP
       ============================== */

    public List<MonthlyTrendDto> getMonthlyTrend(Long userId) {
        List<Object[]> raw = transactionRepository.getMonthlyExpenseTrend(userId);

        List<MonthlyTrendDto> result = new ArrayList<>();
        for (Object[] row : raw) {
            String month = row[0].toString();
            BigDecimal amount = (BigDecimal) row[1];
            result.add(new MonthlyTrendDto(month, amount));
        }
        return result;
    }

    public byte[] generateMonthlyTrendChart(List<MonthlyTrendDto> data) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (MonthlyTrendDto d : data) {
            dataset.addValue(d.getAmount().doubleValue(), "Expenses", d.getMonth());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Monthly Expense Trend",
                "Month",
                "Amount",
                dataset
        );

        BufferedImage image = chart.createBufferedImage(800, 400);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

  
//    public void sendMonthlyReportToUser(User user) throws Exception {
//
//        String body = """
//            <h2>📊 Monthly Report</h2>
//            <p>Hello <b>%s</b>,</p>
//            <p>✅ Your monthly report is ready!</p>
//            <p>Please find the report attached below.</p>
//        """.formatted(user.getUsername());
//
//        // ✅ Generate Monthly PDF attachment
//        byte[] pdfAttachment = generateMonthlyReportPdf(user);
//
//        // ✅ EMAIL report
//        if (user.isAlertsEnabled()) {
//            emailAlertService.sendMonthlyReport(
//                    user,
//                    "📊 Monthly Expense Report - ExpenseTracker",
//                    body,
//                    pdfAttachment
//            );
//        }
//
//        // ✅ TELEGRAM report (Only message)
//        if (user.isTelegramReportsEnabled() && user.getTelegramChatId() != null) {
//            telegramAlertService.send(user.getTelegramChatId(),
//                    "📊 Monthly Report ✅\nHello " + user.getUsername() +
//                            "\nYour monthly report PDF was sent to your email.");
//        }
//
//        // ✅ WHATSAPP report (Only message)
//        if (user.isMobileReportsEnabled() && user.getPhoneNumber() != null) {
//            smsAlertService.send(user.getPhoneNumber(),
//                    "📊 Monthly Report ✅\nHello " + user.getUsername() +
//                            "\nYour monthly report PDF was sent to your email.");
//        }
//    }
//    
//    public void sendMonthlyReportToUser(User user) throws Exception {
//
//        String body = """
//            <h2>📊 Monthly Expense Report</h2>
//            <p>Hello <b>%s</b>,</p>
//            <p>✅ Your monthly report is ready!</p>
//            <p>Attached PDF contains monthly expense charts + insights.</p>
//        """.formatted(user.getUsername());
//
//        // ✅ Generate Monthly PDF
//        byte[] pdf = generateMonthlyReportPdf(user);
//
//        // ✅ EMAIL (PDF)
//        if (user.isAlertsEnabled()) {
//            emailAlertService.sendMonthlyPdfReport(
//                    user,
//                    "📊 Monthly Expense Report - ExpenseTracker",
//                    body,
//                    pdf
//            );
//        }
//
//        // ✅ TELEGRAM Message
//        if (user.isTelegramReportsEnabled() && user.getTelegramChatId() != null) {
//            telegramAlertService.send(
//                    user.getTelegramChatId(),
//                    "📊 Monthly Report ✅\nHello " + user.getUsername() +
//                            "\nYour monthly PDF report was sent to your email ✅"
//            );
//        }
//
//        // ✅ WHATSAPP Message
//        if (user.isMobileReportsEnabled() && user.getPhoneNumber() != null) {
//            smsAlertService.send(
//                    user.getPhoneNumber(),
//                    "📊 Monthly Report ✅\nHello " + user.getUsername() +
//                            "\nYour monthly PDF report was sent to your email ✅"
//            );
//        }
//    }
    public void sendMonthlyReportToUser(User user) throws Exception {

        String body = """
            <h2>📊 Monthly Expense Report</h2>
            <p>Hello <b>%s</b>,</p>
            <p>✅ Your monthly report PDF is attached.</p>
        """.formatted(user.getUsername());

        // ✅ Generate Monthly PDF
        byte[] pdfAttachment = generateMonthlyReportPdf(user);

        // ✅ EMAIL report
        if (user.isAlertsEnabled()) {
            emailAlertService.sendMonthlyPdfReport(
                    user,
                    "📊 Monthly Expense Report - ExpenseTracker",
                    body,
                    pdfAttachment
            );
        }

       
       

        // ✅ WHATSAPP (message only)
        if (user.isMobileReportsEnabled() && user.getPhoneNumber() != null) {
            smsAlertService.send(
                    user.getPhoneNumber(),
                    "📊 Monthly Report ✅\nHello " + user.getUsername()
                            + "\nYour monthly report PDF was sent to your email."
            );
        }
    }



}