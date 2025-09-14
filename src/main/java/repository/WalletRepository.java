package repository;

import entity.Wallet;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Osnovne metode za funkcionalnost 2.1 - Virtuelni novčanik

    // Svi novčanici određenog korisnika
    List<Wallet> findByUserId(Long userId);

    // Aktivni novčanici (nisu arhivirani)
    List<Wallet> findByUserIdAndArchivedFalse(Long userId);

    // Arhivirani novčanici
    List<Wallet> findByUserIdAndArchivedTrue(Long userId);

    // Za statistike i dashboard - ukupno stanje korisnika (suma svih aktivnih novčanika)
    @Query("SELECT SUM(w.currentBalance) FROM Wallet w WHERE w.user.id = :userId AND w.archived = false")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}