package repository;

import entity.RecurringTransactionTemplate;
import entity.User;
import entity.Wallet;
import entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionTemplateRepository extends JpaRepository<RecurringTransactionTemplate, Long> {
    
    // Ponavljajuće transakcije određenog korisnika
    List<RecurringTransactionTemplate> findByUser(User user);
    List<RecurringTransactionTemplate> findByUserId(Long userId);
    
    // Aktivne ponavljajuće transakcije
    List<RecurringTransactionTemplate> findByUserAndIsActiveTrue(User user);
    List<RecurringTransactionTemplate> findByUserIdAndIsActiveTrue(Long userId);
    
    // Po novčaniku
    List<RecurringTransactionTemplate> findByWallet(Wallet wallet);
    List<RecurringTransactionTemplate> findByWalletAndIsActiveTrue(Wallet wallet);
    
    // Po tipu
    List<RecurringTransactionTemplate> findByUserIdAndType(Long userId, CategoryType type);
    
    // Po frekvenciji
    List<RecurringTransactionTemplate> findByFrequency(String frequency);
    List<RecurringTransactionTemplate> findByUserIdAndFrequency(Long userId, String frequency);
    
    // Transakcije koje treba izvršiti (za scheduled job)
    @Query("SELECT rt FROM RecurringTransactionTemplate rt WHERE rt.isActive = true AND rt.startDate <= :currentDate")
    List<RecurringTransactionTemplate> findActiveTemplatesForExecution(@Param("currentDate") LocalDate currentDate);
    
    // Po datumu kreiranja
    List<RecurringTransactionTemplate> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}