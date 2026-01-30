package com.ExpenseTracker.security.services;
import java.time.LocalDate;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.dao.TransactionServiceRepo;
import com.ExpenseTracker.mod.AdminTransactionSummary;
import com.ExpenseTracker.mod.Transaction;

@Service
public class TransactionServiceImpl implements TransactionServiceRepo {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Page<Transaction> getAllTransactions(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return transactionRepository.findAll(pageable);
        }

        return transactionRepository.searchAll(search, pageable);
    }

	@Override
	public Page<Transaction> getUserTransactions(
	        Long userId,
	        String search,
	        Pageable pageable
	) {
	    if (search == null || search.isBlank()) {
	        return transactionRepository.findByUserId(userId, pageable);
	    }

	    return transactionRepository.searchUserTransactions(
	            userId,
	            search,
	            pageable
	    );
	}
	

	   

	

	    @Override
	    public Page<Transaction> getAdminTransactions(
	            int page,
	            int size,
	            String username,
	            String search,
	            String type,
	            LocalDate startDate,
	            LocalDate endDate
	    ) {

	        Pageable pageable = PageRequest.of(
	            page,
	            size,
	            Sort.by(Sort.Direction.DESC, "date")
	        );

	        return transactionRepository.findAdminTransactions(
	            username,
	            type,
	            search,
	            startDate,
	            endDate,
	            pageable
	        );
	    }

		@Override
		public Page<Transaction> getAdminTransactions(int page, int size, String search, String type, String username,
				LocalDate startDate, LocalDate endDate, String sortBy, String direction) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AdminTransactionSummary getTotals() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Object[]> getMonthlyChartData() {
		    // Example: sum of income and expense grouped by month
		    return transactionRepository.getMonthlySummary(); // implement in repository
		}
		
	
}
