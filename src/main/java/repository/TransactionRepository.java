package repository;

import entity.Transaction;
import entity.User;
import entity.Wallet;
import entity.Category;
import entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ----- Osnovne metode -----
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByWallet(Wallet wallet);
    List<Transaction> findByWalletId(Long walletId);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByWalletIdAndTransactionDateBetween(Long walletId, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByWalletAndTransactionDateBetween(Wallet wallet, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndIsTransferTrue(Long userId);
    List<Transaction> findByIsTransferTrue();

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (t.fromWallet.id = :walletId OR t.toWallet.id = :walletId) AND t.isTransfer = true")
    List<Transaction> findTransfersByWallet(@Param("userId") Long userId, @Param("walletId") Long walletId);

    List<Transaction> findByWalletAndCategory(Wallet wallet, Category category);
    List<Transaction> findByCategoryAndTransactionDateBetween(Category category, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByWalletAndType(Wallet wallet, CategoryType type);
    List<Transaction> findByUserIdAndType(Long userId, CategoryType type);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndTypeAndTransactionDateBetween(@Param("userId") Long userId,
                                                                  @Param("type") CategoryType type,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    List<Transaction> findByRecurringTemplateIsNotNull();
    List<Transaction> findByUserIdAndRecurringTemplateIsNotNull(Long userId);

    // ----- Paginated metode -----
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);
    Page<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Transaction> findByUserIdOrderByDate(@Param("userId") Long userId, Pageable pageable);

    // ----- Daily/Weekly/Monthly/Quarterly -----
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate = :date")
    List<Transaction> findDailyTransactions(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    List<Transaction> findWeeklyTransactions(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    List<Transaction> findMonthlyTransactions(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND YEAR(t.transactionDate) = :year AND QUARTER(t.transactionDate) = :quarter")
    List<Transaction> findQuarterlyTransactions(@Param("userId") Long userId, @Param("year") int year, @Param("quarter") int quarter);

    // ----- Admin filtrirane metode -----
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:userId IS NULL OR t.user.id = :userId) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<Transaction> findFilteredTransactions(@Param("userId") Long userId,
                                               @Param("categoryId") Long categoryId,
                                               @Param("minAmount") BigDecimal minAmount,
                                               @Param("maxAmount") BigDecimal maxAmount,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               Pageable pageable);

    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllOrderByTransactionDateDesc(Pageable pageable);

    // ----- Dashboard / statistike -----
    Page<Transaction> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Page<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.date >= :since ORDER BY t.amount DESC")
    List<Transaction> findTopTransactionsSince(@Param("since") LocalDate since, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :since ORDER BY t.amount DESC")
    List<Transaction> findTopTransactionsSinceTimestamp(@Param("since") LocalDateTime since, Pageable pageable);

    long countByUserId(Long userId);
    long countByCategoryId(Long categoryId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.category.id = :categoryId")
    BigDecimal sumAmountByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT t FROM Transaction t ORDER BY t.amount DESC")
    Page<Transaction> findTopTransactions(Pageable pageable);

    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    Page<Transaction> findRecentTransactions(Pageable pageable);

    // ----- Statističke metode po kategorijama, danima, nedeljama, mesecima -----
    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY c.id, c.name")
    List<Object[]> getSumByUserIdAndDateBetweenGroupByCategory(@Param("userId") Long userId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY c.id, c.name")
    List<Object[]> getSumByUserIdAndTypeAndDateBetweenGroupByCategory(@Param("userId") Long userId,
                                                                     @Param("type") CategoryType type,
                                                                     @Param("startDate") LocalDate startDate,
                                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND YEAR(t.transactionDate) = :year GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) ORDER BY MONTH(t.transactionDate)")
    List<Object[]> getMonthlyStatsByUserIdAndTypeAndYear(@Param("userId") Long userId,
                                                        @Param("type") CategoryType type,
                                                        @Param("year") int year);

    @Query("SELECT WEEK(t.transactionDate), SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY WEEK(t.transactionDate) ORDER BY WEEK(t.transactionDate)")
    List<Object[]> getWeeklyStatsByUserIdAndTypeAndDateBetween(@Param("userId") Long userId,
                                                              @Param("type") CategoryType type,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT t.transactionDate, SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY t.transactionDate ORDER BY t.transactionDate")
    List<Object[]> getDailyStatsByUserIdAndTypeAndDateBetween(@Param("userId") Long userId,
                                                             @Param("type") CategoryType type,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    // ----- Top prihodi/troškovi -----
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = 'TROSAK' AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.amount DESC")
    List<Transaction> findTopExpensesByUserIdAndDateBetween(@Param("userId") Long userId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = 'PRIHOD' AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.amount DESC")
    List<Transaction> findTopIncomeByUserIdAndDateBetween(@Param("userId") Long userId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         Pageable pageable);

    // ----- Filteri po sumi i kombinovani filteri -----
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.amount >= :minAmount AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndAmountGreaterThanEqualAndDateBetween(@Param("userId") Long userId,
                                                                         @Param("minAmount") BigDecimal minAmount,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.amount BETWEEN :minAmount AND :maxAmount AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndAmountBetweenAndDateBetween(@Param("userId") Long userId,
                                                               @Param("minAmount") BigDecimal minAmount,
                                                               @Param("maxAmount") BigDecimal maxAmount,
                                                               @Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.amount >= :minAmount AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndCategoryIdAndAmountGreaterThanEqualAndDateBetween(@Param("userId") Long userId,
                                                                                      @Param("categoryId") Long categoryId,
                                                                                      @Param("minAmount") BigDecimal minAmount,
                                                                                      @Param("startDate") LocalDate startDate,
                                                                                      @Param("endDate") LocalDate endDate);
}
