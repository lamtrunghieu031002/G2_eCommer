package com.project.productservice.services.category;

import com.project.productservice.dtos.category.CategoryDTO;
import com.project.productservice.exceptions.DataNotFoundException;
import com.project.productservice.models.Category;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(long id);
    List<Category> getAllCategories();
    Category updateCategory(long categoryId, CategoryDTO category);
    void deleteCategory(long id);
}
