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

    // Kreiranje nove transakcije
    public Transaction createTransaction(Transaction transaction) {
        // Validacija
        if (transaction.getWallet() == null) {
            throw new RuntimeException("Novčanik mora biti specificiran!");
        }
        
        // Postaviti datum ako nije postavljen
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }
        
        // Sačuvaj transakciju
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Ažuriraj balans novčanika
        BigDecimal amount = transaction.getAmount();
        if (transaction.getType() == CategoryType.TROSAK) {
            amount = amount.negate(); // Negativan iznos za trošak
        }
        
        walletService.updateBalance(transaction.getWallet().getId(), amount);
        
        return savedTransaction;
    }

    // Transfer između novčanika
    public Transaction createTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, Long userId) {
        Wallet fromWallet = walletService.findById(fromWalletId)
            .orElseThrow(() -> new RuntimeException("Izvorni novčanik nije pronađen!"));
        Wallet toWallet = walletService.findById(toWalletId)
            .orElseThrow(() -> new RuntimeException("Odredišni novčanik nije pronađen!"));
        
        // Proveri da li ima dovoljno sredstava
        if (fromWallet.getCurrentBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Nedovoljno sredstava na novčaniku!");
        }
        
        // Konverzija valuta ako je potrebno
        BigDecimal convertedAmount = currencyService.convertAmount(amount, fromWallet, toWallet);
        
        // Kreiranje transfer transakcije
        Transaction transfer = new Transaction();
        transfer.setName("Transfer: " + fromWallet.getName() + " -> " + toWallet.getName());
        transfer.setAmount(amount);
        transfer.setFromWallet(fromWallet);
        transfer.setToWallet(toWallet);
        transfer.setFromAmount(amount);
        transfer.setToAmount(convertedAmount);
        transfer.setTransactionDate(LocalDate.now());
        transfer.setTransfer(true);
        transfer.setWallet(fromWallet); // Glavna referenca
        
        // Sačuvaj transakciju
        Transaction savedTransfer = transactionRepository.save(transfer);
        
        // Ažuriraj balanse
        walletService.updateBalance(fromWalletId, amount.negate());
        walletService.updateBalance(toWalletId, convertedAmount);
        
        return savedTransfer;
    }

    // Pronađi transakcije po novčaniku
    public List<Transaction> getWalletTransactions(Long walletId) {
        Wallet wallet = walletService.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        return transactionRepository.findByWallet(wallet);
    }

    // Pronađi transakcije korisnika sa paginacijom
    public Page<Transaction> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    // Filtriraj transakcije po datumu
    public List<Transaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }

    // Filtriraj transakcije po kategoriji
    public List<Transaction> getTransactionsByCategory(Long walletId, Category category) {
        Wallet wallet = walletService.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        return transactionRepository.findByWalletAndCategory(wallet, category);
    }

    // Filtriraj transakcije po tipu
    public List<Transaction> getTransactionsByType(Long userId, CategoryType type) {
        return transactionRepository.findByUserIdAndType(userId, type);
    }

    // Ažuriranje transakcije
    public Transaction updateTransaction(Long transactionId, Transaction updatedTransaction) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transakcija nije pronađena!"));
        
        // Vraći stari iznos
        BigDecimal oldAmount = transaction.getAmount();
        if (transaction.getType() == CategoryType.TROSAK) {
            oldAmount = oldAmount.negate();
        }
        walletService.updateBalance(transaction.getWallet().getId(), oldAmount.negate());
        
        // Ažuriraj transakciju
        transaction.setName(updatedTransaction.getName());
        transaction.setAmount(updatedTransaction.getAmount());
        transaction.setType(updatedTransaction.getType());
        transaction.setCategory(updatedTransaction.getCategory());
        transaction.setTransactionDate(updatedTransaction.getTransactionDate());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Primeni novi iznos
        BigDecimal newAmount = updatedTransaction.getAmount();
        if (updatedTransaction.getType() == CategoryType.TROSAK) {
            newAmount = newAmount.negate();
        }
        walletService.updateBalance(transaction.getWallet().getId(), newAmount);
        
        return savedTransaction;
    }

    // Brisanje transakcije
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transakcija nije pronađena!"));
        
        // Vraći iznos u novčanik
        BigDecimal amount = transaction.getAmount();
        if (transaction.getType() == CategoryType.TROSAK) {
            amount = amount.negate();
        }
        walletService.updateBalance(transaction.getWallet().getId(), amount.negate());
        
        transactionRepository.delete(transaction);
    }

    // Statistike
    public BigDecimal getTotalIncomeForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.getSumByUserIdAndTypeAndDateBetween(
            userId, CategoryType.PRIHOD, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpenseForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.getSumByUserIdAndTypeAndDateBetween(
            userId, CategoryType.TROSAK, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Top transakcije
    public List<Transaction> getTopTransactions(LocalDate since, int limit) {
        return transactionRepository.findTopTransactionsSince(since, 
            org.springframework.data.domain.PageRequest.of(0, limit));
    }

    // Admin funkcionalnosti
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllOrderByTransactionDateDesc(pageable);
    }
}