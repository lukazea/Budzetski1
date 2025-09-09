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
}
