package com.ExpenseTracker.ui;

import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.report.ReportGenerator;
import com.ExpenseTracker.service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.File; // Needed for JFileChooser

/**
 * Manages the Transaction Form and the JTable display for adding, viewing, and deleting transactions.
 * Also includes the functionality to generate the PDF report.
 */
public class TransactionPanel {

    private final ExpenseService expenseService;
    private final ChartGenerator chartGenerator;
    private final ReportGenerator reportGenerator; // REQUIRED FIELD
    
    private final JPanel mainPanel;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField amountField, descriptionField;
    private JComboBox<String> typeComboBox, categoryComboBox;
    private JTextField dateField; 

    // --- CONSTRUCTOR (Updated to accept 3 dependencies) ---
    public TransactionPanel(ExpenseService service, ChartGenerator chartGen, ReportGenerator reportGen) {
        this.expenseService = service;
        this.chartGenerator = chartGen;
        this.reportGenerator = reportGen; // ASSIGNMENT for ReportGenerator
        this.mainPanel = new JPanel(new BorderLayout());
        
        initializeForm();
        initializeTable();
        loadTransactions();
    }
    
    public JPanel getPanel() {
        return mainPanel;
    }

    // -------------------------
    // UI INITIALIZATION
    // -------------------------

    private void initializeForm() {
        JPanel formContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Transaction"));
        
        // --- Input Fields ---
        amountField = new JTextField(10);
        descriptionField = new JTextField(15);
        dateField = new JTextField(LocalDate.now().toString(), 10);

        typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
        categoryComboBox = new JComboBox<>(new String[]{"Food", "Rent", "Salary", "Utilities", "Entertainment", "Other"});

        formPanel.add(new JLabel("Amount ($):"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryComboBox);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        
        // --- Action Button ---
        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(e -> addTransaction());
        formPanel.add(new JLabel());
        formPanel.add(addButton);
        
        formContainer.add(formPanel);
        mainPanel.add(formContainer, BorderLayout.NORTH);
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "Date", "Type", "Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 return columnIndex == 0 ? Long.class : super.getColumnClass(columnIndex);
             }
        };
        transactionTable = new JTable(tableModel);
        
        // Hide the ID column
        transactionTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(0);
        transactionTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Buttons Panel (South) ---
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        
        // NEW: PDF Report Button
        JButton reportButton = new JButton("Generate PDF Report");
        reportButton.addActionListener(e -> generateReportAction()); 
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(deleteButton);
        southPanel.add(reportButton); // ADDED PDF BUTTON
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);
    }
    
    // -------------------------
    // DATA AND CRUD LOGIC
    // -------------------------
    
    /**
     * Retrieves all transactions from the service and populates the JTable.
     */
    private void loadTransactions() {
        tableModel.setRowCount(0); 
        List<Transaction> transactions = expenseService.getAllTransactions();
        
        for (Transaction t : transactions) {
            Object[] row = new Object[]{
                t.getId(), 
                t.getDate().toString(), 
                t.getType(), 
                t.getCategory(), 
                String.format("$%,.2f", t.getAmount()), 
                t.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addTransaction() {
        try {
            Double amount = Double.parseDouble(amountField.getText());
            String type = (String) typeComboBox.getSelectedItem();
            String category = (String) categoryComboBox.getSelectedItem();
            String description = descriptionField.getText();
            LocalDate date = LocalDate.parse(dateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            
            if (amount <= 0) {
                 JOptionPane.showMessageDialog(mainPanel, "Amount must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            Transaction newTx = new Transaction(amount, type, category, description, date);
            expenseService.addTransaction(newTx);
            
            // Clear fields and refresh UI
            amountField.setText("");
            descriptionField.setText("");
            loadTransactions();
            chartGenerator.updateSummaryPanel();
            
        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(mainPanel, "Invalid input format. Check Amount (number) and Date (YYYY-MM-DD).", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Error adding transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow != -1) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0); 
            
            int confirm = JOptionPane.showConfirmDialog(mainPanel, "Are you sure you want to delete this transaction?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (expenseService.deleteTransaction(id.intValue())) {
                    loadTransactions();
                    chartGenerator.updateSummaryPanel();
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Failed to delete transaction.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Please select a transaction to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // -------------------------
    // REPORT GENERATION LOGIC
    // -------------------------

    /**
     * Handles the PDF report generation process and file selection using JFileChooser.
     */
    private void generateReportAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Financial Report");
        
        // Suggest a default file name
        fileChooser.setSelectedFile(new File("ExpenseReport_" + LocalDate.now() + ".pdf")); 

        int userSelection = fileChooser.showSaveDialog(mainPanel);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String destPath = fileChooser.getSelectedFile().getAbsolutePath();
            
            // Ensure the file has the .pdf extension
            if (!destPath.toLowerCase().endsWith(".pdf")) {
                destPath += ".pdf";
            }
            
            try {
                // Call the ReportGenerator method
                reportGenerator.generateFullReport(destPath); 
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "Report successfully saved to:\n" + destPath, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Error generating PDF: " + ex.getMessage() + "\nCheck iText setup and file permissions.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}