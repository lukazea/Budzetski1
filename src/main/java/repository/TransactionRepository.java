package repository;

import entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Filtrirane transakcije - glavna query
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:userId IS NULL OR t.user.id = :userId) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR t.date >= :startDate) AND " +
           "(:endDate IS NULL OR t.date <= :endDate)")
    Page<Transaction> findFilteredTransactions(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable);

    // Transakcije po korisniku
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    // Transakcije po kategoriji
    Page<Transaction> findByCategoryId(Long categoryId, Pageable pageable);

    // Transakcije u opsegu suma
    Page<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    // Transakcije u datumskom opsegu
    Page<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Top transakcije u poslednjih X dana (po datumu)
    @Query("SELECT t FROM Transaction t WHERE t.date >= :since ORDER BY t.amount DESC")
    List<Transaction> findTopTransactionsSince(@Param("since") LocalDate since, Pageable pageable);

    // Top transakcije u poslednjih X minuta (po createdAt timestamp-u)
    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :since ORDER BY t.amount DESC")
    List<Transaction> findTopTransactionsSinceTimestamp(@Param("since") LocalDateTime since, Pageable pageable);

    // Brojanje transakcija po korisniku
    long countByUserId(Long userId);

    // Brojanje transakcija po kategoriji
    long countByCategoryId(Long categoryId);

    // Ukupna suma po korisniku
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    // Ukupna suma po kategoriji
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.category.id = :categoryId")
    BigDecimal sumAmountByCategoryId(@Param("categoryId") Long categoryId);

    // Top N najvećih transakcija
    @Query("SELECT t FROM Transaction t ORDER BY t.amount DESC")
    Page<Transaction> findTopTransactions(Pageable pageable);

    // Poslednje transakcije
    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    Page<Transaction> findRecentTransactions(Pageable pageable);
}
