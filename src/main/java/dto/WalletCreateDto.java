package dto;

import java.math.BigDecimal;


public class WalletCreateDto {
    private String name;
    private BigDecimal initialBalance;
    private boolean savings;

    public WalletCreateDto() {} // required by Jackson

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public boolean isSavings() { return savings; }
    public void setSavings(boolean savings) { this.savings = savings; }
}
