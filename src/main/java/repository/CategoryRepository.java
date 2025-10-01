package repository;

import entity.Category;
import entity.CategoryType;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // --- Predefinisane kategorije (globalne) ---
    List<Category> findByPredefinedTrue();

    // --- Kategorije određenog korisnika (custom) ---
    List<Category> findByUser(User user);
    List<Category> findByUserId(Long userId);
    List<Category> findByUserAndPredefinedFalse(User user);

    // --- Kombinovano - predefinisane + korisničke ---
    @Query("SELECT c FROM Category c WHERE (c.predefined = true AND c.active = true) OR c.user.id = :userId")
    List<Category> findAvailableForUser(@Param("userId") Long userId);

    // --- Po tipu kategorije ---
    List<Category> findByType(CategoryType type);
    List<Category> findByTypeAndPredefinedTrue(CategoryType type);

    @Query("SELECT c FROM Category c WHERE c.type = :type AND ((c.predefined = true AND c.active = true) OR c.user.id = :userId)")
    List<Category> findByTypeAndAvailableForUser(@Param("type") CategoryType type, @Param("userId") Long userId);

    // --- Za admin - sve kategorije ---
    List<Category> findByOrderByNameAsc();

    // --- Provera postojanja kategorije sa imenom ---
    boolean existsByNameAndUser(String name, User user);
    boolean existsByNameAndPredefinedTrue(String name);

    // --- Kategorije sa transakcijama (za statistike) ---
    @Query("SELECT DISTINCT c FROM Category c JOIN c.transactions t WHERE t.user.id = :userId")
    List<Category> findCategoriesWithTransactionsByUserId(@Param("userId") Long userId);

    // --- Provera da li kategorija pripada korisniku ili je predefinisana ---
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE c.id = :categoryId AND (c.user.id = :userId OR c.predefined = true)")
    boolean isAvailableForUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    // --- Pronađi kategoriju po nazivu za korisnika ---
    Optional<Category> findByNameAndUserId(String name, Long userId);

    // --- Pronađi predefinisanu kategoriju po nazivu ---
    Optional<Category> findByNameAndPredefinedTrue(String name);

    // --- Nova metoda: pretraga dostupnih kategorija po imenu (predefinisane + korisničke) ---
    @Query("SELECT c FROM Category c " +
            "WHERE ((c.predefined = true AND c.active = true) OR c.user.id = :userId) " +
            "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> findAvailableForUserByNameContaining(@Param("userId") Long userId,
                                                        @Param("searchTerm") String searchTerm);
}