package repository;

import entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Pronađi novčanik po ID-ju i korisniku
    Optional<Wallet> findByIdAndUserId(Long id, Long userId);

    // Pronađi sve novčanike korisnika
    List<Wallet> findByUserId(Long userId);

    // Pronađi aktivne (nearhivirane) novčanike korisnika
    List<Wallet> findByUserIdAndArchivedFalse(Long userId);

    // Pronađi štedne novčanike korisnika
    List<Wallet> findByUserIdAndSavingsTrue(Long userId);

    // Proveri da li novčanik pripada korisniku
    boolean existsByIdAndUserId(Long id, Long userId);

    // Pronađi novčanike po nazivu
    List<Wallet> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    // Broj novčanika korisnika
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // Broj aktivnih novčanika korisnika
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.user.id = :userId AND w.archived = false")
    Long countActiveByUserId(@Param("userId") Long userId);
}