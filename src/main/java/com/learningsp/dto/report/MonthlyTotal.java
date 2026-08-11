package com.learningsp.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTotal {
    private String month; // e.g. "2026-08"
    private BigDecimal total;
}
