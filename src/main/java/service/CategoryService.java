package service;

import dto.CategoryDto;
import entity.Category;
import entity.CategoryType;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CategoryRepository;
import repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // Pronađi sve dostupne kategorije za korisnika
    public List<CategoryDto> getAvailableCategories(Long userId) {
        List<Category> categories = categoryRepository.findAvailableForUser(userId);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pronađi kategorije po tipu
    public List<CategoryDto> getCategoriesByType(Long userId, CategoryType type) {
        List<Category> categories = categoryRepository.findByUserAndType(userId, type);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pronađi kategoriju po ID-ju
    public CategoryDto findById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
        return convertToDto(category);
    }

    // Proveri da li korisnik može koristiti kategoriju
    public boolean isAvailableForUser(Long categoryId, Long userId) {
        return categoryRepository.isAvailableForUser(categoryId, userId);
    }

    // Konvertovanje entiteta u DTO
    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setPredefined(category.isPredefined());
        dto.setUserId(category.getUser() != null ? category.getUser().getId() : null);
        return dto;
    }
}