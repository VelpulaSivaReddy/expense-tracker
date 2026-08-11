package com.learningsp.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String categoryName;

    private String icon;

    private String color;
}
