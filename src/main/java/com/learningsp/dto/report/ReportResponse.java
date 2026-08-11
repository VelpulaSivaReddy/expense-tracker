package com.learningsp.dto.report;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private List<MonthlyTotal> monthlyExpenses;
    private List<CategoryTotal> categoryBreakdown;
    private List<MonthlyTotal> weeklyExpenses;
    private BigDecimal currentMonthTotal;
    private BigDecimal previousMonthTotal;
    private double monthOverMonthChangePercent;
    private BigDecimal currentYearTotal;
    private BigDecimal previousYearTotal;
    private double yearOverYearChangePercent;
    private List<CategoryTotal> topSpendingCategories;
}
