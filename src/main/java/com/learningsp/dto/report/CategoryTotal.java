package com.learningsp.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTotal {
    private Long categoryId;
    private String categoryName;
    private String color;
    private BigDecimal total;
    private double percentage;
}
