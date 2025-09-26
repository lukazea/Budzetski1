package repository;

import entity.Transaction;
import entity.User;
import entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Osnovne metode
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByWalletId(Long walletId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    // Metode za filtriranje po datumu
    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    );

    List<Transaction> findByWalletIdAndTransactionDateBetween(
            Long walletId, LocalDate startDate, LocalDate endDate
    );

    // Metode za transfer transakcije
    List<Transaction> findByUserIdAndIsTransferTrue(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (t.fromWallet.id = :walletId OR t.toWallet.id = :walletId) " +
            "AND t.isTransfer = true")
    List<Transaction> findTransfersByWallet(@Param("userId") Long userId,
                                            @Param("walletId") Long walletId);

    // Paginacija
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable
    );

    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);

    // Pregled po periodima - dnevni
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.transactionDate = :date")
    List<Transaction> findDailyTransactions(@Param("userId") Long userId,
                                            @Param("date") LocalDate date);

    // Pregled po periodima - nedeljni
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    List<Transaction> findWeeklyTransactions(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // Pregled po periodima - mesečni
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    List<Transaction> findMonthlyTransactions(@Param("userId") Long userId,
                                              @Param("year") int year,
                                              @Param("month") int month);

    // Pregled po periodima - kvartalni
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND YEAR(t.transactionDate) = :year " +
            "AND QUARTER(t.transactionDate) = :quarter")
    List<Transaction> findQuarterlyTransactions(@Param("userId") Long userId,
                                                @Param("year") int year,
                                                @Param("quarter") int quarter);

    // Kombinovani pregled (obične transakcije + transferi)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (t.wallet.id = :walletId " +
            "OR t.fromWallet.id = :walletId " +
            "OR t.toWallet.id = :walletId)")
    List<Transaction> findAllTransactionsByWallet(@Param("userId") Long userId,
                                                  @Param("walletId") Long walletId);

    // Paginacija sa sortiranjem
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> findByUserIdOrderByDate(@Param("userId") Long userId,
                                              Pageable pageable);

    // Provera da li korisnik ima pristup transakciji
    boolean existsByIdAndUserId(Long id, Long userId);

    // Brisanje transakcija korisnika
    void deleteByIdAndUserId(Long id, Long userId);
}