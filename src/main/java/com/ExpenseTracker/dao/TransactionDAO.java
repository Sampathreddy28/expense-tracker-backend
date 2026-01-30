package com.ExpenseTracker.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.ExpenseTracker.mod.Transaction;
import java.time.LocalDate;

public class TransactionDAO {
    
    // SQLite JDBC connection string
	// OLD (SQLite):
	// private static final String JDBC_URL = "jdbc:sqlite:expense_tracker.db"; 

	// NEW (MySQL - You must update the host, port, database name, user, and password):
	private static final String DB_URL = "jdbc:mysql://localhost:3306/expense_tracker_db";
	private static final String DB_USER = "root"; // <-- CHANGE THIS
	private static final String DB_PASS = "Sampath@28"; // <-- CHANGE THIS

	private Connection getConnection() throws SQLException {
	    // You may also need to explicitly load the driver depending on your Java version/setup:
	    // try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { e.printStackTrace(); }
	    return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	}
    
    /**
     * Establishes a connection to the SQLite database.
     */
   

    /**
     * Initializes the database table if it doesn't exist.
     */
	// ... inside TransactionDAO.java ...

	/**
	 * Initializes the database table if it doesn't exist using MySQL syntax.
	 */
	public void createTable() {
	    // Note the change from 'INTEGER PRIMARY KEY AUTOINCREMENT' 
	    // to 'BIGINT PRIMARY KEY AUTO_INCREMENT'
	    String sql = "CREATE TABLE IF NOT EXISTS transactions ("
	               + "id BIGINT PRIMARY KEY AUTO_INCREMENT," 
	               + "amount DOUBLE NOT NULL," // Use DOUBLE or DECIMAL for money
	               + "type VARCHAR(50) NOT NULL,"    
	               + "category VARCHAR(100) NOT NULL,"
	               + "description VARCHAR(255),"
	               + "date DATE NOT NULL" // Use DATE type
	               + ");";
	    
	    try (Connection conn = getConnection();
	         Statement stmt = conn.createStatement()) {
	        stmt.execute(sql);
	        System.out.println("Transactions table created or already exists (MySQL).");
	    } catch (SQLException e) {
	        System.err.println("Error creating table: " + e.getMessage());
	    }
	}

	// ... rest of the TransactionDAO class ...
    // --- CREATE (Fixed Type Mismatch) ---

    /**
     * Saves a new transaction to the database.
     * @param transaction The Transaction object to save.
     * @return The ID of the newly inserted transaction.
     */
 // package com.ExpenseTracker.dao; ...

    /**
     * Saves a new transaction to the database.
     * @param transaction The Transaction object to save.
     * @return The ID of the newly inserted transaction (as int).
     */
	// Inside TransactionDAO.java's addTransaction method
	public int addTransaction(Transaction transaction) {
	    // We assume the SQL column for category is the foreign key ID (e.g., category_id)
	    String sql = "INSERT INTO transactions(amount, type, category_id, description, date) VALUES(?, ?, ?, ?, ?)";
	    int generatedId = -1;
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	        
	        // 1. FIX: BigDecimal to JDBC
	        // Use setBigDecimal() instead of setDouble()
	        pstmt.setBigDecimal(1, transaction.getAmount());
	        
	        // 2. FIX: Transaction.Type (Enum) to String
	        // Must call .toString() on the Enum
	        pstmt.setString(2, transaction.getType().toString());
	        
	        // 3. FIX: Category Entity to Category ID (Long)
	        // You cannot save the whole Category entity; you must save its ID (the foreign key)
	        pstmt.setLong(3, transaction.getCategory().getId()); 
	        
	        // 4. Date conversion (assuming date column is DATE or VARCHAR)
	        pstmt.setString(4, transaction.getDescription());
	        pstmt.setDate(5, java.sql.Date.valueOf(transaction.getDate()));
	        
	        // ... rest of the JDBC logic (executeUpdate, getGeneratedKeys)
	        // ...
	        
	    } catch (SQLException e) {
	        System.err.println("Error adding transaction: " + e.getMessage());
	    }
	    return generatedId;
	}

