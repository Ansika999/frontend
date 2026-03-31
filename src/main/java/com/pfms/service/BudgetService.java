package com.pfms.service;

import com.pfms.dto.BudgetDto;
import com.pfms.entity.Budget;
import com.pfms.entity.User;
import com.pfms.repository.BudgetRepository;
import com.pfms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired private BudgetRepository budgetRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AuthService authService;

    @Transactional
    public BudgetDto.Response createBudget(BudgetDto.Request request) {
        User user = authService.getCurrentUser();

        // Check for duplicate
        if (budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                user.getId(), request.getCategory(), request.getMonth(), request.getYear()).isPresent()) {
            throw new RuntimeException("Budget for this category and month already exists");
        }

        // Calculate already-spent amount
        BigDecimal spent = transactionRepository.sumExpenseByUserIdAndCategoryAndMonthAndYear(
                user.getId(), request.getCategory(), request.getMonth(), request.getYear());

        Budget budget = Budget.builder()
                .category(request.getCategory())
                .budgetLimit(request.getBudgetLimit())
                .spentAmount(spent)
                .month(request.getMonth())
                .year(request.getYear())
                .alertThreshold(request.getAlertThreshold())
                .user(user)
                .build();

        return mapToResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDto.Response updateBudget(Long id, BudgetDto.Request request) {
        User user = authService.getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        budget.setBudgetLimit(request.getBudgetLimit());
        budget.setAlertThreshold(request.getAlertThreshold());
        return mapToResponse(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(Long id) {
        User user = authService.getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }

    public List<BudgetDto.Response> getBudgetsByMonth(int month, int year) {
        User user = authService.getCurrentUser();
        return budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BudgetDto.Response> getAllBudgets() {
        User user = authService.getCurrentUser();
        return budgetRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BudgetDto.Response mapToResponse(Budget b) {
        BudgetDto.Response r = new BudgetDto.Response();
        r.setId(b.getId());
        r.setCategory(b.getCategory());
        r.setBudgetLimit(b.getBudgetLimit());
        r.setSpentAmount(b.getSpentAmount());
        r.setRemainingAmount(b.getRemainingAmount());
        r.setMonth(b.getMonth());
        r.setYear(b.getYear());
        r.setAlertThreshold(b.getAlertThreshold());
        r.setUsagePercentage(b.getUsagePercentage());
        r.setOverBudget(b.getSpentAmount().compareTo(b.getBudgetLimit()) > 0);
        if (b.getCreatedAt() != null)
            r.setCreatedAt(b.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return r;
    }
}
