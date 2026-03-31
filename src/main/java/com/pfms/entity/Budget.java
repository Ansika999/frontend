package com.pfms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Budget entity for tracking monthly spending limits per category.
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category", "month", "year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Transaction.TransactionCategory category;

    @NotNull(message = "Budget limit is required")
    @Positive(message = "Budget limit must be positive")
    @Column(name = "budget_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetLimit;

    @Column(name = "spent_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @NotNull(message = "Month is required")
    @Column(name = "month", nullable = false)
    private Integer month;

    @NotNull(message = "Year is required")
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "alert_threshold")
    @Builder.Default
    private Integer alertThreshold = 80; // Alert when 80% of budget is used

    @Column(name = "is_alert_sent")
    @Builder.Default
    private Boolean isAlertSent = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Returns the percentage of budget used
     */
    public double getUsagePercentage() {
        if (budgetLimit.compareTo(BigDecimal.ZERO) == 0) return 0;
        return spentAmount.divide(budgetLimit, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Returns remaining budget
     */
    public BigDecimal getRemainingAmount() {
        return budgetLimit.subtract(spentAmount).max(BigDecimal.ZERO);
    }
}
