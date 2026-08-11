package com.learningsp.dto.expense;

import com.learningsp.entity.Expense;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long expenseId;
    private String title;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Expense.PaymentMethod paymentMethod;
    private String description;
    private String notes;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
}
