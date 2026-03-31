package com.pfms.service;

import com.pfms.dto.BudgetDto;
import com.pfms.dto.DashboardDto;
import com.pfms.dto.TransactionDto;
import com.pfms.entity.Transaction.TransactionType;
import com.pfms.entity.User;
import com.pfms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates data from all services to build the dashboard summary.
 */
@Service
public class DashboardService {

    @Autowired private AuthService authService;
    @Autowired private TransactionService transactionService;
    @Autowired private BudgetService budgetService;
    @Autowired private SavingsGoalService savingsGoalService;
    @Autowired private TransactionRepository transactionRepository;

    public DashboardDto getDashboard() {
        User user = authService.getCurrentUser();
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // All-time totals
        BigDecimal totalIncome = transactionRepository
                .sumAmountByUserIdAndType(user.getId(), TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository
                .sumAmountByUserIdAndType(user.getId(), TransactionType.EXPENSE);

        // Monthly totals
        BigDecimal monthlyIncome = transactionRepository
                .sumAmountByUserIdAndTypeAndMonthAndYear(user.getId(), TransactionType.INCOME, month, year);
        BigDecimal monthlyExpense = transactionRepository
                .sumAmountByUserIdAndTypeAndMonthAndYear(user.getId(), TransactionType.EXPENSE, month, year);

        // Budget data
        List<BudgetDto.Response> budgets = budgetService.getBudgetsByMonth(month, year);
        int overspent = (int) budgets.stream().filter(BudgetDto.Response::isOverBudget).count();

        // Financial health score
        double healthScore = computeHealthScore(totalIncome, totalExpense, monthlyIncome,
                monthlyExpense, budgets, overspent);

        // Notifications
        Map<String, Object> notifications = buildNotifications(budgets, overspent);

        return DashboardDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .monthlyIncome(monthlyIncome)
                .monthlyExpense(monthlyExpense)
                .monthlyBalance(monthlyIncome.subtract(monthlyExpense))
                .financialHealthScore(healthScore)
                .recentTransactions(transactionService.getRecentTransactions())
                .categorySummaries(transactionService.getCategorySummary(month, year))
                .monthlyTrend(transactionService.getMonthlyTrend())
                .budgets(budgets)
                .savingsGoals(savingsGoalService.getAllGoals())
                .notifications(notifications)
                .totalBudgets(budgets.size())
                .overspentBudgets(overspent)
                .build();
    }

    /**
     * Compute a 0–100 financial health score based on:
     * - savings rate (income vs expense)
     * - budget compliance
     * - positive balance
     */
    private double computeHealthScore(BigDecimal totalIncome, BigDecimal totalExpense,
                                       BigDecimal monthlyIncome, BigDecimal monthlyExpense,
                                       List<BudgetDto.Response> budgets, int overspent) {
        double score = 50.0; // base

        // Savings rate component (up to +30)
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            double savingsRate = monthlyIncome.subtract(monthlyExpense)
                    .divide(monthlyIncome, 4, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
            score += Math.min(30, savingsRate * 60);
        }

        // Budget compliance component (up to +20)
        if (!budgets.isEmpty()) {
            double compliance = (double)(budgets.size() - overspent) / budgets.size();
            score += compliance * 20;
        }

        // Penalty for negative all-time balance
        if (totalIncome.subtract(totalExpense).compareTo(BigDecimal.ZERO) < 0) {
            score -= 15;
        }

        return Math.max(0, Math.min(100, score));
    }

    private Map<String, Object> buildNotifications(List<BudgetDto.Response> budgets, int overspent) {
        Map<String, Object> notifs = new HashMap<>();
        List<String> warnings = budgets.stream()
                .filter(b -> b.getUsagePercentage() >= b.getAlertThreshold())
                .map(b -> String.format("%s budget is %.0f%% used", b.getCategory(), b.getUsagePercentage()))
                .collect(Collectors.toList());
        notifs.put("budgetWarnings", warnings);
        notifs.put("overspentCount", overspent);
        return notifs;
    }
}
