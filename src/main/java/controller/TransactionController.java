package controller;

import dto.TransactionDto;
import entity.Transaction;
import entity.CategoryType;
import entity.Category;
import service.TransactionService;
import service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private CategoryService categoryService;

    // Sve transakcije sa paginacijom (admin)
    @GetMapping("/admin/all")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // Filtrirane i sortirane transakcije (admin)
    @GetMapping("/admin/filtered")
    public ResponseEntity<Page<TransactionDto>> getFilteredTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Validacija sortBy parametra
        String validSortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionService.getFilteredTransactions(
            userId, categoryId, minAmount, maxAmount, startDate, endDate, pageable);
        
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // Transakcije po korisniku (admin)
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        String validSortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionService.getTransactionsByUser(userId, pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // Transakcije po kategoriji (admin)
    @GetMapping("/admin/category/{categoryId}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        String validSortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionService.getTransactionsByCategory(categoryId, pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    private String validateSortBy(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "date":
                return "date";
            case "amount":
            case "value":
                return "amount";
            case "description":
                return "description";
            default:
                return "date";
        }
    }
}