// ... rest of the TransactionDAO class ...
    
    // --- READ (Fixed Type Mismatch) ---
    
    /**
     * Retrieves a single transaction by its ID from the database.
     * @param id The ID of the transaction to retrieve.
     * @return The corresponding Transaction object, or null if not found.
     */
    public Transaction getTransactionById(int id) {
        String sql = "SELECT id, amount, type, category, description, date FROM transactions WHERE id = ?";
        Transaction transaction = null;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long txId = rs.getLong("id"); // Use getLong() to ensure compatibility
                    double amount = rs.getDouble("amount");
                    String type = rs.getString("type");
                    String category = rs.getString("category");
                    String description = rs.getString("description");
                    LocalDate date = LocalDate.parse(rs.getString("date")); 
                    
                    transaction = new Transaction(amount, type, category, description, date);
                    // FIX: Use the long ID
                    transaction.setId(txId); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transaction by ID: " + e.getMessage());
        }
        return transaction;
    }

    /**
     * Retrieves all transactions from the database.
     * @return A list of all Transaction objects.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        // Note: Ordering by date DESC and then id DESC is a good practice for recent transactions
        String sql = "SELECT id, amount, type, category, description, date FROM transactions ORDER BY date DESC, id DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Map the ResultSet row back to a Transaction object
                long txId = rs.getLong("id"); // Use getLong() to ensure compatibility
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");
                String category = rs.getString("category");
                String description = rs.getString("description");
                // Convert String date from DB back to LocalDate
                LocalDate date = LocalDate.parse(rs.getString("date")); 
                
                Transaction transaction = new Transaction(amount, type, category, description, date);
                // FIX: Use the long ID
                transaction.setId(txId);
                
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }
        return transactions;
    }

    // --- UPDATE (Fixed Type Mismatch) ---

    /**
     * Updates an existing transaction in the database.
     * @param transaction The Transaction object containing the updated data and existing ID.
     * @return true if the transaction was updated successfully, false otherwise.
     */
    public boolean updateTransaction(Transaction transaction) {
        // NOTE: The SQL should reference the category's foreign key ID, not the whole Category entity!
        String sql = "UPDATE transactions SET amount = ?, type = ?, category_id = ?, description = ?, date = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 1. AMOUNT (Still needs fixing if you use BigDecimal)
            // Assuming the amount column is DECIMAL/NUMERIC in DB. 
            // If getAmount() returns BigDecimal:
            pstmt.setBigDecimal(1, transaction.getAmount()); // ✅ Use setBigDecimal()
            // If the DB column is DOUBLE, you must still use the correct type setter for the data type.
            
            // 2. TYPE (FIX: Convert Enum to String)
            // Use .toString() or .name() to get the String representation of the Enum.
            pstmt.setString(2, transaction.getType().toString()); // ✅ FIX: Use .toString()
            
            // 3. CATEGORY (FIX: Convert Category Entity to its foreign key ID)
            // You cannot save the whole Category object; you must save its ID.
            // Assuming Category entity has a getId() method:
            pstmt.setLong(3, transaction.getCategory().getId()); // ✅ FIX: Use setLong() with the ID
            
            // 4. DESCRIPTION (String is fine)
            pstmt.setString(4, transaction.getDescription());
            
            // 5. DATE (Use JDBC native method for dates)
            pstmt.setDate(5, java.sql.Date.valueOf(transaction.getDate())); // ✅ Best practice for dates
            
            // 6. ID (Assumed correct, using Long to int conversion)
            pstmt.setLong(6, transaction.getId()); // Use setLong to match BIGINT primary key
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            return false;
        }
    }
    // --- DELETE ---

    /**
     * Deletes a transaction from the database using its ID.
     * @param id The ID of the transaction to delete.
     * @return true if the transaction was deleted successfully, false otherwise.
     */
    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return false;
        }
    }
}