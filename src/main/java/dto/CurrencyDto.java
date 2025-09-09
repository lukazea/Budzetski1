package dto;

import entity.Currency;

public class CurrencyDto {
    private Long id;
    private String currency;
    private float valueToEur;

    public CurrencyDto() {}

    public CurrencyDto(Currency currency) {
        this.id = currency.getId();
        this.currency = currency.getCurrency();
        this.valueToEur = currency.getValueToEur();
    }

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public float getValueToEur() { return valueToEur; }
    public void setValueToEur(float valueToEur) { this.valueToEur = valueToEur; }
}