package controller;

import dto.CategoryDto;
import entity.CategoryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Pronađi sve dostupne kategorije za korisnika
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryDto>> getUserCategories(@PathVariable Long userId) {
        try {
            List<CategoryDto> categories = categoryService.getAvailableCategories(userId);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Pronađi kategorije po tipu
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @PathVariable Long userId,
            @PathVariable CategoryType type) {
        try {
            List<CategoryDto> categories = categoryService.getCategoriesByType(userId, type);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Pronađi kategoriju po ID-ju
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        try {
            CategoryDto category = categoryService.findById(categoryId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}