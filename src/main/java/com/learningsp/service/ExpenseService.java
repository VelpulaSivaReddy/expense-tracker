package com.learningsp.service;

import com.learningsp.dto.common.PageResponse;
import com.learningsp.dto.expense.*;
import com.learningsp.entity.Category;
import com.learningsp.entity.Expense;
import com.learningsp.exception.ResourceNotFoundException;
import com.learningsp.repo.CategoryRepository;
import com.learningsp.repo.ExpenseRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ExpenseResponse create(Long userId, ExpenseCreateRequest request) {
        Category category = resolveCategory(userId, request.getCategoryId());

        boolean duplicate = expenseRepository.existsPossibleDuplicate(
                userId, request.getTitle(), request.getAmount(), request.getExpenseDate(), -1L);

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(category)
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .notes(request.getNotes())
                .expenseDate(request.getExpenseDate())
                .userId(userId)
                .build();

        Expense saved = expenseRepository.save(expense);
        ExpenseResponse response = toResponse(saved);
        response.setNotes(duplicate ? appendDuplicateWarning(response.getNotes()) : response.getNotes());
        return response;
    }

    @Transactional
    public ExpenseResponse update(Long userId, Long expenseId, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Category category = resolveCategory(userId, request.getCategoryId());

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());
        expense.setExpenseDate(request.getExpenseDate());

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public ExpenseResponse getById(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return toResponse(expense);
    }

    /** Filters + search + pagination + sorting all live here via a JPA Specification. */
    public PageResponse<ExpenseResponse> search(Long userId, ExpenseSearchCriteria criteria) {
        Specification<Expense> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                String like = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), criteria.getCategoryId()));
            }
            if (criteria.getPaymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), criteria.getPaymentMethod()));
            }
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), criteria.getEndDate()));
            }
            if (criteria.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), criteria.getMinAmount()));
            }
            if (criteria.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), criteria.getMaxAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(criteria.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                resolveSortProperty(criteria.getSortBy())
        );

        Pageable pageable = PageRequest.of(
                Math.max(criteria.getPage(), 0),
                criteria.getSize() != null && criteria.getSize() > 0 ? criteria.getSize() : 10,
                sort
        );

        Page<Expense> page = expenseRepository.findAll(spec, pageable);

        return PageResponse.<ExpenseResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /** Frontend sends simple field names; "categoryId" needs to map to the nested association path. */
    private String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return "expenseDate";
        return switch (sortBy) {
            case "categoryId" -> "category.categoryId";
            case "title", "amount", "expenseDate", "paymentMethod", "createdAt" -> sortBy;
            default -> "expenseDate";
        };
    }

    public BigDecimal sumAll(Long userId) {
        return expenseRepository.sumAllByUser(userId);
    }

    public BigDecimal sumRange(Long userId, LocalDate start, LocalDate end) {
        return expenseRepository.sumByUserAndDateRange(userId, start, end);
    }

    public List<ExpenseResponse> recent(Long userId, int limit) {
        return expenseRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    public List<Expense> allForUser(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
    }

    private Category resolveCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean accessible = category.getUserId() == null || category.getUserId().equals(userId);
        if (!accessible) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private String appendDuplicateWarning(String notes) {
        String warning = "[Possible duplicate expense detected]";
        return (notes == null || notes.isBlank()) ? warning : notes + " " + warning;
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .categoryId(expense.getCategory().getCategoryId())
                .categoryName(expense.getCategory().getCategoryName())
                .categoryColor(expense.getCategory().getColor())
                .paymentMethod(expense.getPaymentMethod())
                .description(expense.getDescription())
                .notes(expense.getNotes())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
