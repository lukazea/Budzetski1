package repository;

import entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    // Za pronalaženje valute po nazivu (kod valute)
    Optional<Currency> findByCurrency(String currency);
}