package controller;

import dto.WalletDto;
import entity.Wallet;
import service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // 2.1 Funkcionalnost: Dodavanje novčanika
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

            Wallet savedWallet = walletService.createWallet(wallet, userId, currencyCode);
            return ResponseEntity.ok(new WalletDto(savedWallet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Pregled stanja po novčaniku (aktivni novčanici)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletDto>> getUserWallets(@PathVariable Long userId) {
        try {
            List<Wallet> wallets = walletService.getUserWallets(userId);
            List<WalletDto> walletDtos = wallets.stream()
                    .map(WalletDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(walletDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Pregled arhiviranih novčanika
    @GetMapping("/user/{userId}/archived")
    public ResponseEntity<List<WalletDto>> getArchivedWallets(@PathVariable Long userId) {
        try {
            List<Wallet> wallets = walletService.getArchivedUserWallets(userId);
            List<WalletDto> walletDtos = wallets.stream()
                    .map(WalletDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(walletDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Preuzmi novčanik po ID (za pregled stanja)
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDto> getWalletById(@PathVariable Long walletId) {
        return walletService.findById(walletId)
                .map(wallet -> ResponseEntity.ok(new WalletDto(wallet)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 2.1 Funkcionalnost: Uređivanje novčanika
    @PutMapping("/{walletId}")
    public ResponseEntity<WalletDto> updateWallet(
            @PathVariable Long walletId,
            @RequestBody WalletDto walletDto) {
        try {
            Wallet updatedWallet = new Wallet();
            updatedWallet.setName(walletDto.getName());
            updatedWallet.setSavings(walletDto.isSavings());

            Wallet wallet = walletService.updateWallet(walletId, updatedWallet);
            return ResponseEntity.ok(new WalletDto(wallet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Arhiviranje neaktivnih novčanika
    @PutMapping("/{walletId}/archive")
    public ResponseEntity<Void> archiveWallet(@PathVariable Long walletId) {
        try {
            walletService.archiveWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Vraćanje novčanika iz arhive
    @PutMapping("/{walletId}/activate")
    public ResponseEntity<Void> activateWallet(@PathVariable Long walletId) {
        try {
            walletService.activateWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Brisanje novčanika
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long walletId) {
        try {
            walletService.deleteWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: U statistici i dashboard prikaz ukupnog stanja korisnika
    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<BigDecimal> getTotalUserBalance(@PathVariable Long userId) {
        try {
            BigDecimal total = walletService.getTotalUserBalance(userId);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.1 Funkcionalnost: Prikaz pojedinačnog stanja novčanika (za dashboard)
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