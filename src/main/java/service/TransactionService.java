package service;

import entity.Transaction;
import repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;

    // Sve transakcije sa paginacijom
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    // Filtrirane transakcije
    public Page<Transaction> getFilteredTransactions(
            Long userId, 
            Long categoryId, 
            BigDecimal minAmount, 
            BigDecimal maxAmount, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable) {
        
        return transactionRepository.findFilteredTransactions(
            userId, categoryId, minAmount, maxAmount, startDate, endDate, pageable);
    }

    // Transakcije po korisniku
    public Page<Transaction> getTransactionsByUser(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    // Transakcije po kategoriji
    public Page<Transaction> getTransactionsByCategory(Long categoryId, Pageable pageable) {
        return transactionRepository.findByCategoryId(categoryId, pageable);
    }

    // Transakcije u opsegu suma
    public Page<Transaction> getTransactionsByAmountRange(
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount, pageable);
    }

    // Transakcije u datumskom opsegu
    public Page<Transaction> getTransactionsByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return transactionRepository.findByDateBetween(startDate, endDate, pageable);
    }

    // Transakcija po ID
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    // Sve transakcije bez paginacije (za export ili statistike)
    public List<Transaction> getAllTransactionsList() {
        return transactionRepository.findAll();
    }

    // Broj transakcija po korisniku
    public long countTransactionsByUser(Long userId) {
        return transactionRepository.countByUserId(userId);
    }

    // Ukupna suma transakcija po korisniku
    public BigDecimal getTotalAmountByUser(Long userId) {
        BigDecimal total = transactionRepository.sumAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }
}
