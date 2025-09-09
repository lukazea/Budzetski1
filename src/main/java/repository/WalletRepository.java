package repository;

import entity.Wallet;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    // Novčanici određenog korisnika
    List<Wallet> findByUser(User user);
    List<Wallet> findByUserId(Long userId);
    
    // Aktivni novčanici (nisu arhivirani)
    List<Wallet> findByUserAndArchivedFalse(User user);
    List<Wallet> findByUserIdAndArchivedFalse(Long userId);
    
    // Arhivirani novčanici
    List<Wallet> findByUserAndArchivedTrue(User user);
    List<Wallet> findByUserIdAndArchivedTrue(Long userId);
    
    // Štedni novčanici
    List<Wallet> findByUserAndSavingsTrue(User user);
    List<Wallet> findByUserIdAndSavingsTrue(Long userId);
    
    // Ukupno stanje korisnika (suma svih aktivnih novčanika)
    @Query("SELECT SUM(w.currentBalance) FROM Wallet w WHERE w.user.id = :userId AND w.archived = false")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
    
    // Za admin dashboard - ukupan novac na svim računima
    @Query("SELECT SUM(w.currentBalance) FROM Wallet w WHERE w.archived = false")
    BigDecimal getTotalSystemBalance();
    
    // Prosečno stanje aktivnih korisnika
    @Query("SELECT AVG(w.currentBalance) FROM Wallet w WHERE w.archived = false AND w.user.blocked = false")
    BigDecimal getAverageUserBalance();
    
    // Novčanici kreirani u određenom periodu
    List<Wallet> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);
}