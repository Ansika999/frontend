package com.pfms.repository;

import com.pfms.entity.Budget;
import com.pfms.entity.Transaction.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndMonthAndYear(Long userId, int month, int year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(
            Long userId, TransactionCategory category, int month, int year);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserIdOrderByCreatedAtDesc(Long userId);
}
