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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // ----- Osnovne metode -----
    public Optional<Category> findByIdOptional(Long id) {
        return categoryRepository.findById(id);
    }

    public CategoryDto findById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
        return convertToDto(category);
    }

    // ----- Kreiranje kategorija -----
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

    // ----- Ažuriranje i brisanje -----
    public Category updateCategory(Long categoryId, String newName) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));

        if (category.isPredefined()) {
            throw new RuntimeException("Predefinisane kategorije ne mogu biti izmenjene!");
        }

        category.setName(newName);
        return categoryRepository.save(category);
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

    // ----- Dohvatanje kategorija -----
    public List<CategoryDto> getAvailableCategories(Long userId) {
        List<Category> categories = categoryRepository.findAvailableForUser(userId);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CategoryDto> getCategoriesByType(Long userId, CategoryType type) {
        List<Category> categories = categoryRepository.findByTypeAndAvailableForUser(type, userId);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CategoryDto> getUserCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        List<Category> categories = categoryRepository.findByUserAndPredefinedFalse(user);
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean isAvailableForUser(Long categoryId, Long userId) {
        return categoryRepository.isAvailableForUser(categoryId, userId);
    }

    // ----- Konverzija u DTO -----
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