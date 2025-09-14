package service;

import dto.DashboardStatsDto;
import dto.TransactionDto;
import entity.Transaction;
import repository.UserRepository;
import repository.TransactionRepository;
import repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private WalletRepository walletRepository;

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        
        // Ukupan broj korisnika
        stats.setTotalUsers(userRepository.count());
        
        // Broj aktivnih korisnika (aktivnost u poslednjih 30 dana)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        stats.setActiveUsers(userRepository.countActiveUsersSince(thirtyDaysAgo));
        
        // Prosečna količina novca kod aktivnih korisnika
        BigDecimal averageAmount = walletRepository.getAverageBalanceForActiveUsers(thirtyDaysAgo);
        stats.setAverageActiveUserBalance(averageAmount != null ? averageAmount : BigDecimal.ZERO);
        
        // Ukupna količina novca na svim računima
        BigDecimal totalAmount = walletRepository.getTotalSystemBalance();
        stats.setTotalSystemBalance(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        return stats;
    }

    public List<TransactionDto> getTopTransactionsLastMonth() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Pageable top10 = PageRequest.of(0, 10);
        
        List<Transaction> transactions = transactionRepository
            .findTopTransactionsSince(thirtyDaysAgo, top10);
        
        return transactions.stream()
            .map(TransactionDto::new)
            .collect(Collectors.toList());
    }

    public List<TransactionDto> getTopTransactionsLastTwoMinutes() {
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        Pageable top10 = PageRequest.of(0, 10);
        
        List<Transaction> transactions = transactionRepository
            .findTopTransactionsSinceTimestamp(twoMinutesAgo, top10);
        
        return transactions.stream()
            .map(TransactionDto::new)
            .collect(Collectors.toList());
    }
}