package com.learningsp.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUpdateRequest {

    @DecimalMin(value = "0.0", message = "Daily budget cannot be negative")
    private BigDecimal dailyBudget;

    @DecimalMin(value = "0.0", message = "Weekly budget cannot be negative")
    private BigDecimal weeklyBudget;

    @DecimalMin(value = "0.0", message = "Monthly budget cannot be negative")
    private BigDecimal monthlyBudget;
}
