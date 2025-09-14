package repository;

import entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllOrderByTransactionDateDesc(Pageable pageable);
    
    // NOVA METODA za transakcije specifičnog korisnika
    @Query("SELECT t FROM Transaction t WHERE t.wallet.user.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserIdOrderByTransactionDateDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.wallet.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}