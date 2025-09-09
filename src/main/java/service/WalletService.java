package service;

import entity.Wallet;
import entity.User;
import entity.Currency;
import repository.WalletRepository;
import repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private CurrencyRepository currencyRepository;

    // Kreiranje novog novčanika
    public Wallet createWallet(Wallet wallet, Long userId, String currencyCode) {
        // Pronađi valute
        Currency currency = currencyRepository.findByCurrency(currencyCode)
            .orElseThrow(() -> new RuntimeException("Valuta nije pronađena!"));
        
        wallet.setCreationDate(LocalDate.now());
        wallet.setCurrentBalance(wallet.getInitialBalance());
        wallet.setArchived(false);
        
        // Dodeli valute novčaniku
        wallet.getCurrencies().add(currency);
        
        return walletRepository.save(wallet);
    }

    // Pronađi sve novčanike korisnika
    public List<Wallet> getUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedFalse(userId);
    }

    // Pronađi sve aktivne novčanike korisnika
    public List<Wallet> getActiveUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedFalse(userId);
    }

    // Pronađi arhivirane novčanike
    public List<Wallet> getArchivedUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedTrue(userId);
    }

    // Pronađi štedne novčanike
    public List<Wallet> getSavingsWallets(Long userId) {
        return walletRepository.findByUserIdAndSavingsTrue(userId);
    }

    // Ažuriranje novčanika
    public Wallet updateWallet(Long walletId, Wallet updatedWallet) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        
        wallet.setName(updatedWallet.getName());
        wallet.setSavings(updatedWallet.isSavings());
        
        return walletRepository.save(wallet);
    }

    // Arhiviranje novčanika
    public void archiveWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        wallet.setArchived(true);
        walletRepository.save(wallet);
    }

    // Aktiviranje novčanika
    public void activateWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        wallet.setArchived(false);
        walletRepository.save(wallet);
    }

    // Brisanje novčanika
    public void deleteWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        
        // Proveri da li ima transakcije
        if (!wallet.getTransactions().isEmpty()) {
            throw new RuntimeException("Ne možete obrisati novčanik koji ima transakcije!");
        }
        
        walletRepository.delete(wallet);
    }

    // Ažuriranje balansa
    public void updateBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        
        wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        walletRepository.save(wallet);
    }

    // Ukupno stanje korisnika
    public BigDecimal getTotalUserBalance(Long userId) {
        BigDecimal total = walletRepository.getTotalBalanceByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Admin funkcionalnosti
    public BigDecimal getTotalSystemBalance() {
        BigDecimal total = walletRepository.getTotalSystemBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getAverageUserBalance() {
        BigDecimal avg = walletRepository.getAverageUserBalance();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    // Pronađi novčanik po ID
    public Optional<Wallet> findById(Long walletId) {
        return walletRepository.findById(walletId);
    }
}
