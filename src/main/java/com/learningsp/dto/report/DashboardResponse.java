package com.learningsp.dto.report;

import com.learningsp.dto.expense.ExpenseResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private BigDecimal totalExpenses;
    private BigDecimal todayExpenses;
    private BigDecimal weekExpenses;
    private BigDecimal monthExpenses;
    private BigDecimal remainingBudget;
    private long totalCategories;
    private List<ExpenseResponse> recentTransactions;
    private List<MonthlyTotal> monthlyOverview;
}
