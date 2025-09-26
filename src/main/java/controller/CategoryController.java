package controller;

import dto.CategoryDto;
import entity.Category;
import entity.CategoryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // --- Kreiranje korisničke kategorije ---
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

    // --- Preuzmi sve dostupne kategorije za korisnika (predefinisane + korisničke) ---
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryDto>> getAvailableCategories(@PathVariable Long userId) {
        try {
            List<Category> categories = categoryService.getAvailableCategories(userId);
            List<CategoryDto> categoryDtos = categories.stream()
                    .map(CategoryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- Preuzmi kategorije po tipu ---
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @PathVariable Long userId,
            @PathVariable CategoryType type) {
        try {
            List<Category> categories = categoryService.getCategoriesByType(userId, type);
            List<CategoryDto> categoryDtos = categories.stream()
                    .map(CategoryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- Preuzmi samo korisničke kategorije ---
    @GetMapping("/user/{userId}/custom")
    public ResponseEntity<List<CategoryDto>> getUserCategories(@PathVariable Long userId) {
        try {
            List<Category> categories = categoryService.getUserCategories(userId);
            List<CategoryDto> categoryDtos = categories.stream()
                    .map(CategoryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(categoryDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- Ažuriranje kategorije ---
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

    // --- Brisanje kategorije ---
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- Preuzmi kategoriju po ID ---
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        return categoryService.findById(categoryId)
                .map(category -> ResponseEntity.ok(new CategoryDto(category)))
                .orElse(ResponseEntity.notFound().build());
    }
}
