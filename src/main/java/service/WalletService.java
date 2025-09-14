package service;

import entity.Wallet;
import repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    // metoda koju TransactionService koristi
    public Optional<Wallet> findById(Long id) {
        return walletRepository.findById(id);
    }

    // dodatne metode po potrebi (kreiranje, update, brisanje)
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public void delete(Long id) {
        walletRepository.deleteById(id);
    }
}
