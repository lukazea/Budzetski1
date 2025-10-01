package controller;

import dto.CategoryDto;
import entity.Category;
import entity.CategoryType;
import service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ==================== ADMIN ENDPOINTS ====================

    // Sve kategorije (admin)
    @GetMapping("/admin/all")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> categoryDtos = categories.stream()
            .map(CategoryDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    // Predefinisane kategorije (admin)
    @GetMapping("/admin/predefined")
    public ResponseEntity<List<CategoryDto>> getPredefinedCategories() {
        List<Category> categories = categoryService.getPredefinedCategories();
        List<CategoryDto> categoryDtos = categories.stream()
            .map(CategoryDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    // Kreiranje predefinisane kategorije (admin)
    @PostMapping("/admin/predefined")
    public ResponseEntity<CategoryDto> createPredefinedCategory(@RequestBody CategoryDto categoryDto) {
        try {
            Category category = categoryService.createPredefinedCategory(
                categoryDto.getName(), 
                categoryDto.getType()
            );
            return ResponseEntity.ok(new CategoryDto(category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/admin/{categoryId}/activate")
    public ResponseEntity<CategoryDto> activatePredefined(@PathVariable Long categoryId) {
        try {
            Category c = categoryService.activatePredefined(categoryId);
            return ResponseEntity.ok(new CategoryDto(c));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Deaktiviraj predefinisanu (ADMIN)
    @PatchMapping("/admin/{categoryId}/deactivate")
    public ResponseEntity<CategoryDto> deactivatePredefined(@PathVariable Long categoryId) {
        try {
            Category c = categoryService.deactivatePredefined(categoryId);
            return ResponseEntity.ok(new CategoryDto(c));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== USER ENDPOINTS ====================

    // Kreiranje korisničke kategorije
    @PostMapping("/user/{userId}")
    public ResponseEntity<CategoryDto> createUserCategory(
            @PathVariable Long userId,
            @RequestBody CategoryDto categoryDto) {
        try {
            Category category = categoryService.createUserCategory(
                    categoryDto.getName(),
                    categoryDto.getType(),
                    userId
            );
            return ResponseEntity.ok(new CategoryDto(category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi sve dostupne kategorije za korisnika (predefinisane + korisničke)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryDto>> getAvailableCategories(@PathVariable Long userId) {
        try {
            List<CategoryDto> categoryDtos = categoryService.getAvailableCategories(userId);
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi kategorije po tipu
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @PathVariable Long userId,
            @PathVariable CategoryType type) {
        try {
            List<CategoryDto> categoryDtos = categoryService.getCategoriesByType(userId, type);
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi samo korisničke kategorije
    @GetMapping("/user/{userId}/custom")
    public ResponseEntity<List<CategoryDto>> getUserCategories(@PathVariable Long userId) {
        try {
            List<CategoryDto> categoryDtos = categoryService.getUserCategories(userId);
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== CRUD OPERATIONS ====================

    // Preuzmi kategoriju po ID
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        return categoryService.findByIdOptional(categoryId)
                .map(category -> ResponseEntity.ok(new CategoryDto(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Ažuriranje kategorije
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryDto categoryDto) {
        try {
            Category category = categoryService.updateCategory(categoryId, categoryDto.getName());
            return ResponseEntity.ok(new CategoryDto(category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Brisanje kategorije
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok().build();
    }

    // ==================== ADDITIONAL ENDPOINTS ====================

    // Bulk kreiranje kategorija
    @PostMapping("/user/{userId}/bulk")
    public ResponseEntity<String> createMultipleCategories(
            @PathVariable Long userId,
            @RequestBody List<CategoryDto> categoryDtos) {
        try {
            categoryService.createMultipleCategories(categoryDtos, userId);
            return ResponseEntity.ok("Kategorije su uspešno kreirane");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Greška pri kreiranju kategorija: " + e.getMessage());
        }
    }

    // Pretraživanje kategorija po imenu
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<CategoryDto>> searchCategories(
            @PathVariable Long userId,
            @RequestParam String searchTerm) {
        try {
            List<CategoryDto> categoryDtos = categoryService.searchCategories(userId, searchTerm);
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Proveri da li je kategorija dostupna za korisnika
    @GetMapping("/{categoryId}/available/{userId}")
    public ResponseEntity<Boolean> isCategoryAvailableForUser(
            @PathVariable Long categoryId,
            @PathVariable Long userId) {
        try {
            boolean isAvailable = categoryService.isAvailableForUser(categoryId, userId);
            return ResponseEntity.ok(isAvailable);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    // Samo predefinisane kategorije (za javni pristup)
    @GetMapping("/predefined")
    public ResponseEntity<List<CategoryDto>> getPublicPredefinedCategories() {
        try {
            List<CategoryDto> categoryDtos = categoryService.getPredefinedCategoriesDto();
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}