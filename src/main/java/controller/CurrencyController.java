package controller;

import dto.CurrencyDto;
import entity.Currency;
import service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/currencies")

public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    // Preuzmi sve valute
    @GetMapping
    public ResponseEntity<List<CurrencyDto>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        List<CurrencyDto> currencyDtos = currencies.stream()
            .map(CurrencyDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(currencyDtos);
    }

    // Kreiranje nove valute
    @PostMapping
    public ResponseEntity<CurrencyDto> createCurrency(@RequestBody CurrencyDto currencyDto) {
        try {
            Currency currency = currencyService.createCurrency(
                currencyDto.getCurrency(), 
                currencyDto.getValueToEur()
            );
            return ResponseEntity.ok(new CurrencyDto(currency));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi valutu po kodu
    @GetMapping("/{currencyCode}")
    public ResponseEntity<CurrencyDto> getCurrency(@PathVariable String currencyCode) {
        return currencyService.findByCurrencyCode(currencyCode)
            .map(currency -> ResponseEntity.ok(new CurrencyDto(currency)))
            .orElse(ResponseEntity.notFound().build());
    }

    // Ažuriranje kursne liste
    @PutMapping("/{currencyCode}")
    public ResponseEntity<CurrencyDto> updateExchangeRate(
            @PathVariable String currencyCode, 
            @RequestParam float newRate) {
        try {
            Currency currency = currencyService.updateExchangeRate(currencyCode, newRate);
            return ResponseEntity.ok(new CurrencyDto(currency));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Konverzija između valuta
    @GetMapping("/convert")
    public ResponseEntity<BigDecimal> convertBetweenCurrencies(
            @RequestParam BigDecimal amount,
            @RequestParam String fromCode,
            @RequestParam String toCode) {
        try {
            BigDecimal convertedAmount = currencyService.convertBetweenCurrencies(
                amount, fromCode, toCode);
            return ResponseEntity.ok(convertedAmount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Brisanje valute
    @DeleteMapping("/{currencyId}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Long currencyId) {
        try {
            currencyService.deleteCurrency(currencyId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ažuriranje kursne liste iz spoljašnjeg API-ja
    @PostMapping("/update-rates")
    public ResponseEntity<Void> updateExchangeRatesFromAPI() {
        try {
            currencyService.updateExchangeRatesFromAPI();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}