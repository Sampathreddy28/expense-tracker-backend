package com.ExpenseTracker.Scheduler;

import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.service.ReportingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonthlyReportScheduler {

    private final UserRepository userRepository;
    private final ReportingService reportingService;

    public MonthlyReportScheduler(UserRepository userRepository, ReportingService reportingService) {
        this.userRepository = userRepository;
        this.reportingService = reportingService;
    }

    // ✅ Runs every month 1st day at 9:00 AM
//    @Scheduled(cron = "*/30 * * * * *")
    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlyReportsAutomatically() {

        System.out.println("✅ Monthly Scheduler Running...");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                reportingService.sendMonthlyReportToUser(user);
                System.out.println("✅ Sent monthly report to: " + user.getUsername());
            } catch (Exception e) {
                System.out.println("❌ Failed monthly report for: " + user.getUsername());
                e.printStackTrace();
            }
        }
    }
}
