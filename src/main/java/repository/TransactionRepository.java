package repository;

import entity.Transaction;
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
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Transakcije određenog novčanika
    List<Transaction> findByWallet(Wallet wallet);
    List<Transaction> findByWalletId(Long walletId);
    
    // Sa paginacijom i sortiranjem po datumu
    Page<Transaction> findByWalletOrderByTransactionDateDesc(Wallet wallet, Pageable pageable);
    
    // Transakcije korisnika (sve iz svih novčanika)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Filteri po datumu
    List<Transaction> findByWalletAndTransactionDateBetween(Wallet wallet, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndTransactionDateBetween(@Param("userId") Long userId, 
                                                          @Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);
    
    // Filteri po kategoriji
    List<Transaction> findByWalletAndCategory(Wallet wallet, Category category);
    List<Transaction> findByCategoryAndTransactionDateBetween(Category category, LocalDate startDate, LocalDate endDate);
    
    // Filteri po tipu (PRIHOD/TROSAK)
    List<Transaction> findByWalletAndType(Wallet wallet, CategoryType type);
    List<Transaction> findByUserIdAndType(Long userId, CategoryType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndTypeAndTransactionDateBetween(@Param("userId") Long userId, 
                                                                 @Param("type") CategoryType type,
                                                                 @Param("startDate") LocalDate startDate, 
                                                                 @Param("endDate") LocalDate endDate);
    
    // Transfer transakcije
    List<Transaction> findByIsTransferTrue();
    List<Transaction> findByUserIdAndIsTransferTrue(Long userId);
    
    // Statistike - najveće transakcije
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :since ORDER BY t.amount DESC")
    List<Transaction> findTopTransactionsSince(@Param("since") LocalDate since, Pageable pageable);
    
    // Za admin - sve transakcije sistema
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllOrderByTransactionDateDesc(Pageable pageable);
    
    // Suma transakcija po korisniku za period
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getSumByUserIdAndDateBetween(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    // Suma po tipu transakcije
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getSumByUserIdAndTypeAndDateBetween(@Param("userId") Long userId, 
                                                  @Param("type") CategoryType type,
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
    
    // Poslednje transakcije (za admin dashboard)
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :since ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("since") LocalDate since);
    
    // Recurring transakcije
    List<Transaction> findByRecurringTemplateIsNotNull();
    List<Transaction> findByUserIdAndRecurringTemplateIsNotNull(Long userId);
    
    // Statistike po kategorijama
    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY c.id, c.name")
    List<Object[]> getSumByUserIdAndDateBetweenGroupByCategory(@Param("userId") Long userId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY c.id, c.name")
    List<Object[]> getSumByUserIdAndTypeAndDateBetweenGroupByCategory(@Param("userId") Long userId,
                                                                     @Param("type") CategoryType type,
                                                                     @Param("startDate") LocalDate startDate,
                                                                     @Param("endDate") LocalDate endDate);
    
    // Mesečne statistike
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND YEAR(t.transactionDate) = :year GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) ORDER BY MONTH(t.transactionDate)")
    List<Object[]> getMonthlyStatsByUserIdAndTypeAndYear(@Param("userId") Long userId,
                                                        @Param("type") CategoryType type,
                                                        @Param("year") int year);
    
    // Nedeljne statistike
    @Query("SELECT WEEK(t.transactionDate), SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY WEEK(t.transactionDate) ORDER BY WEEK(t.transactionDate)")
    List<Object[]> getWeeklyStatsByUserIdAndTypeAndDateBetween(@Param("userId") Long userId,
                                                              @Param("type") CategoryType type,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
    
    // Dnevne statistike
    @Query("SELECT t.transactionDate, SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY t.transactionDate ORDER BY t.transactionDate")
    List<Object[]> getDailyStatsByUserIdAndTypeAndDateBetween(@Param("userId") Long userId,
                                                             @Param("type") CategoryType type,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
    
    // Top troškovi za period
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = 'TROSAK' AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.amount DESC")
    List<Transaction> findTopExpensesByUserIdAndDateBetween(@Param("userId") Long userId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           Pageable pageable);
    
    // Top prihodi za period
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = 'PRIHOD' AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.amount DESC")
    List<Transaction> findTopIncomeByUserIdAndDateBetween(@Param("userId") Long userId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         Pageable pageable);
    
    // Filtriranje po sumi (iznos veći/manji od)
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
    
    // Kombinovani filteri
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.amount >= :minAmount AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndCategoryIdAndAmountGreaterThanEqualAndDateBetween(@Param("userId") Long userId,
                                                                                      @Param("categoryId") Long categoryId,
                                                                                      @Param("minAmount") BigDecimal minAmount,
                                                                                      @Param("startDate") LocalDate startDate,
                                                                                      @Param("endDate") LocalDate endDate);
}
