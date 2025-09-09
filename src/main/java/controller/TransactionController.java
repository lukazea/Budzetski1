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

    // Kreiranje nove transakcije
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody CreateTransactionRequest request) {
        try {
            Transaction transaction = new Transaction();
            transaction.setName(request.getName());
            transaction.setAmount(request.getAmount());
            transaction.setType(request.getType());
            transaction.setTransactionDate(request.getTransactionDate());
            
            // Dodeli entitete na osnovu ID-jeva
            if (request.getCategoryId() != null) {
                Category category = categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
                transaction.setCategory(category);
            }
            
            Transaction savedTransaction = transactionService.createTransaction(transaction);
            return ResponseEntity.ok(new TransactionDto(savedTransaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Transfer između novčanika
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(@RequestBody TransferRequest request) {
        try {
            Transaction transfer = transactionService.createTransfer(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                request.getUserId()
            );
            return ResponseEntity.ok(new TransactionDto(transfer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi transakcije novčanika
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionDto>> getWalletTransactions(@PathVariable Long walletId) {
        try {
            List<Transaction> transactions = transactionService.getWalletTransactions(walletId);
            List<TransactionDto> transactionDtos = transactions.stream()
                .map(TransactionDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(transactionDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi transakcije korisnika sa paginacijom
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionDto>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactions = transactionService.getUserTransactions(userId, pageable);
            Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
            return ResponseEntity.ok(transactionDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Filtriraj transakcije po datumu
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                userId, startDate, endDate);
            List<TransactionDto> transactionDtos = transactions.stream()
                .map(TransactionDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(transactionDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Filtriraj transakcije po kategoriji
    @GetMapping("/wallet/{walletId}/category/{categoryId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByCategory(
            @PathVariable Long walletId,
            @PathVariable Long categoryId) {
        try {
            Category category = categoryService.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
            List<Transaction> transactions = transactionService.getTransactionsByCategory(walletId, category);
            List<TransactionDto> transactionDtos = transactions.stream()
                .map(TransactionDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(transactionDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Filtriraj transakcije po tipu
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByType(
            @PathVariable Long userId,
            @PathVariable CategoryType type) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByType(userId, type);
            List<TransactionDto> transactionDtos = transactions.stream()
                .map(TransactionDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(transactionDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ažuriranje transakcije
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long transactionId,
            @RequestBody UpdateTransactionRequest request) {
        try {
            Transaction updatedTransaction = new Transaction();
            updatedTransaction.setName(request.getName());
            updatedTransaction.setAmount(request.getAmount());
            updatedTransaction.setType(request.getType());
            updatedTransaction.setTransactionDate(request.getTransactionDate());
            
            if (request.getCategoryId() != null) {
                Category category = categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
                updatedTransaction.setCategory(category);
            }
            
            Transaction transaction = transactionService.updateTransaction(transactionId, updatedTransaction);
            return ResponseEntity.ok(new TransactionDto(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Brisanje transakcije
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        try {
            transactionService.deleteTransaction(transactionId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Statistike - ukupni prihodi za period
    @GetMapping("/user/{userId}/stats/income")
    public ResponseEntity<BigDecimal> getTotalIncomeForPeriod(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        BigDecimal totalIncome = transactionService.getTotalIncomeForPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(totalIncome);
    }

    // Statistike - ukupni troškovi za period
    @GetMapping("/user/{userId}/stats/expense")
    public ResponseEntity<BigDecimal> getTotalExpenseForPeriod(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        BigDecimal totalExpense = transactionService.getTotalExpenseForPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(totalExpense);
    }

    // Top transakcije
    @GetMapping("/top")
    public ResponseEntity<List<TransactionDto>> getTopTransactions(
            @RequestParam LocalDate since,
            @RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionService.getTopTransactions(since, limit);
        List<TransactionDto> transactionDtos = transactions.stream()
            .map(TransactionDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    // === ADMIN ENDPOINTS ===
    
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

    // DTO klase za request body
    public static class CreateTransactionRequest {
        private String name;
        private BigDecimal amount;
        private CategoryType type;
        private Long categoryId;
        private Long walletId;
        private Long userId;
        private LocalDate transactionDate;

        // Getteri i Setteri
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public CategoryType getType() { return type; }
        public void setType(CategoryType type) { this.type = type; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getWalletId() { return walletId; }
        public void setWalletId(Long walletId) { this.walletId = walletId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    }

    public static class UpdateTransactionRequest {
        private String name;
        private BigDecimal amount;
        private CategoryType type;
        private Long categoryId;
        private LocalDate transactionDate;

        // Getteri i Setteri
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public CategoryType getType() { return type; }
        public void setType(CategoryType type) { this.type = type; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    }

    public static class TransferRequest {
        private Long fromWalletId;
        private Long toWalletId;
        private BigDecimal amount;
        private Long userId;

        // Getteri i Setteri
        public Long getFromWalletId() { return fromWalletId; }
        public void setFromWalletId(Long fromWalletId) { this.fromWalletId = fromWalletId; }
        public Long getToWalletId() { return toWalletId; }
        public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}