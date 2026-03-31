package com.pfms.dto;

import com.pfms.entity.SavingsGoal.GoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalDto {

    @Data
    public static class Request {
        @NotBlank private String title;
        private String description;
        @NotNull @Positive private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private LocalDate targetDate;
        private String icon;
        private String color;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private LocalDate targetDate;
        private GoalStatus status;
        private double progressPercentage;
        private String icon;
        private String color;
        private String createdAt;
    }

    @Data
    public static class ContributionRequest {
        @NotNull @Positive private BigDecimal amount;
    }
}
