package com.learningsp.repo;

import com.learningsp.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByExpenseIdAndUserId(Long expenseId, Long userId);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId, LocalDate start, LocalDate end);

    Page<Expense> findByUserIdOrderByExpenseDateDescCreatedAtDesc(Long userId, Pageable pageable);

    List<Expense> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId")
    BigDecimal sumAllByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT FUNCTION('DATE_FORMAT', e.expenseDate, '%Y-%m') as ym, COALESCE(SUM(e.amount),0) as total " +
           "FROM Expense e WHERE e.userId = :userId GROUP BY ym ORDER BY ym ASC")
    List<Object[]> sumGroupedByMonth(@Param("userId") Long userId);

    @Query("SELECT e.category.categoryId, e.category.categoryName, e.category.color, COALESCE(SUM(e.amount),0) " +
           "FROM Expense e WHERE e.userId = :userId AND e.expenseDate BETWEEN :start AND :end " +
           "GROUP BY e.category.categoryId, e.category.categoryName, e.category.color " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumGroupedByCategory(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.userId = :userId AND e.title = :title " +
           "AND e.amount = :amount AND e.expenseDate = :date AND e.expenseId <> :excludeId")
    boolean existsPossibleDuplicate(@Param("userId") Long userId, @Param("title") String title,
                                     @Param("amount") BigDecimal amount, @Param("date") LocalDate date,
                                     @Param("excludeId") Long excludeId);

    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    long countByUserIdAndCategory_CategoryId(Long userId, Long categoryId);
}
