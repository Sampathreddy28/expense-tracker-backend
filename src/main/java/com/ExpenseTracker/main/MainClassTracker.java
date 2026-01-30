package com.ExpenseTracker.main;

import com.ExpenseTracker.dao.TransactionDAO;
import com.ExpenseTracker.report.ReportGenerator;
import com.ExpenseTracker.service.ExpenseService;
import com.ExpenseTracker.ui.ChartGenerator;
import com.ExpenseTracker.ui.TransactionPanel; // Assuming this is now created

import javax.swing.*;
import java.awt.*;

public class MainClassTracker {
	
    public static void main(String[] args) {

        // Ensure the UI is launched safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {

            // --- 1. CORE APPLICATION SETUP (Single Initialization) ---
            
            // 1.1 Instantiate the DAO, Service, and Report layers
            TransactionDAO transactionDAO = new TransactionDAO();
            ExpenseService expenseService = new ExpenseService(transactionDAO);
            
            // NEW: Instantiate the Report Generator
            ReportGenerator reportGenerator = new ReportGenerator(expenseService); 

            // --- 2. FRAME SETUP ---
            JFrame frame = new JFrame("Personal Expense Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750); 
            frame.setLocationRelativeTo(null);

            // --- 3. TABBED PANE SETUP ---
            JTabbedPane tabs = new JTabbedPane();
            
            // --- 4. SUMMARY DASHBOARD (Charts) ---
            ChartGenerator chartGenerator = new ChartGenerator(expenseService, tabs);
            JPanel summaryPanel = chartGenerator.getSummaryPanel();
            
            // 4.1. Add Refresh Button to a new South Panel
            JPanel southPanel = new JPanel();
            JButton refreshButton = new JButton("Refresh Data");
            refreshButton.addActionListener(e -> {
                // The TransactionPanel loadTransactions will be called by the refresh
                // action hook, if you add one to TransactionPanel's refresh logic.
                chartGenerator.updateSummaryPanel(); 
                System.out.println("Data refreshed.");
            });
            southPanel.add(refreshButton);
            
            summaryPanel.add(southPanel, BorderLayout.SOUTH);
            tabs.addTab("📊 Summary Dashboard", summaryPanel);

            // --- 5. TRANSACTION MANAGEMENT UI ---
            
            // FIX: Pass the ReportGenerator to the TransactionPanel constructor
            TransactionPanel transactionsPanel = new TransactionPanel(
                expenseService, 
                chartGenerator, 
                reportGenerator // Dependency Injection for PDF generation
            );
            tabs.addTab("➕ Transaction Management", transactionsPanel.getPanel());

            // --- 6. FINAL FRAME DISPLAY ---
            frame.add(tabs, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}