package com.ExpenseTracker.security.services;
	
import java.math.BigDecimal;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.ExpenseTracker.dao.AdminService;
import com.ExpenseTracker.dao.RoleRepository;
import com.ExpenseTracker.dao.TokenRepository;
import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.mod.Role.RoleName;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategoryAnalyticsDto;
import com.ExpenseTracker.mod.dto.response.AdminChartResponse;
import com.ExpenseTracker.mod.dto.response.AdminMetricsResponse;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;

@Service("adminSecurityService")
@Primary
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TransactionRepository transactionRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository, TransactionRepository transactionRepository, TokenRepository tokenRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
		this.transactionRepository = transactionRepository;
		this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public void promoteUserToAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN);

        if (alreadyAdmin) {
            throw new RuntimeException("User is already an ADMIN");
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        user.getRoles().add(adminRole);
        userRepository.save(user);
    }


    @Override
    public AdminMetricsResponse getMetrics() {

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long totalTransactions = transactionRepository.count();

        BigDecimal totalIncome =
                transactionRepository.totalIncome() != null
                        ? transactionRepository.totalIncome()
                        : BigDecimal.ZERO;

        BigDecimal totalExpenses =
                transactionRepository.totalExpenses() != null
                        ? transactionRepository.totalExpenses()
                        : BigDecimal.ZERO;

        return new AdminMetricsResponse(
                totalUsers,
                activeUsers,
                totalTransactions,
                totalIncome,
                totalExpenses
        );
    }
    @PutMapping("/users/{id}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);

        if (isAdmin) {
            return ResponseEntity.badRequest()
                    .body("Cannot block an ADMIN user");
        }

        user.setEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok("User blocked successfully");
    }
    @Override
    public List<AdminChartResponse> getMonthlyAnalytics() {

        return transactionRepository.monthlyAnalytics()
                .stream()
                .map(row -> new AdminChartResponse(
                        "Month " + row[0],
                        (BigDecimal) row[1],
                        (BigDecimal) row[2],
                        (Long) row[3]
                ))
                .toList();
    }
//    @Override
//    @Transactional
//    public void revokeAllTokensForUser(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // 🔐 Force logout by disabling user
//        user.setEnabled(false);
//        userRepository.save(user);
//    }
    @Override
    public List<CategoryAnalyticsDto> getCategoryAnalytics() {
        return transactionRepository.getExpenseByCategory()
                .stream()
                .map(row -> new CategoryAnalyticsDto(
                        (String) row[0],
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendDto> monthlyTrendAllUsers() {
        return transactionRepository.monthlyTrendAllUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendDto> monthlyTrendByUser(String username) {
        return transactionRepository.monthlyTrendByUser(username);
    }
    private final TokenRepository tokenRepository;

    @Transactional
    public void revokeAllTokensForUser(String username) {
        tokenRepository.revokeAllValidTokensByUsername(username);
    }
    
    
}
