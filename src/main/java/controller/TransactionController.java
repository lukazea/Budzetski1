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

    // Filtriraj transakcije po kategoriji
    @GetMapping("/wallet/{walletId}/category/{categoryId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByCategory(
            @PathVariable Long walletId,
            @PathVariable Long categoryId) {
        Category category = categoryService.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
        List<Transaction> transactions = transactionService.getTransactionsByCategory(walletId, category);
        return ResponseEntity.ok(
            transactions.stream().map(TransactionDto::new).collect(Collectors.toList())
        );
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
}
