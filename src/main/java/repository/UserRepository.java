package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Osnovne metode za pronalaženje korisnika
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
}