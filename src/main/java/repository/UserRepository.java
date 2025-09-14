package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Prva funkcionalnost - broj registrovanih korisnika
    // Koristi se ugrađena count() metoda iz JpaRepository
}