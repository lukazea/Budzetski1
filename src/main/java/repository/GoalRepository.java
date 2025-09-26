package repository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import entity.Goal;
import entity.GoalStatus;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserId(Long userId);
    List<Goal> findByUserIdAndStatus(Long userId, GoalStatus status);
    List<Goal> findByWalletId(Long walletId);
    
    @Query("SELECT g FROM Goal g WHERE g.deadline < :date AND g.status = 'ACTIVE'")
    List<Goal> findExpiredGoals(@Param("date") LocalDate date);
}