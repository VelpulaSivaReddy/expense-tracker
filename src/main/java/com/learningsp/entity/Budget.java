package com.learningsp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long budgetId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "daily_budget", precision = 12, scale = 2)
    private BigDecimal dailyBudget;

    @Column(name = "weekly_budget", precision = 12, scale = 2)
    private BigDecimal weeklyBudget;

    @Column(name = "monthly_budget", precision = 12, scale = 2)
    private BigDecimal monthlyBudget;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
