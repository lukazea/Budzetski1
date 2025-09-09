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

    // Kreiranje novog novčanika
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

    // Preuzmi sve aktivne novčanike korisnika
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

    // Preuzmi arhivirane novčanike korisnika
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

    // Preuzmi štedne novčanike korisnika
    @GetMapping("/user/{userId}/savings")
    public ResponseEntity<List<WalletDto>> getSavingsWallets(@PathVariable Long userId) {
        try {
            List<Wallet> wallets = walletService.getSavingsWallets(userId);
            List<WalletDto> walletDtos = wallets.stream()
                .map(WalletDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(walletDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi novčanik po ID
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDto> getWalletById(@PathVariable Long walletId) {
        return walletService.findById(walletId)
            .map(wallet -> ResponseEntity.ok(new WalletDto(wallet)))
            .orElse(ResponseEntity.notFound().build());
    }

    // Ažuriranje novčanika
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

    // Arhiviranje novčanika
    @PutMapping("/{walletId}/archive")
    public ResponseEntity<Void> archiveWallet(@PathVariable Long walletId) {
        try {
            walletService.archiveWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aktiviranje novčanika
    @PutMapping("/{walletId}/activate")
    public ResponseEntity<Void> activateWallet(@PathVariable Long walletId) {
        try {
            walletService.activateWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Brisanje novčanika
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long walletId) {
        try {
            walletService.deleteWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ažuriranje balansa (manuelno)
    @PutMapping("/{walletId}/balance")
    public ResponseEntity<Void> updateBalance(
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount) {
        try {
            walletService.updateBalance(walletId, amount);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ukupno stanje korisnika
    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<BigDecimal> getTotalUserBalance(@PathVariable Long userId) {
        try {
            BigDecimal total = walletService.getTotalUserBalance(userId);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === ADMIN ENDPOINTS ===
    
    // Ukupno stanje u sistemu (admin)
    @GetMapping("/admin/total-balance")
    public ResponseEntity<BigDecimal> getTotalSystemBalance() {
        BigDecimal total = walletService.getTotalSystemBalance();
        return ResponseEntity.ok(total);
    }

    // Prosečno stanje korisnika (admin)
    @GetMapping("/admin/average-balance")
    public ResponseEntity<BigDecimal> getAverageUserBalance() {
        BigDecimal average = walletService.getAverageUserBalance();
        return ResponseEntity.ok(average);
    }
}