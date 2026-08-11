package com.learningsp.repo;

import com.learningsp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.userId IS NULL OR c.userId = :userId ORDER BY c.categoryName ASC")
    List<Category> findAllVisibleToUser(@Param("userId") Long userId);

    Optional<Category> findByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByCategoryNameIgnoreCaseAndUserId(String categoryName, Long userId);
}
