package com.teamfour.smartexpense.repository;

import com.teamfour.smartexpense.model.Category;
import com.teamfour.smartexpense.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrUserIsNull(Long userId);
    List<Category> findByType(CategoryType type);
}
