package service;

import entity.Wallet;
import entity.User;
import entity.Currency;
import repository.WalletRepository;
import repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    // 2.1 Funkcionalnost: Dodavanje novčanika
    public Wallet createWallet(Wallet wallet, Long userId, String currencyCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));

        Currency currency = currencyRepository.findByCurrency(currencyCode)
                .orElseThrow(() -> new RuntimeException("Valuta nije pronađena!"));

        wallet.setUser(user);
        wallet.setCreationDate(LocalDate.now());
        wallet.setCurrentBalance(wallet.getInitialBalance());
        wallet.setArchived(false);

        // Dodeli valute novčaniku
        wallet.getCurrencies().add(currency);

        return walletRepository.save(wallet);
    }

    // 2.1 Funkcionalnost: Pregled stanja po novčaniku (aktivni novčanici)
    public List<Wallet> getUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedFalse(userId);
    }

    // 2.1 Funkcionalnost: Pregled arhiviranih novčanika
    public List<Wallet> getArchivedUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedTrue(userId);
    }

    // 2.1 Funkcionalnost: Pronađi novčanik po ID
    public Optional<Wallet> findById(Long walletId) {
        return walletRepository.findById(walletId);
    }

    // 2.1 Funkcionalnost: Uređivanje novčanika
    public Wallet updateWallet(Long walletId, Wallet updatedWallet) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));

        wallet.setName(updatedWallet.getName());
        wallet.setSavings(updatedWallet.isSavings());

        return walletRepository.save(wallet);
    }

    // 2.1 Funkcionalnost: Arhiviranje neaktivnih novčanika
    public void archiveWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        wallet.setArchived(true);
        walletRepository.save(wallet);
    }

    // 2.1 Funkcionalnost: Aktiviranje novčanika (vraćanje iz arhive)
    public void activateWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        wallet.setArchived(false);
        walletRepository.save(wallet);
    }

    // 2.1 Funkcionalnost: Brisanje novčanika
    public void deleteWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));

        // Za sada samo proverava da novčanik postoji - provera transakcija će biti dodana
        // kada se implementira funkcionalnost transakcija u sledećoj kontrolnoj tački

        walletRepository.delete(wallet);
    }

    // 2.1 Funkcionalnost: U statistici i dashboard prikaz ukupnog stanja korisnika
    public BigDecimal getTotalUserBalance(Long userId) {
        BigDecimal total = walletRepository.getTotalBalanceByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // 2.1 Funkcionalnost: Prikaz pojedinačnog stanja novčanika (za dashboard)
    public BigDecimal getWalletBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        return wallet.getCurrentBalance();
    }
}