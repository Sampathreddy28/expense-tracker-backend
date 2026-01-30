package com.ExpenseTracker.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ExpenseTracker.mod.User;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;


@Service
public class AlertService {
	@Autowired(required = false)
    private  EmailAlertService emailService;
	@Autowired(required = false)
    private  Optional<SmsAlertService> smsService;
	@Autowired(required = false)

//    public AlertService(
//    	    EmailAlertService emailService,
//            Optional<SmsAlertService> smsService,
//            Optional<TelegramAlertService> telegramService) {
//
//        this.emailService = emailService;
//        this.smsService = smsService;
//        this.telegramService = telegramService;
//    }

    // ===============================
    // 🔔 ALERT (threshold alerts)
    // ===============================
    public void sendAlert(User user, String message) {

        // ✅ ALWAYS EMAIL
        emailService.send(
                user.getEmail(),
                "⚠ Expense Alert",
                message
        );

        // 📱 OPTIONAL SMS
        if (user.isMobileAlertsEnabled()) {
            smsService.ifPresent(s ->
                    s.send(user.getPhoneNumber(), message));
        }

        // 📲 OPTIONAL TELEGRAM
       
    }

    // ===============================
    // 📄 MONTHLY REPORT (PDF)
    // ===============================
    public void sendReport(User user, byte[] pdf) throws MessagingException {

        // ✅ REPORT ALWAYS GOES TO EMAIL
        emailService.sendWithAttachment(
                user.getEmail(),
                "📊 Monthly Expense Report",
                "Hi " + user.getUsername() + ",\n\nPlease find your monthly expense report attached.",
                pdf,
                "Monthly_Expense_Report.pdf"
        );

        // 📱 OPTIONAL MOBILE NOTIFICATION
        if (user.isMobileAlertsEnabled()) {
            smsService.ifPresent(s ->
                    s.send(
                        user.getPhoneNumber(),
                        "📊 Your monthly expense report has been emailed to you."
                    ));
        }

       
       
    }
}
