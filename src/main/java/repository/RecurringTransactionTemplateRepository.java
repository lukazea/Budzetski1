package repository;
import entity.RecurringTransactionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringTransactionTemplateRepository extends JpaRepository<RecurringTransactionTemplate,Long> {
}
