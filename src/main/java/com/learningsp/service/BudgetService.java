package com.learningsp.service;

import com.learningsp.dto.budget.*;
import com.learningsp.entity.Budget;
import com.learningsp.repo.BudgetRepository;
import com.learningsp.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public BudgetResponse setBudget(Long userId, BudgetCreateRequest request) {
        Budget budget = budgetRepository.findByUserId(userId).orElseGet(() -> {
            Budget b = new Budget();
            b.setUserId(userId);
            return b;
        });

        budget.setDailyBudget(request.getDailyBudget());
        budget.setWeeklyBudget(request.getWeeklyBudget());
        budget.setMonthlyBudget(request.getMonthlyBudget());

        budgetRepository.save(budget);
        return getBudgetStatus(userId);
    }

    @Transactional
    public BudgetResponse updateBudget(Long userId, BudgetUpdateRequest request) {
        Budget budget = budgetRepository.findByUserId(userId).orElseGet(() -> {
            Budget b = new Budget();
            b.setUserId(userId);
            b.setDailyBudget(BigDecimal.ZERO);
            b.setWeeklyBudget(BigDecimal.ZERO);
            b.setMonthlyBudget(BigDecimal.ZERO);
            return b;
        });

        if (request.getDailyBudget() != null) budget.setDailyBudget(request.getDailyBudget());
        if (request.getWeeklyBudget() != null) budget.setWeeklyBudget(request.getWeeklyBudget());
        if (request.getMonthlyBudget() != null) budget.setMonthlyBudget(request.getMonthlyBudget());

        budgetRepository.save(budget);
        return getBudgetStatus(userId);
    }

    public BudgetResponse getBudgetStatus(Long userId) {
        Budget budget = budgetRepository.findByUserId(userId).orElseGet(() -> {
            Budget b = new Budget();
            b.setUserId(userId);
            b.setDailyBudget(BigDecimal.ZERO);
            b.setWeeklyBudget(BigDecimal.ZERO);
            b.setMonthlyBudget(BigDecimal.ZERO);
            return b;
        });

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek());
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal dailyUsed = expenseRepository.sumByUserAndDateRange(userId, today, today);
        BigDecimal weeklyUsed = expenseRepository.sumByUserAndDateRange(userId, weekStart, weekEnd);
        BigDecimal monthlyUsed = expenseRepository.sumByUserAndDateRange(userId, monthStart, monthEnd);

        return BudgetResponse.builder()
                .dailyBudget(budget.getDailyBudget())
                .weeklyBudget(budget.getWeeklyBudget())
                .monthlyBudget(budget.getMonthlyBudget())
                .dailyUsed(dailyUsed)
                .weeklyUsed(weeklyUsed)
                .monthlyUsed(monthlyUsed)
                .dailyRemaining(budget.getDailyBudget().subtract(dailyUsed))
                .weeklyRemaining(budget.getWeeklyBudget().subtract(weeklyUsed))
                .monthlyRemaining(budget.getMonthlyBudget().subtract(monthlyUsed))
                .dailyPercentUsed(percent(dailyUsed, budget.getDailyBudget()))
                .weeklyPercentUsed(percent(weeklyUsed, budget.getWeeklyBudget()))
                .monthlyPercentUsed(percent(monthlyUsed, budget.getMonthlyBudget()))
                .dailyExceeded(exceeds(dailyUsed, budget.getDailyBudget()))
                .weeklyExceeded(exceeds(weeklyUsed, budget.getWeeklyBudget()))
                .monthlyExceeded(exceeds(monthlyUsed, budget.getMonthlyBudget()))
                .build();
    }

    /** Convenience used by the dashboard: remaining monthly budget as a single figure. */
    public BigDecimal remainingMonthlyBudget(Long userId) {
        BudgetResponse status = getBudgetStatus(userId);
        return status.getMonthlyRemaining();
    }

    private double percent(BigDecimal used, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        return used.divide(budget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    private boolean exceeds(BigDecimal used, BigDecimal budget) {
        return budget != null && budget.compareTo(BigDecimal.ZERO) > 0 && used.compareTo(budget) > 0;
    }
}
