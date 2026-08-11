package com.learningsp.controller;

import com.learningsp.dto.common.ApiResponse;
import com.learningsp.dto.common.PageResponse;
import com.learningsp.dto.expense.*;
import com.learningsp.entity.Expense;
import com.learningsp.service.ExpenseService;
import com.learningsp.util.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody ExpenseCreateRequest request) {
        ExpenseResponse response = expenseService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Expense added", response));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable Long expenseId,
                                                                 @Valid @RequestBody ExpenseUpdateRequest request) {
        ExpenseResponse response = expenseService.update(principal.getUserId(), expenseId, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated", response));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable Long expenseId) {
        expenseService.delete(principal.getUserId(), expenseId);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @PathVariable Long expenseId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getById(principal.getUserId(), expenseId)));
    }

    /** Combined listing endpoint: search keyword, filters, sorting and pagination all in one call. */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Expense.PaymentMethod paymentMethod,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "expenseDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        ExpenseSearchCriteria criteria = ExpenseSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .paymentMethod(paymentMethod)
                .startDate(startDate)
                .endDate(endDate)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(ApiResponse.success(expenseService.search(principal.getUserId(), criteria)));
    }
}
