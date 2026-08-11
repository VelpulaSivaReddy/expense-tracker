package com.learningsp.service;

import com.learningsp.dto.category.*;
import com.learningsp.entity.Category;
import com.learningsp.exception.BadRequestException;
import com.learningsp.exception.DuplicateResourceException;
import com.learningsp.exception.ResourceNotFoundException;
import com.learningsp.repo.CategoryRepository;
import com.learningsp.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public List<CategoryResponse> getAllForUser(Long userId) {
        return categoryRepository.findAllVisibleToUser(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryCreateRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndUserId(request.getCategoryName(), userId)) {
            throw new DuplicateResourceException("You already have a category named '" + request.getCategoryName() + "'");
        }

        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .icon(request.getIcon() != null ? request.getIcon() : "tag")
                .color(request.getColor() != null ? request.getColor() : "#22C55E")
                .isDefault(false)
                .userId(userId)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not editable"));

        category.setCategoryName(request.getCategoryName());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getColor() != null) category.setColor(request.getColor());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = categoryRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not editable"));

        boolean inUse = expenseRepository.countByUserIdAndCategory_CategoryId(userId, categoryId) > 0;

        if (inUse) {
            throw new BadRequestException("Cannot delete a category that has expenses. Reassign or delete those expenses first.");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {
        boolean isGlobalDefault = category.getUserId() == null;
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .icon(category.getIcon())
                .color(category.getColor())
                .isDefault(Boolean.TRUE.equals(category.getIsDefault()))
                .editable(!isGlobalDefault)
                .build();
    }
}
