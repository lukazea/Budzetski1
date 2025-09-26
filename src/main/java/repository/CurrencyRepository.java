package repository;

import entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    // Pronađi valutu po kodu (npr. "USD", "EUR")
    Optional<Currency> findByCode(String code);

    // Pronađi valutu po nazivu
    Optional<Currency> findByCurrency(String currency);

    // Proveri da li valuta postoji
    boolean existsByCurrency(String currency);

    // Sortiranje po nazivu
    List<Currency> findAllByOrderByCurrencyAsc();

    // Za konverziju - pronađi EUR (bazna valuta)
    Optional<Currency> findByCurrencyIgnoreCase(String currency);

    // Valute sa određenim opsegom kurseva (za statistike)
    List<Currency> findByValueToEurBetween(float minValue, float maxValue);
}

