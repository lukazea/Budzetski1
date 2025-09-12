package service;

import entity.Transaction;
import entity.Wallet;
import entity.Category;
import entity.CategoryType;
import repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private CurrencyService currencyService;

    // Admin funkcionalnosti - postojeće
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllOrderByTransactionDateDesc(pageable);
    }

    // NOVA METODA - Transakcije specifičnog korisnika
    public Page<Transaction> getTransactionsByUser(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
    }

    // NOVA METODA - Broj transakcija korisnika
    public Long countTransactionsByUser(Long userId) {
        return transactionRepository.countByUserId(userId);
    }
}
