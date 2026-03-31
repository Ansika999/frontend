package com.pfms.repository;

import com.pfms.entity.Transaction;
import com.pfms.entity.Transaction.TransactionCategory;
import com.pfms.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    // Sum by type and user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    // Monthly sum by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = :type AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumAmountByUserIdAndTypeAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("month") int month,
            @Param("year") int year);

    // Monthly sum by category
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.category = :category AND t.type = 'EXPENSE' " +
           "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumExpenseByUserIdAndCategoryAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("category") TransactionCategory category,
            @Param("month") int month,
            @Param("year") int year);

    // Recent transactions
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<Transaction> findTop10ByUserIdOrderByTransactionDateDesc(@Param("userId") Long userId, Pageable pageable);

    // Category-wise summary
    @Query("SELECT t.category, t.type, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND MONTH(t.transactionDate) = :month " +
           "AND YEAR(t.transactionDate) = :year GROUP BY t.category, t.type")
    List<Object[]> findCategoryWiseSummary(@Param("userId") Long userId,
                                            @Param("month") int month,
                                            @Param("year") int year);

    // Monthly summary for charts (last 6 months)
    @Query("SELECT MONTH(t.transactionDate), YEAR(t.transactionDate), t.type, SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate >= :startDate GROUP BY MONTH(t.transactionDate), YEAR(t.transactionDate), t.type " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> findMonthlyTrend(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    // Date range filter
    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Type filter
    List<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(Long userId, TransactionType type);

    // Category filter
    List<Transaction> findByUserIdAndCategoryOrderByTransactionDateDesc(Long userId, TransactionCategory category);
}
