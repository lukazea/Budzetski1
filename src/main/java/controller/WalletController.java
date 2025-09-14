package controller;

import dto.WalletDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // Pronađi novčanik po ID-ju
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDto> getWallet(
            @PathVariable Long walletId,
            @RequestParam Long userId) {
        try {
            WalletDto wallet = walletService.findById(walletId, userId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Pronađi sve novčanike korisnika
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletDto>> getUserWallets(@PathVariable Long userId) {
        try {
            List<WalletDto> wallets = walletService.findUserWallets(userId);
            return ResponseEntity.ok(wallets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Pronađi aktivne novčanike korisnika
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<WalletDto>> getActiveUserWallets(@PathVariable Long userId) {
        try {
            List<WalletDto> wallets = walletService.findActiveUserWallets(userId);
            return ResponseEntity.ok(wallets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}