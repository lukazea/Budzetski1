package controller;

import dto.TransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.TransactionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Kreiranje obične transakcije
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionDto transactionDto) {
        try {
            TransactionDto created = transactionService.createTransaction(transactionDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Kreiranje transfer transakcije
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> createTransfer(@RequestBody TransactionDto transferDto) {
        try {
            TransactionDto created = transactionService.createTransfer(transferDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Pregled svih transakcija korisnika
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDto>> getUserTransactions(@PathVariable Long userId) {
        try {
            List<TransactionDto> transactions = transactionService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Pregled transakcija po novčaniku
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

    // Pregled po danima
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

    // Pregled po nedeljama
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

    // Pregled po mesecima
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

    // Pregled po kvartalima
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

    // Paginacija - osnovni pregled
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

    // Paginacija sa filterom po datumu
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

    // Ažuriranje transakcije
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

    // Brisanje transakcije
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
}