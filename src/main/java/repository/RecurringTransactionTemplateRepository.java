package repository;

import entity.RecurringTransactionTemplate;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import entity.Transaction;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionTemplateRepository extends JpaRepository<RecurringTransactionTemplate, Long> {

    // Pronađi sve aktivne ponavljajuće transakcije za korisnika
    List<RecurringTransactionTemplate> findByUserAndIsActiveTrue(User user);

    // Pronađi sve ponavljajuće transakcije za korisnika (aktivne i neaktivne)
    List<RecurringTransactionTemplate> findByUserOrderByStartDateDesc(User user);

    // Pronađi aktivne template-e čiji je datum početka manji ili jednak današnjem datumu
    @Query("SELECT r FROM RecurringTransactionTemplate r WHERE r.isActive = true AND r.startDate <= :currentDate")
    List<RecurringTransactionTemplate> findActiveTemplatesReadyForExecution(@Param("currentDate") LocalDate currentDate);

    // Pronađi template po ID-u i korisniku (za bezbednost)
    RecurringTransactionTemplate findByIdAndUser(Long id, User user);

    // Pronađi aktivne template-e po frekvenciji
    List<RecurringTransactionTemplate> findByIsActiveTrueAndFrequency(String frequency);

    // Proveri da li korisnik ima aktivne ponavljajuće transakcije
    @Query("SELECT COUNT(r) FROM RecurringTransactionTemplate r WHERE r.user = :user AND r.isActive = true")
    long countActiveByUser(@Param("user") User user);

    // Pronađi transakcije generisane iz određenog template-a (za pregled istorije)
    @Query("SELECT t FROM Transaction t WHERE t.recurringTemplate.id = :templateId ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByTemplateId(@Param("templateId") Long templateId);
}