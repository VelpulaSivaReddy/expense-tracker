package com.learningsp.controller;

import com.learningsp.dto.budget.*;
import com.learningsp.dto.common.ApiResponse;
import com.learningsp.service.BudgetService;
import com.learningsp.util.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> getStatus(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudgetStatus(principal.getUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> set(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody BudgetCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Budget saved", budgetService.setBudget(principal.getUserId(), request)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                                @Valid @RequestBody BudgetUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Budget updated", budgetService.updateBudget(principal.getUserId(), request)));
    }
}
