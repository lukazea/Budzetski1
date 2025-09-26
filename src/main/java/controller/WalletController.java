package controller;

import dto.WalletDto;
import entity.Wallet;
import service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // --- KREIRANJE NOVČANIKA ---
    @PostMapping("/user/{userId}")
    public ResponseEntity<WalletDto> createWallet(
            @PathVariable Long userId,
            @RequestParam String currencyCode,
            @RequestBody WalletDto walletDto) {
        try {
            Wallet wallet = new Wallet();
            wallet.setName(walletDto.getName());
            wallet.setInitialBalance(walletDto.getInitialBalance());
            wallet.setSavings(walletDto.isSavings());

            WalletDto savedWallet = walletService.createWallet(wallet, userId, currencyCode);
            return ResponseEntity.ok(savedWallet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- SVI NOVČANICI KORISNIKA ---
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletDto>> getUserWallets(@PathVariable Long userId) {
        try {
            List<WalletDto> wallets = walletService.getUserWallets(userId);
            return ResponseEntity.ok(wallets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- ARHIVIRANI NOVČANICI ---
    @GetMapping("/user/{userId}/archived")
    public ResponseEntity<List<WalletDto>> getArchivedWallets(@PathVariable Long userId) {
        try {
            List<WalletDto> wallets = walletService.getArchivedUserWallets(userId);
            return ResponseEntity.ok(wallets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- JEDAN NOVČANIK PO ID ---
    @GetMapping("/user/{userId}/{walletId}")
    public ResponseEntity<WalletDto> getWalletById(
            @PathVariable Long walletId,
            @PathVariable Long userId) {
        try {
            WalletDto wallet = walletService.findById(walletId, userId);
            return ResponseEntity.ok(wallet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- IZMENA NOVČANIKA ---
    @PutMapping("/{walletId}")
    public ResponseEntity<WalletDto> updateWallet(
            @PathVariable Long walletId,
            @RequestBody WalletDto walletDto) {
        try {
            Wallet updatedWallet = new Wallet();
            updatedWallet.setName(walletDto.getName());
            updatedWallet.setSavings(walletDto.isSavings());

            WalletDto wallet = walletService.updateWallet(walletId, updatedWallet);
            return ResponseEntity.ok(wallet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- ARHIVIRANJE ---
    @PutMapping("/{walletId}/archive")
    public ResponseEntity<Void> archiveWallet(@PathVariable Long walletId) {
        try {
            walletService.archiveWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- AKTIVACIJA ---
    @PutMapping("/{walletId}/activate")
    public ResponseEntity<Void> activateWallet(@PathVariable Long walletId) {
        try {
            walletService.activateWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- BRISANJE ---
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long walletId) {
        try {
            walletService.deleteWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- UKUPAN BALANS KORISNIKA ---
    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<BigDecimal> getTotalUserBalance(@PathVariable Long userId) {
        try {
            BigDecimal total = walletService.getTotalUserBalance(userId);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- BALANS PO NOVČANIKU ---
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long walletId) {
        try {
            BigDecimal balance = walletService.getWalletBalance(walletId);
            return ResponseEntity.ok(balance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
