package com.learningsp.dto.expense;

import com.learningsp.entity.Expense;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCreateRequest {

    @NotBlank(message = "Expense title is required")
    @Size(max = 150)
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Payment method is required")
    private Expense.PaymentMethod paymentMethod;

    @Size(max = 500)
    private String description;

    @Size(max = 500)
    private String notes;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;
}
