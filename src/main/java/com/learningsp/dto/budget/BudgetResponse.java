package com.learningsp.dto.budget;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
    private BigDecimal dailyBudget;
    private BigDecimal weeklyBudget;
    private BigDecimal monthlyBudget;

    private BigDecimal dailyUsed;
    private BigDecimal weeklyUsed;
    private BigDecimal monthlyUsed;

    private BigDecimal dailyRemaining;
    private BigDecimal weeklyRemaining;
    private BigDecimal monthlyRemaining;

    private double dailyPercentUsed;
    private double weeklyPercentUsed;
    private double monthlyPercentUsed;

    private boolean dailyExceeded;
    private boolean weeklyExceeded;
    private boolean monthlyExceeded;
}
