package service;

import dto.WalletDto;
import entity.Wallet;
import entity.User;
import entity.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.WalletRepository;
import repository.UserRepository;
import repository.CurrencyRepository;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
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

    // Pronađi novčanik po ID-ju
    public WalletDto findById(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));
        return convertToDto(wallet);
    }

    // Pronađi sve novčanike korisnika
    public List<WalletDto> findUserWallets(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pronađi aktivne novčanike korisnika
    public List<WalletDto> findActiveUserWallets(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdAndArchivedFalse(userId);
        return wallets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Konvertovanje entiteta u DTO
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