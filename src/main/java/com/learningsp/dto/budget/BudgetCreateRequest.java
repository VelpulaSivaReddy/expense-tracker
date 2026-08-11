package com.learningsp.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetCreateRequest {

    @NotNull
    @DecimalMin(value = "0.0", message = "Daily budget cannot be negative")
    private BigDecimal dailyBudget;

    @NotNull
    @DecimalMin(value = "0.0", message = "Weekly budget cannot be negative")
    private BigDecimal weeklyBudget;

    @NotNull
    @DecimalMin(value = "0.0", message = "Monthly budget cannot be negative")
    private BigDecimal monthlyBudget;
}
