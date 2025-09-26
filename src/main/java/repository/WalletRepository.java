package repository;

import entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // ----- Osnovne metode -----
    List<Wallet> findByUserId(Long userId);
    List<Wallet> findByUserIdAndArchivedFalse(Long userId);
    List<Wallet> findByUserIdAndArchivedTrue(Long userId);
    List<Wallet> findByUserIdAndSavingsTrue(Long userId);
    Optional<Wallet> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    List<Wallet> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    // ----- Brojanje -----
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.user.id = :userId AND w.archived = false")
    Long countActiveByUserId(@Param("userId") Long userId);

    // ----- Dashboard / statistike -----
    @Query("SELECT SUM(w.balance) FROM Wallet w")
    BigDecimal getTotalSystemBalance();

    @Query("SELECT AVG(w.balance) FROM Wallet w " +
           "WHERE EXISTS (SELECT 1 FROM Transaction t WHERE t.user.id = w.user.id AND t.date >= :since)")
    BigDecimal getAverageBalanceForActiveUsers(@Param("since") LocalDate since);

    @Query("SELECT SUM(w.currentBalance) FROM Wallet w WHERE w.user.id = :userId AND w.archived = false")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}
