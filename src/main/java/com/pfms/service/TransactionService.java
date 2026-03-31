package com.pfms.service;

import com.pfms.dto.TransactionDto;
import com.pfms.entity.Budget;
import com.pfms.entity.Transaction;
import com.pfms.entity.Transaction.TransactionCategory;
import com.pfms.entity.Transaction.TransactionType;
import com.pfms.entity.User;
import com.pfms.repository.BudgetRepository;
import com.pfms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user transactions: CRUD, filtering, and budget syncing.
 */
@Service
public class TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private AuthService authService;

    /**
     * Create a new transaction and update relevant budget.
     */
    @Transactional
    public TransactionDto.Response createTransaction(TransactionDto.Request request) {
        User user = authService.getCurrentUser();

        Transaction transaction = Transaction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .tags(request.getTags())
                .user(user)
                .build();

        transaction = transactionRepository.save(transaction);

        // Update budget spent amount if this is an EXPENSE
        if (request.getType() == TransactionType.EXPENSE) {
            updateBudgetSpending(user.getId(), request.getCategory(),
                    request.getTransactionDate().getMonthValue(),
                    request.getTransactionDate().getYear(),
                    request.getAmount());
        }

        return mapToResponse(transaction);
    }

    /**
     * Update an existing transaction.
     */
    @Transactional
    public TransactionDto.Response updateTransaction(Long id, TransactionDto.Request request) {
        User user = authService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Reverse old budget effect
        if (transaction.getType() == TransactionType.EXPENSE) {
            updateBudgetSpending(user.getId(), transaction.getCategory(),
                    transaction.getTransactionDate().getMonthValue(),
                    transaction.getTransactionDate().getYear(),
                    transaction.getAmount().negate());
        }

        transaction.setTitle(request.getTitle());
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setReferenceNumber(request.getReferenceNumber());
        if (request.getIsRecurring() != null) transaction.setIsRecurring(request.getIsRecurring());
        transaction.setTags(request.getTags());

        transaction = transactionRepository.save(transaction);

        // Apply new budget effect
        if (request.getType() == TransactionType.EXPENSE) {
            updateBudgetSpending(user.getId(), request.getCategory(),
                    request.getTransactionDate().getMonthValue(),
                    request.getTransactionDate().getYear(),
                    request.getAmount());
        }

        return mapToResponse(transaction);
    }

    /**
     * Delete a transaction and reverse its budget impact.
     */
    @Transactional
    public void deleteTransaction(Long id) {
        User user = authService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getType() == TransactionType.EXPENSE) {
            updateBudgetSpending(user.getId(), transaction.getCategory(),
                    transaction.getTransactionDate().getMonthValue(),
                    transaction.getTransactionDate().getYear(),
                    transaction.getAmount().negate());
        }

        transactionRepository.delete(transaction);
    }

    /**
     * Get paginated list of transactions for current user.
     */
    public Page<TransactionDto.Response> getTransactions(int page, int size) {
        User user = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get transaction by ID.
     */
    public TransactionDto.Response getTransactionById(Long id) {
        User user = authService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return mapToResponse(transaction);
    }

    /**
     * Get recent transactions (top 10) for dashboard.
     */
    public List<TransactionDto.Response> getRecentTransactions() {
        User user = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(0, 10);
        return transactionRepository
                .findTop10ByUserIdOrderByTransactionDateDesc(user.getId(), pageable)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get transactions filtered by type and/or category.
     */
    public List<TransactionDto.Response> getFilteredTransactions(TransactionDto.FilterRequest filter) {
        User user = authService.getCurrentUser();
        List<Transaction> transactions;

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            transactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                            user.getId(), filter.getStartDate(), filter.getEndDate());
        } else if (filter.getType() != null) {
            transactions = transactionRepository
                    .findByUserIdAndTypeOrderByTransactionDateDesc(user.getId(), filter.getType());
        } else if (filter.getCategory() != null) {
            transactions = transactionRepository
                    .findByUserIdAndCategoryOrderByTransactionDateDesc(user.getId(), filter.getCategory());
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());
        }

        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Calculate total income or expense for user.
     */
    public BigDecimal getTotalByType(TransactionType type) {
        User user = authService.getCurrentUser();
        return transactionRepository.sumAmountByUserIdAndType(user.getId(), type);
    }

    /**
     * Get category-wise expense summary for a specific month.
     */
    public List<TransactionDto.CategorySummary> getCategorySummary(int month, int year) {
        User user = authService.getCurrentUser();
        List<Object[]> rawData = transactionRepository
                .findCategoryWiseSummary(user.getId(), month, year);

        BigDecimal totalExpense = rawData.stream()
                .filter(row -> TransactionType.EXPENSE.name().equals(row[1].toString()))
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rawData.stream()
                .filter(row -> TransactionType.EXPENSE.name().equals(row[1].toString()))
                .map(row -> {
                    TransactionDto.CategorySummary summary = new TransactionDto.CategorySummary();
                    summary.setCategory((TransactionCategory) row[0]);
                    summary.setTotalAmount((BigDecimal) row[2]);
                    summary.setTransactionCount(0);
                    double pct = totalExpense.compareTo(BigDecimal.ZERO) > 0
                            ? ((BigDecimal) row[2]).divide(totalExpense, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0;
                    summary.setPercentage(pct);
                    return summary;
                }).collect(Collectors.toList());
    }

    /**
     * Get 6-month monthly trend for charts.
     */
    public List<TransactionDto.MonthlySummary> getMonthlyTrend() {
        User user = authService.getCurrentUser();
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Object[]> raw = transactionRepository.findMonthlyTrend(user.getId(), sixMonthsAgo);

        // Build a map of month/year -> summary
        java.util.Map<String, TransactionDto.MonthlySummary> map = new java.util.LinkedHashMap<>();
        for (Object[] row : raw) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            String key = year + "-" + month;
            TransactionDto.MonthlySummary summary = map.computeIfAbsent(key, k -> {
                TransactionDto.MonthlySummary s = new TransactionDto.MonthlySummary();
                s.setMonth(month);
                s.setYear(year);
                s.setTotalIncome(BigDecimal.ZERO);
                s.setTotalExpense(BigDecimal.ZERO);
                return s;
            });
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = (BigDecimal) row[3];
            if (type == TransactionType.INCOME) summary.setTotalIncome(amount);
            else summary.setTotalExpense(amount);
        }
        map.values().forEach(s ->
                s.setNetBalance(s.getTotalIncome().subtract(s.getTotalExpense())));
        return new java.util.ArrayList<>(map.values());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void updateBudgetSpending(Long userId, TransactionCategory category,
                                       int month, int year, BigDecimal delta) {
        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(userId, category, month, year)
                .ifPresent(budget -> {
                    budget.setSpentAmount(budget.getSpentAmount().add(delta).max(BigDecimal.ZERO));
                    budgetRepository.save(budget);
                });
    }

    public TransactionDto.Response mapToResponse(Transaction t) {
        TransactionDto.Response r = new TransactionDto.Response();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setAmount(t.getAmount());
        r.setType(t.getType());
        r.setCategory(t.getCategory());
        r.setTransactionDate(t.getTransactionDate());
        r.setPaymentMethod(t.getPaymentMethod());
        r.setReferenceNumber(t.getReferenceNumber());
        r.setIsRecurring(t.getIsRecurring());
        r.setTags(t.getTags());
        if (t.getCreatedAt() != null)
            r.setCreatedAt(t.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return r;
    }
}
