package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Broj aktivnih korisnika (imali transakcije u poslednjih X dana)
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
           "WHERE EXISTS (SELECT 1 FROM Transaction t WHERE t.user.id = u.id AND t.date >= :since)")
    Long countActiveUsersSince(@Param("since") LocalDate since);
    
    // Alternativno - ako se aktivnost računa i po wallet operacijama
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
           "WHERE EXISTS (SELECT 1 FROM Transaction t WHERE t.user.id = u.id AND t.date >= :since) " +
           "OR EXISTS (SELECT 1 FROM Wallet w WHERE w.user.id = u.id AND w.lastActivity >= :since)")
    Long countActiveUsersWithWalletActivity(@Param("since") LocalDate since);
}