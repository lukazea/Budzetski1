package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Osnovni find metodi - prilagođeni vašim poljima
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    
    // Za početnu stranicu - broj registrovanih korisnika
    long count();
    
    // Za admin dashboard - broj nelokovanih korisnika
    @Query("SELECT COUNT(u) FROM User u WHERE u.blocked = false")
    long countActiveUsers();
    
    // Svi blokirani korisnici (za admin)
    List<User> findByBlockedTrue();
    
    // Svi neblokirani korisnici (za admin)
    List<User> findByBlockedFalse();
    
    // Korisnici po ulozi
    List<User> findByRole(User.Role role);
    
    // Korisnici registrovani u određenom periodu
    List<User> findByRegistrationdateBetween(LocalDate startDate, LocalDate endDate);
    
    // Korisnici sa određenom valutom
    List<User> findByCurrency(String currency);
    
    // Za admin - svi korisnici sortirani po datumu registracije
    @Query("SELECT u FROM User u ORDER BY u.registrationdate DESC")
    List<User> findAllOrderByRegistrationDateDesc();
}
