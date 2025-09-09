package service;

import entity.Category;
import entity.CategoryType;
import entity.User;
import repository.CategoryRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;
    
	@Autowired
	private UserRepository userRepository;

    // Admin funkcionalnosti
    public List<Category> getAllCategories() {
        return categoryRepository.findByOrderByNameAsc();
    }

    public List<Category> getPredefinedCategories() {
        return categoryRepository.findByPredefinedTrue();
    }

    public Category createPredefinedCategory(String name, CategoryType type) {
        if (categoryRepository.existsByNameAndPredefinedTrue(name)) {
            throw new RuntimeException("Predefinisana kategorija sa ovim imenom već postoji!");
        }
        
        Category category = new Category(name, type, true, null);
        return categoryRepository.save(category);
    }
}