package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ----- Osnovne metode -----
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);

    // Provera postojanja korisnika osim određenog ID-a
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userName = :userName AND u.id != :userId")
    boolean existsByUserNameAndIdNot(@Param("userName") String userName, @Param("userId") Long userId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);

    // Provera blokiranih korisnika
    Optional<User> findByIdAndBlockedFalse(Long id);
    boolean existsByIdAndBlockedTrue(Long id);

    // ----- Aktivnost korisnika (Dashboard) -----
    // Broj aktivnih korisnika sa transakcijama u poslednjih X dana
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
           "WHERE EXISTS (SELECT 1 FROM Transaction t WHERE t.user.id = u.id AND t.transactionDate >= :since)")
    Long countActiveUsersSince(@Param("since") LocalDate since);

    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
            "WHERE EXISTS (SELECT 1 FROM Transaction t WHERE t.user.id = u.id AND t.transactionDate >= :since) " +
            "OR EXISTS (SELECT 1 FROM Wallet w JOIN w.transactions t2 " +
            "           WHERE w.user.id = u.id AND t2.transactionDate >= :since)")
    Long countActiveUsersWithWalletActivity(@Param("since") LocalDate since);

}
