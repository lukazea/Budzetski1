package service;

import dto.WalletDto;
import entity.Currency;
import entity.User;
import entity.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.WalletRepository;
import repository.UserRepository;
import repository.CurrencyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    // Kreiranje štednog novčanika
    public Wallet createSavingsWallet(User user, String name, Currency currency) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setName(name);

        Set<Currency> currencySet = new HashSet<>();
        currencySet.add(currency);
        wallet.setCurrencies(currencySet);

        wallet.setSavings(true);
        wallet.setCurrentBalance(BigDecimal.ZERO);
        wallet.setCreationDate(LocalDate.now());
        return walletRepository.save(wallet);
    }

    // Dodavanje običnog novčanika
    public WalletDto createWallet(Wallet wallet, Long userId, String currencyCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));

        Currency currency = currencyRepository.findByCurrency(currencyCode)
                .orElseThrow(() -> new RuntimeException("Valuta nije pronađena!"));

        wallet.setUser(user);
        wallet.setCreationDate(LocalDate.now());
        wallet.setCurrentBalance(wallet.getInitialBalance());
        wallet.setArchived(false);

        wallet.getCurrencies().add(currency);

        Wallet saved = walletRepository.save(wallet);
        return convertToDto(saved);
    }

    // Pregled aktivnih novčanika korisnika
    public List<WalletDto> getUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedFalse(userId)
                .stream().map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled arhiviranih novčanika
    public List<WalletDto> getArchivedUserWallets(Long userId) {
        return walletRepository.findByUserIdAndArchivedTrue(userId)
                .stream().map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pronađi novčanik po ID
    public WalletDto findById(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        return convertToDto(wallet);
    }

    // Uređivanje novčanika
    public WalletDto updateWallet(Long walletId, Wallet updatedWallet) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));

        wallet.setName(updatedWallet.getName());
        wallet.setSavings(updatedWallet.isSavings());

        return convertToDto(walletRepository.save(wallet));
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
        walletRepository.delete(wallet);
    }

    // Ukupno stanje korisnika
    public BigDecimal getTotalUserBalance(Long userId) {
        BigDecimal total = walletRepository.getTotalBalanceByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Stanje pojedinačnog novčanika
    public BigDecimal getWalletBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        return wallet.getCurrentBalance();
    }

    // Konverzija Wallet -> WalletDto
    private WalletDto convertToDto(Wallet wallet) {
        WalletDto dto = new WalletDto();
        dto.setId(wallet.getId());
        dto.setName(wallet.getName());
        dto.setInitialBalance(wallet.getInitialBalance());
        dto.setCurrentBalance(wallet.getCurrentBalance());
        dto.setCreationDate(wallet.getCreationDate());
        dto.setUserId(wallet.getUser() != null ? wallet.getUser().getId() : null);
        dto.setSavings(wallet.isSavings());
        dto.setArchived(wallet.isArchived());

        if (wallet.getCurrencies() != null) {
            Set<Long> currencyIds = wallet.getCurrencies().stream()
                    .map(Currency::getId)
                    .collect(Collectors.toSet());
            dto.setCurrencyIds(currencyIds);
        }

        return dto;
    }
}

