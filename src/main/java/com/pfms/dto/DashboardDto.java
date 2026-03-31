package com.pfms.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for dashboard summary data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal monthlyBalance;
    private double financialHealthScore;
    private List<TransactionDto.Response> recentTransactions;
    private List<TransactionDto.CategorySummary> categorySummaries;
    private List<TransactionDto.MonthlySummary> monthlyTrend;
    private List<BudgetDto.Response> budgets;
    private List<SavingsGoalDto.Response> savingsGoals;
    private Map<String, Object> notifications;
    private int transactionCount;
    private int totalBudgets;
    private int overspentBudgets;
}
