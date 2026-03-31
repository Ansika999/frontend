package com.pfms.dto;

import com.pfms.entity.Transaction.TransactionCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

public class BudgetDto {

    @Data
    public static class Request {
        @NotNull private TransactionCategory category;
        @NotNull @Positive private BigDecimal budgetLimit;
        @NotNull private Integer month;
        @NotNull private Integer year;
        private Integer alertThreshold = 80;
    }

    @Data
    public static class Response {
        private Long id;
        private TransactionCategory category;
        private BigDecimal budgetLimit;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private Integer month;
        private Integer year;
        private Integer alertThreshold;
        private double usagePercentage;
        private boolean isOverBudget;
        private String createdAt;
    }
}
