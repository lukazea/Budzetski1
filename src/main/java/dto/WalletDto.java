package dto;

import entity.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class WalletDto {
    private Long id;
    private String name;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private LocalDate creationDate;
    private boolean savings;
    private boolean archived;
    private Long userId;
    private String userName;
    private Set<String> currencies;
    private Set<TransactionDto> transactions;

    public WalletDto() {}

    public WalletDto(Wallet wallet) {
        this.id = wallet.getId();
        this.name = wallet.getName();
        this.initialBalance = wallet.getInitialBalance();
        this.currentBalance = wallet.getCurrentBalance();
        this.creationDate = wallet.getCreationDate();
        this.savings = wallet.isSavings();
        this.archived = wallet.isArchived();

        if (wallet.getUser() != null) {
            this.userId = wallet.getUser().getId();
            this.userName = wallet.getUser().getUserName();
        }

        if (wallet.getCurrencies() != null) {
            this.currencies = wallet.getCurrencies().stream()
                    .map(currency -> currency.getCurrency())
                    .collect(Collectors.toSet());
        }

    }

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public boolean isSavings() { return savings; }
    public void setSavings(boolean savings) { this.savings = savings; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Set<String> getCurrencies() { return currencies; }
    public void setCurrencies(Set<String> currencies) { this.currencies = currencies; }

    public Set<TransactionDto> getTransactions() { return transactions; }
    public void setTransactions(Set<TransactionDto> transactions) { this.transactions = transactions; }
}