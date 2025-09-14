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
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
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
}