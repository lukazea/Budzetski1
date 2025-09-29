package controller;

import dto.StatisticsDto;
import dto.TopTransactionsDto;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")

public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    // ==================== ADMIN ENDPOINTS ====================

    // Admin: sve transakcije sa paginacijom
    @GetMapping("/admin/all")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // Admin: filtrirane i sortirane transakcije
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

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String validSortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactions = transactionService.getFilteredTransactions(
                userId, categoryId, minAmount, maxAmount, startDate, endDate, pageable);

        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // Admin: transakcije po korisniku
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<Page<TransactionDto>> getAdminTransactionsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String validSortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactions = transactionService.getTransactionsByUser(userId, pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // Admin: transakcije po kategoriji
    @GetMapping("/admin/category/{categoryId}")
    public ResponseEntity<Page<TransactionDto>> getAdminTransactionsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String validSortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactions = transactionService.getTransactionsByCategory(categoryId, pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
        return ResponseEntity.ok(transactionDtos);
    }

    // ==================== CRUD OPERATIONS ====================

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionDto transactionDto) {
        try {
            TransactionDto created = transactionService.createTransaction(transactionDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(@RequestBody TransactionDto transferDto) {
        try {
            TransactionDto created = transactionService.createTransfer(transferDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long transactionId,
            @RequestBody TransactionDto transactionDto,
            @RequestParam Long userId) {
        try {
            TransactionDto updated = transactionService.updateTransaction(transactionId, transactionDto, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long transactionId,
            @RequestParam Long userId) {
        try {
            transactionService.deleteTransaction(transactionId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== USER TRANSACTION QUERIES ====================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDto>> getUserTransactions(@PathVariable Long userId) {
        try {
            List<TransactionDto> transactions = transactionService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionDto>> getWalletTransactions(
            @PathVariable Long walletId,
            @RequestParam Long userId) {
        try {
            List<TransactionDto> transactions = transactionService.getWalletTransactions(walletId, userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==================== FILTERING ENDPOINTS ====================

    // Filtriraj transakcije po datumu
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(
            transactions.stream().map(TransactionDto::new).collect(Collectors.toList())
        );
    }

    // Filtriraj transakcije po kategoriji (user endpoint)
    @GetMapping("/wallet/{walletId}/category/{categoryId}/user/{userId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByCategory(
            @PathVariable Long walletId,
            @PathVariable Long categoryId,
            @PathVariable Long userId) {  // <-- sad imamo userId

        Category category = categoryService.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));

        List<Transaction> transactions = transactionService.getTransactionsByCategory(walletId, userId, category);

        List<TransactionDto> transactionDtos = transactions.stream()
                .map(TransactionDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDtos);
    }

    // Filtriraj transakcije po tipu
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByType(
            @PathVariable Long userId,
            @PathVariable CategoryType type) {
        List<Transaction> transactions = transactionService.getTransactionsByType(userId, type);
        return ResponseEntity.ok(
            transactions.stream().map(TransactionDto::new).collect(Collectors.toList())
        );
    }

    // ==================== TIME-BASED QUERIES ====================

    @GetMapping("/daily")
    public ResponseEntity<List<TransactionDto>> getDailyTransactions(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TransactionDto> transactions = transactionService.getDailyTransactions(userId, date);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<TransactionDto>> getWeeklyTransactions(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TransactionDto> transactions = transactionService.getWeeklyTransactions(userId, date);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<TransactionDto>> getMonthlyTransactions(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            List<TransactionDto> transactions = transactionService.getMonthlyTransactions(userId, year, month);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/quarterly")
    public ResponseEntity<List<TransactionDto>> getQuarterlyTransactions(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int quarter) {
        try {
            List<TransactionDto> transactions = transactionService.getQuarterlyTransactions(userId, year, quarter);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==================== PAGINATION ENDPOINTS ====================

    @GetMapping("/paginated")
    public ResponseEntity<Page<TransactionDto>> getTransactionsPaginated(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionDto> transactions = transactionService.getTransactionsPaginated(userId, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/paginated/date-range")
    public ResponseEntity<Page<TransactionDto>> getTransactionsPaginatedByDate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionDto> transactions = transactionService
                    .getTransactionsPaginatedByDate(userId, startDate, endDate, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==================== STATISTICS ENDPOINTS ====================

    // Statistike - ukupni prihodi za period
    @GetMapping("/user/{userId}/stats/income")
    public ResponseEntity<BigDecimal> getTotalIncomeForPeriod(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTotalIncomeForPeriod(userId, startDate, endDate));
    }

    // Statistike - ukupni troškovi za period
    @GetMapping("/user/{userId}/stats/expense")
    public ResponseEntity<BigDecimal> getTotalExpenseForPeriod(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTotalExpenseForPeriod(userId, startDate, endDate));
    }

    // Top transakcije
    @GetMapping("/top")
    public ResponseEntity<List<TransactionDto>> getTopTransactions(
            @RequestParam LocalDate since,
            @RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionService.getTopTransactions(since, limit);
        return ResponseEntity.ok(
            transactions.stream().map(TransactionDto::new).collect(Collectors.toList())
        );
    }

    // Kompletna statistika za period
    @GetMapping("/user/{userId}/stats/complete")
    public ResponseEntity<StatisticsDto> getCompleteStatistics(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getCompleteStatistics(userId, startDate, endDate));
    }

    // Statistike sa periodima (daily, weekly, monthly, yearly)
    @GetMapping("/user/{userId}/stats/periods")
    public ResponseEntity<StatisticsDto> getStatisticsWithPeriods(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String periodType) {
        return ResponseEntity.ok(transactionService.getStatisticsWithPeriods(userId, startDate, endDate, periodType));
    }
    
    @GetMapping("/user/{userId}/stats/top")
    public ResponseEntity<TopTransactionsDto> getTopTransactionsForUser(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transactionService.getTopTransactionsForUser(userId, startDate, endDate, limit));
    }

    // ==================== UTILITY METHODS ====================

    // Validacija sort parametra
    private String validateSortBy(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "date": return "date";
            case "amount":
            case "value": return "amount";
            case "description": return "description";
            default: return "date";
        }
    }
}