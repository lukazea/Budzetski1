package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class WalletDto {
    private Long id;
    private String name;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private Set<Long> currencyIds;
    private LocalDate creationDate;
    private Long userId;
    private boolean savings;
    private boolean archived;

    // Konstruktori
    public WalletDto() {}

    public WalletDto(String name, BigDecimal initialBalance, BigDecimal currentBalance,
                     LocalDate creationDate, Long userId, boolean savings) {
        this.name = name;
        this.initialBalance = initialBalance;
        this.currentBalance = currentBalance;
        this.creationDate = creationDate;
        this.userId = userId;
        this.savings = savings;
        this.archived = false;
    }

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Set<Long> getCurrencyIds() { return currencyIds; }
    public void setCurrencyIds(Set<Long> currencyIds) { this.currencyIds = currencyIds; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isSavings() { return savings; }
    public void setSavings(boolean savings) { this.savings = savings; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}