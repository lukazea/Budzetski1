package repository;

import entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    // Pronađi po nazivu valute (EUR, USD, RSD...)
    Optional<Currency> findByCurrency(String currency);
    
    // Proveri da li postoji valuta
    boolean existsByCurrency(String currency);
    
    // Sortiranje po nazivu
    List<Currency> findAllByOrderByCurrencyAsc();
    
    // Za konverziju - pronađi EUR (bazna valuta)
    Optional<Currency> findByCurrencyIgnoreCase(String currency);
    
    // Valute sa određenim opsegom kurseva (za statistike)
    List<Currency> findByValueToEurBetween(float minValue, float maxValue);
}