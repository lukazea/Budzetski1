package service;

import entity.Currency;
import entity.Wallet;
import repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;


    // Kreiranje nové valute
    public Currency createCurrency(String currencyCode, float valueToEur) {
        if (currencyRepository.existsByCurrency(currencyCode)) {
            throw new RuntimeException("Valuta već postoji!");
        }
        
        Currency currency = new Currency(currencyCode, valueToEur);
        return currencyRepository.save(currency);
    }

    // Ažuriranje kursa
    public Currency updateExchangeRate(String currencyCode, float newRate) {
        Currency currency = currencyRepository.findByCurrency(currencyCode)
            .orElseThrow(() -> new RuntimeException("Valuta nije pronađena!"));
        
        currency.setValueToEur(newRate);
        return currencyRepository.save(currency);
    }

    // Konverzija između valuta
    public BigDecimal convertAmount(BigDecimal amount, Wallet fromWallet, Wallet toWallet) {
        // Ako su iste valute
        Currency fromCurrency = fromWallet.getCurrencies().iterator().next();
        Currency toCurrency = toWallet.getCurrencies().iterator().next();
        
        if (fromCurrency.getCurrency().equals(toCurrency.getCurrency())) {
            return amount;
        }
        
        // Konverzija preko EUR
        BigDecimal amountInEur = amount.divide(
            BigDecimal.valueOf(fromCurrency.getValueToEur()), 4, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountInEur.multiply(
            BigDecimal.valueOf(toCurrency.getValueToEur()));
        
        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    // Konverzija direktno između kodova valuta
    public BigDecimal convertBetweenCurrencies(BigDecimal amount, String fromCode, String toCode) {
        if (fromCode.equals(toCode)) {
            return amount;
        }
        
        Currency fromCurrency = currencyRepository.findByCurrency(fromCode)
            .orElseThrow(() -> new RuntimeException("Izvorišna valuta nije pronađena!"));
        Currency toCurrency = currencyRepository.findByCurrency(toCode)
            .orElseThrow(() -> new RuntimeException("Odredišna valuta nije pronađena!"));
        
        // Konverzija preko EUR
        BigDecimal amountInEur = amount.divide(
            BigDecimal.valueOf(fromCurrency.getValueToEur()), 4, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountInEur.multiply(
            BigDecimal.valueOf(toCurrency.getValueToEur()));
        
        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    // Preuzmi kursnu listu sa spoljašnjeg API-ja (bonus funkcionalnost)
    public void updateExchangeRatesFromAPI() {
        try {
            // Poziv na https://frankfurter.dev/ API
            String url = "https://api.frankfurter.app/latest";
            // TODO: Implementirati parsing response-a i ažuriranje kurseva
            // Ovo je kompleksnija implementacija koja zahteva dodatni kod
        } catch (Exception e) {
            System.out.println("Greška pri preuzimanju kurseva: " + e.getMessage());
        }
    }

    // Sve valute
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAllByOrderByCurrencyAsc();
    }

    // Pronađi valutu
    public Optional<Currency> findByCurrencyCode(String code) {
        return currencyRepository.findByCurrency(code);
    }

    // Brisanje valute
    public void deleteCurrency(Long currencyId) {
        Currency currency = currencyRepository.findById(currencyId)
            .orElseThrow(() -> new RuntimeException("Valuta nije pronađena!"));
        
        // Proveri da li se koristi u novčanicima
        if (!currency.getWallets().isEmpty()) {
            throw new RuntimeException("Ne možete obrisati valutu koja se koristi!");
        }
        
        currencyRepository.delete(currency);
    }
}

