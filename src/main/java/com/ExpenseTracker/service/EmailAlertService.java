package com.ExpenseTracker.service;

import java.util.List;



import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.mod.User;

import org.springframework.core.io.ByteArrayResource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Service
@ConditionalOnProperty(
	    name = "spring.mail.host",
	    matchIfMissing = true
	)
public class EmailAlertService {

    private final JavaMailSender mailSender;
    private final TransactionRepository transactionRepository;
    public EmailAlertService(JavaMailSender mailSender, TransactionRepository transactionRepository) {
        this.mailSender = mailSender;
		this.transactionRepository = transactionRepository;
    }

    public void send(String to, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(body);
        mailSender.send(mail);
    }
    public void sendWithAttachment(
            String to,
            String subject,
            String body,
            byte[] file,
            String filename) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        helper.addAttachment(filename, new ByteArrayResource(file), "image/png");

        mailSender.send(message);
    }

//    public void sendMonthlyPdfReport(User user, String subject, String body, byte[] chartPng) {
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//
//            // ✅ true = multipart enabled
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//            helper.setTo(user.getEmail());
//            helper.setSubject(subject);
//
//            // ✅ HTML enabled
//            helper.setText(body, true);
//
//            // ✅ IMPORTANT: add correct content type
//            helper.addAttachment(
//                "monthly_trend.png",
//                new ByteArrayResource(chartPng),
//                "image/png"
//            );
//
//            mailSender.send(message);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to send report email", e);
//        }
//    }
//
//    
    public void sendMonthlyPdfReport(User user, String subject, String body, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());
            helper.setSubject(subject);

            // ✅ HTML BODY
            helper.setText(body, true);

            // ✅ Attach PDF
            helper.addAttachment(
                    "Monthly_Report.pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send monthly PDF email", e);
        }
    }

    public List<Object[]> getMonthlyTrend(Long userId) {
        return transactionRepository.getMonthlyExpenseTrend(userId);
    }

}
