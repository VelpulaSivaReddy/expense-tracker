package com.learningsp.service;

import com.learningsp.dto.expense.ExpenseResponse;
import com.learningsp.dto.report.*;
import com.learningsp.repo.CategoryRepository;
import com.learningsp.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    public DashboardResponse getDashboard(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek());
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal total = expenseService.sumAll(userId);
        BigDecimal todayTotal = expenseService.sumRange(userId, today, today);
        BigDecimal weekTotal = expenseService.sumRange(userId, weekStart, today);
        BigDecimal monthTotal = expenseService.sumRange(userId, monthStart, monthEnd);

        List<ExpenseResponse> recent = expenseService.recent(userId, 10);
        long categoryCount = categoryRepository.findAllVisibleToUser(userId).size();

        return DashboardResponse.builder()
                .totalExpenses(total)
                .todayExpenses(todayTotal)
                .weekExpenses(weekTotal)
                .monthExpenses(monthTotal)
                .remainingBudget(budgetService.remainingMonthlyBudget(userId))
                .totalCategories(categoryCount)
                .recentTransactions(recent)
                .monthlyOverview(monthlyOverview(userId))
                .build();
    }

    public ReportResponse getReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate prevYearStart = yearStart.minusYears(1);
        LocalDate prevYearEnd = prevYearStart.withDayOfYear(prevYearStart.lengthOfYear());

        BigDecimal currentMonth = expenseService.sumRange(userId, monthStart, monthEnd);
        BigDecimal previousMonth = expenseService.sumRange(userId, prevMonthStart, prevMonthEnd);
        BigDecimal currentYear = expenseService.sumRange(userId, yearStart, today);
        BigDecimal previousYear = expenseService.sumRange(userId, prevYearStart, prevYearEnd);

        List<CategoryTotal> categoryBreakdown = categoryBreakdown(userId, monthStart, monthEnd);

        return ReportResponse.builder()
                .monthlyExpenses(monthlyOverview(userId))
                .categoryBreakdown(categoryBreakdown)
                .weeklyExpenses(weeklyOverview(userId))
                .currentMonthTotal(currentMonth)
                .previousMonthTotal(previousMonth)
                .monthOverMonthChangePercent(percentChange(previousMonth, currentMonth))
                .currentYearTotal(currentYear)
                .previousYearTotal(previousYear)
                .yearOverYearChangePercent(percentChange(previousYear, currentYear))
                .topSpendingCategories(categoryBreakdown.stream().limit(5).toList())
                .build();
    }

    private List<MonthlyTotal> monthlyOverview(Long userId) {
        return expenseRepository.sumGroupedByMonth(userId).stream()
                .map(row -> MonthlyTotal.builder()
                        .month((String) row[0])
                        .total((BigDecimal) row[1])
                        .build())
                .toList();
    }

    private List<MonthlyTotal> weeklyOverview(Long userId) {
        LocalDate today = LocalDate.now();
        List<MonthlyTotal> weeks = new java.util.ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            LocalDate weekStart = today.minusWeeks(i).with(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek());
            LocalDate weekEnd = weekStart.plusDays(6);
            BigDecimal total = expenseService.sumRange(userId, weekStart, weekEnd);
            weeks.add(MonthlyTotal.builder().month(weekStart.toString()).total(total).build());
        }
        return weeks;
    }

    private List<CategoryTotal> categoryBreakdown(Long userId, LocalDate start, LocalDate end) {
        List<Object[]> rows = expenseRepository.sumGroupedByCategory(userId, start, end);
        BigDecimal grandTotal = rows.stream()
                .map(r -> (BigDecimal) r[3])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(row -> {
            BigDecimal amount = (BigDecimal) row[3];
            double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(grandTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return CategoryTotal.builder()
                    .categoryId((Long) row[0])
                    .categoryName((String) row[1])
                    .color((String) row[2])
                    .total(amount)
                    .percentage(pct)
                    .build();
        }).toList();
    }

    private double percentChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
