package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Pronađi korisnika po korisničkom imenu
    Optional<User> findByUserName(String userName);

    // Pronađi korisnika po email adresi
    Optional<User> findByEmail(String email);

    // Proveri da li postoji korisnik sa datim korisničkim imenom
    boolean existsByUserName(String userName);

    // Proveri da li postoji korisnik sa datom email adresom
    boolean existsByEmail(String email);

    // Proveri da li postoji korisnik sa datim korisničkim imenom osim određenog ID-a
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userName = :userName AND u.id != :userId")
    boolean existsByUserNameAndIdNot(@Param("userName") String userName, @Param("userId") Long userId);

    // Proveri da li postoji korisnik sa datom email adresom osim određenog ID-a
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);

    // Pronađi korisnika koji nije blokiran
    Optional<User> findByIdAndBlockedFalse(Long id);

    // Proveri da li je korisnik blokiran
    boolean existsByIdAndBlockedTrue(Long id);

    // Broj registrovanih korisnika (može se koristiti ugrađena count() metoda iz JpaRepository)
}
