package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Pronađi korisnika koji nije blokiran
    Optional<User> findByIdAndBlockedFalse(Long id);

    // Proveri da li je korisnik blokiran
    boolean existsByIdAndBlockedTrue(Long id);
}