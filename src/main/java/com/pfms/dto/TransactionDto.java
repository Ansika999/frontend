package com.pfms.dto;

import com.pfms.entity.Transaction.TransactionCategory;
import com.pfms.entity.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs for Transaction CRUD operations.
 */
public class TransactionDto {

    @Data
    public static class Request {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;

        @NotNull @Positive
        private BigDecimal amount;

        @NotNull private TransactionType type;
        @NotNull private TransactionCategory category;
        @NotNull private LocalDate transactionDate;

        private String paymentMethod;
        private String referenceNumber;
        private Boolean isRecurring = false;
        private String tags;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private BigDecimal amount;
        private TransactionType type;
        private TransactionCategory category;
        private LocalDate transactionDate;
        private String paymentMethod;
        private String referenceNumber;
        private Boolean isRecurring;
        private String tags;
        private String createdAt;
    }

    @Data
    public static class FilterRequest {
        private TransactionType type;
        private TransactionCategory category;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer page = 0;
        private Integer size = 20;
    }

    @Data
    public static class CategorySummary {
        private TransactionCategory category;
        private BigDecimal totalAmount;
        private long transactionCount;
        private double percentage;
    }

    @Data
    public static class MonthlySummary {
        private int month;
        private int year;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netBalance;
    }
}
