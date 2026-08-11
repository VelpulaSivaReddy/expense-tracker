package com.learningsp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "is_default")
    private Boolean isDefault;

    /** NULL means it's a global default category shared by all users. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
