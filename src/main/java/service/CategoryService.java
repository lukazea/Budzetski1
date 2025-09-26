package service;

import dto.CategoryDto;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== ADMIN FUNCTIONALITY ====================

    // Admin funkcionalnosti - sve kategorije
    public List<Category> getAllCategories() {
        return categoryRepository.findByOrderByNameAsc();
    }

    // Admin - samo predefinisane kategorije
    public List<Category> getPredefinedCategories() {
        return categoryRepository.findByPredefinedTrue();
    }

    // Admin - kreiranje predefinisanih kategorija
    public Category createPredefinedCategory(String name, CategoryType type) {
        if (categoryRepository.existsByNameAndPredefinedTrue(name)) {
            throw new RuntimeException("Predefinisana kategorija sa ovim imenom već postoji!");
        }
        
        Category category = new Category(name, type, true, null);
        return categoryRepository.save(category);
    }

    // ==================== BASIC METHODS ====================

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findByIdOptional(Long id) {
        return categoryRepository.findById(id);
    }

    public CategoryDto findByIdDto(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
        return convertToDto(category);
    }

    // ==================== CATEGORY CREATION ====================

    // Kreiranje predefinisanih kategorija za novog korisnika
    public void createDefaultCategories(User user) {
        createCategoryIfNotExists("Plata", CategoryType.PRIHOD, true, null);
        createCategoryIfNotExists("Bonus", CategoryType.PRIHOD, true, null);
        createCategoryIfNotExists("Investicije", CategoryType.PRIHOD, true, null);

        createCategoryIfNotExists("Hrana", CategoryType.TROSAK, true, null);
        createCategoryIfNotExists("Zabava", CategoryType.TROSAK, true, null);
        createCategoryIfNotExists("Domaćinstvo", CategoryType.TROSAK, true, null);
        createCategoryIfNotExists("Transport", CategoryType.TROSAK, true, null);
        createCategoryIfNotExists("Režije", CategoryType.TROSAK, true, null);
    }

    private void createCategoryIfNotExists(String name, CategoryType type, boolean predefined, User user) {
        boolean exists = predefined ?
                categoryRepository.existsByNameAndPredefinedTrue(name) :
                categoryRepository.existsByNameAndUser(name, user);

        if (!exists) {
            Category category = new Category(name, type, predefined, user);
            categoryRepository.save(category);
        }
    }

    // Kreiranje custom korisničke kategorije
    public Category createUserCategory(String name, CategoryType type, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));

        if (categoryRepository.existsByNameAndUser(name, user)) {
            throw new RuntimeException("Kategorija sa ovim imenom već postoji!");
        }

        Category category = new Category(name, type, false, user);
        return categoryRepository.save(category);
    }

    // Kreiranje custom kategorije sa DTO
    public CategoryDto createUserCategoryDto(String name, CategoryType type, Long userId) {
        Category category = createUserCategory(name, type, userId);
        return convertToDto(category);
    }

    // ==================== UPDATE AND DELETE ====================

    public Category updateCategory(Long categoryId, String newName) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));

        if (category.isPredefined()) {
            throw new RuntimeException("Predefinisane kategorije ne mogu biti izmenjene!");
        }

        category.setName(newName);
        return categoryRepository.save(category);
    }

    public CategoryDto updateCategoryDto(Long categoryId, String newName) {
        Category category = updateCategory(categoryId, newName);
        return convertToDto(category);
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));

        if (category.isPredefined()) {
            throw new RuntimeException("Predefinisane kategorije ne mogu biti obrisane!");
        }

        if (!category.getTransactions().isEmpty()) {
            throw new RuntimeException("Ne možete obrisati kategoriju koja ima transakcije!");
        }

        categoryRepository.delete(category);
    }

    // ==================== CATEGORY RETRIEVAL ====================

    // Sve dostupne kategorije za korisnika (predefinisane + korisničke)
    public List<CategoryDto> getAvailableCategories(Long userId) {
        List<Category> categories = categoryRepository.findAvailableForUser(userId);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Kategorije po tipu za korisnika
    public List<CategoryDto> getCategoriesByType(Long userId, CategoryType type) {
        List<Category> categories = categoryRepository.findByTypeAndAvailableForUser(type, userId);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Samo korisničke kategorije (ne predefinisane)
    public List<CategoryDto> getUserCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        List<Category> categories = categoryRepository.findByUserAndPredefinedFalse(user);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Samo predefinisane kategorije kao DTO
    public List<CategoryDto> getPredefinedCategoriesDto() {
        List<Category> categories = getPredefinedCategories();
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Sve kategorije kao DTO (za admin)
    public List<CategoryDto> getAllCategoriesDto() {
        List<Category> categories = getAllCategories();
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION METHODS ====================

    public boolean isAvailableForUser(Long categoryId, Long userId) {
        return categoryRepository.isAvailableForUser(categoryId, userId);
    }

    public boolean categoryExists(String name, Long userId) {
        if (categoryRepository.existsByNameAndPredefinedTrue(name)) {
            return true;
        }
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && categoryRepository.existsByNameAndUser(name, user)) {
                return true;
            }
        }
        
        return false;
    }

    // ==================== UTILITY METHODS ====================

    // Konverzija u DTO
    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setPredefined(category.isPredefined());
        dto.setUserId(category.getUser() != null ? category.getUser().getId() : null);
        return dto;
    }

    // Konverzija iz DTO u Entity
    public Category convertFromDto(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setPredefined(dto.isPredefined());
        
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
            category.setUser(user);
        }
        
        return category;
    }

    // Bulk operacije
    public void createMultipleCategories(List<CategoryDto> categoryDtos, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));

        for (CategoryDto dto : categoryDtos) {
            if (!categoryRepository.existsByNameAndUser(dto.getName(), user)) {
                Category category = new Category(dto.getName(), dto.getType(), false, user);
                categoryRepository.save(category);
            }
        }
    }

    // Pretraživanje kategorija po imenu
    public List<CategoryDto> searchCategories(Long userId, String searchTerm) {
        List<Category> categories = categoryRepository.findAvailableForUserByNameContaining(userId, searchTerm);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}