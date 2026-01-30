package com.ExpenseTracker.ui;

import com.ExpenseTracker.service.ExpenseService;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartGenerator {

    private final ExpenseService expenseService;

    private JPanel summaryPanel;
    private JLabel balanceLabel;
    private JPanel chartContainer;
    private JTabbedPane tabbedPane;

    public ChartGenerator(ExpenseService expenseService, JTabbedPane tabbedPane) {
        this.expenseService = expenseService;
        this.tabbedPane = tabbedPane;
        this.summaryPanel = createSummaryPanel();
        // Call update immediately upon creation to populate the initial data
        updateSummaryPanel(); 
    }

    // -------------------------
    // UI PANEL CREATION
    // -------------------------

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Add spacing

        // Balance label - Center alignment is better handled by BorderLayout.NORTH
        balanceLabel = new JLabel("Net Balance: $0.00", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 22)); // Slightly larger font
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Top/Bottom padding

        panel.add(balanceLabel, BorderLayout.NORTH);

        // Chart container
        chartContainer = new JPanel(new GridLayout(1, 2, 5, 5)); // Add spacing between charts
        chartContainer.setBorder(BorderFactory.createTitledBorder("Spending Visualization"));

        panel.add(chartContainer, BorderLayout.CENTER);

        return panel;
    }

    public JPanel getSummaryPanel() {
        return summaryPanel;
    }

    // -------------------------
    // CHART CREATION METHODS
    // -------------------------

    /**
     * Creates a Pie Chart Panel for expense breakdown.
     */
    public static ChartPanel createPieChartPanel(Map<String, Double> data) {

        // Use a descriptive title when data is empty
        String chartTitle = data.isEmpty() ? 
                            "Expense Breakdown (No Data)" : 
                            "Expense Breakdown by Category";

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>(); // Generic type specified
        data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart3D( // Changed to 3D for better visual appeal
                chartTitle,
                dataset,
                true, // Show legend
                true, // Tooltips
                false // URLs
        );

        return new ChartPanel(chart);
    }

    /**
     * Creates a Line Chart Panel for monthly spending trend.
     */
    public static ChartPanel createLineChartPanel(Map<String, Double> data) {

        // Use a descriptive title when data is empty
        String chartTitle = data.isEmpty() ? 
                            "Monthly Trend (No Data)" : 
                            "Monthly Spending Trend";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add values only if data is present
        if (!data.isEmpty()) {
            data.forEach((month, total) ->
                dataset.addValue(total, "Monthly Expenses", month)
            );
        }

        JFreeChart chart = ChartFactory.createLineChart(
                chartTitle,
                "Month",
                "Amount ($)",
                dataset
        );

        return new ChartPanel(chart);
    }

    // -------------------------
    // UPDATE SUMMARY PANEL
    // -------------------------

    public void updateSummaryPanel() {
        
        // 1. Update Balance
        // FIX 1: Store the result in a BigDecimal variable
        BigDecimal netBalanceBigDecimal = expenseService.calculateNetBalance();
        
        // FIX 2: Convert to double only for String.format and comparison logic
        double netBalance = netBalanceBigDecimal.doubleValue(); 
        
        String balanceText = String.format("Net Balance: $%,.2f", netBalance);
        
        // Optional: Highlight balance color based on value
        if (netBalance < 0) {
            balanceLabel.setForeground(Color.RED);
        } else {
            balanceLabel.setForeground(Color.BLUE);
        }
        balanceLabel.setText(balanceText);

        // 2. Fetch data
        // FIX 3: Store the fetched data in the correct Map<String, BigDecimal> type
        Map<String, BigDecimal> categoryDataBigDecimal = expenseService.getExpenseSummaryByCategory();
        Map<String, BigDecimal> monthlyDataBigDecimal = expenseService.getMonthlyExpenseTrend();

        // 3. Generate new charts
        // Charting libraries often require double. We must convert the Maps here.
        
        // Helper function to convert Map<String, BigDecimal> to Map<String, Double> for charts
        Map<String, Double> categoryDataDouble = convertMapToDouble(categoryDataBigDecimal);
        Map<String, Double> monthlyDataDouble = convertMapToDouble(monthlyDataBigDecimal);

        ChartPanel pieChartPanel = createPieChartPanel(categoryDataDouble);
        ChartPanel lineChartPanel = createLineChartPanel(monthlyDataDouble);
        
        // 4. Update UI container
        chartContainer.removeAll();
        chartContainer.add(pieChartPanel);
        chartContainer.add(lineChartPanel);

        // Optimization: Use SwingUtilities.invokeLater for UI updates
        SwingUtilities.invokeLater(() -> {
            chartContainer.revalidate();
            chartContainer.repaint();

            if (tabbedPane.getSelectedComponent() == summaryPanel) {
                summaryPanel.repaint();
            }
        });
    }

    // Helper method needed to convert BigDecimal maps to Double maps for charting libraries
    private Map<String, Double> convertMapToDouble(Map<String, BigDecimal> bigDecimalMap) {
        return bigDecimalMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                e -> e.getValue().doubleValue() // Safely convert to double
            ));
    }
}