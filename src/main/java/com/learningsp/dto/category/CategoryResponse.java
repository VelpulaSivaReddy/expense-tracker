package com.learningsp.dto.category;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String icon;
    private String color;
    private Boolean isDefault;
    private Boolean editable; // false for global default categories
}
