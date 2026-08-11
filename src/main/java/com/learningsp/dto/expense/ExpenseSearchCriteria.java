package com.learningsp.dto.expense;

import com.learningsp.entity.Expense;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Bound from request params on GET /api/expenses to drive filtering, search, sorting and pagination. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSearchCriteria {
    private String keyword;
    private Long categoryId;
    private Expense.PaymentMethod paymentMethod;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String sortBy;
    private String sortDir;
    private Integer page;
    private Integer size;
}
