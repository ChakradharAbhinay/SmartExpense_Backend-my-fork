package com.teamfour.smartexpense.service;

import com.teamfour.smartexpense.dto.CategoryDTO;
import com.teamfour.smartexpense.model.Category;
import com.teamfour.smartexpense.model.CategoryType;
import com.teamfour.smartexpense.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(this::convertToDTO).orElse(null);
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            category.setName(categoryDTO.getName());
            category.setType(CategoryType.valueOf(categoryDTO.getType()));
            category.setIcon(categoryDTO.getIcon());
            category.setColor(categoryDTO.getColor());
            //category.setUserId(categoryDTO.getUserId());
            Category updatedCategory = categoryRepository.save(category);
            return convertToDTO(updatedCategory);
        }
        return null;
    }

    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(String.valueOf(category.getType()));
        dto.setIcon(category.getIcon());
        dto.setColor(category.getColor());
        //dto.setUserId(category.getUserId());
        return dto;
    }

    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setType(CategoryType.valueOf(dto.getType()));
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        //category.setUserId(dto.getUserId());
        return category;
    }
}