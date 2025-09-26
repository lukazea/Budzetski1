package repository;

import entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    // Pronađi valutu po nazivu
    Optional<Currency> findByCurrency(String currency);

    // Proveri da li valuta postoji
    boolean existsByCurrency(String currency);
}
