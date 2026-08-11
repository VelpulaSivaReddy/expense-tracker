package com.learningsp.controller;

import com.learningsp.dto.category.*;
import com.learningsp.dto.common.ApiResponse;
import com.learningsp.service.CategoryService;
import com.learningsp.util.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllForUser(principal.getUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created", response));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @PathVariable Long categoryId,
                                                                  @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse response = categoryService.update(principal.getUserId(), categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated", response));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable Long categoryId) {
        categoryService.delete(principal.getUserId(), categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
