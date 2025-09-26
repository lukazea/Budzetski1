package repository;

import entity.Category;
import entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Pronađi sve kategorije korisnika (uključujući predefinisane)
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.predefined = true")
    List<Category> findAvailableForUser(@Param("userId") Long userId);

    // Pronađi samo korisničke kategorije
    List<Category> findByUserId(Long userId);

    // Pronađi predefinisane kategorije
    List<Category> findByPredefinedTrue();

    // Pronađi kategorije po tipu
    List<Category> findByType(CategoryType type);

    // Pronađi kategorije korisnika po tipu
    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.predefined = true) AND c.type = :type")
    List<Category> findByUserAndType(@Param("userId") Long userId, @Param("type") CategoryType type);

    // Proveri da li kategorija pripada korisniku ili je predefinisana
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE c.id = :categoryId AND (c.user.id = :userId OR c.predefined = true)")
    boolean isAvailableForUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    // Pronađi kategoriju po nazivu za korisnika
    Optional<Category> findByNameAndUserId(String name, Long userId);

    // Pronađi predefinisanu kategoriju po nazivu
    Optional<Category> findByNameAndPredefinedTrue(String name);
}