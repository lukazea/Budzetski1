package repository;

import entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    // Ukupna količina novca na svim računima u sistemu
    @Query("SELECT SUM(w.balance) FROM Wallet w")
    BigDecimal getTotalSystemBalance();
    
    // Prosečna količina novca kod aktivnih korisnika
    @Query("SELECT AVG(w.balance) FROM Wallet w " +
           "WHERE EXISTS (SELECT 1 FROM Transaction t WHERE t.user.id = w.user.id AND t.date >= :since)")
    BigDecimal getAverageBalanceForActiveUsers(@Param("since") LocalDate since);
    
    // Broj wallet-a po korisniku
    Long countByUserId(Long userId);
    
    // Ukupan balance po korisniku
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}