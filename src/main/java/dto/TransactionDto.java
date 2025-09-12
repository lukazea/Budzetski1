package dto;

import entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {
    
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private String categoryName;
    private String walletName;
    private String userName;
    private String currencyCode;
    
    public TransactionDto() {}
    
    public TransactionDto(Transaction transaction) {
    	this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.description = transaction.getName();
        this.transactionDate = transaction.getTransactionDate();
        this.categoryName = transaction.getCategory() != null ? transaction.getCategory().getName() : null;
        this.walletName = transaction.getWallet() != null ? transaction.getWallet().getName() : null;
        this.userName = transaction.getWallet() != null && transaction.getWallet().getUser() != null ? 
                        transaction.getWallet().getUser().getUserName() : null;

        // ODABERIMO PRVU VALUTU IZ SET-A (ako postoji)
        if (transaction.getWallet() != null && transaction.getWallet().getCurrencies() != null
            && !transaction.getWallet().getCurrencies().isEmpty()) {
            this.currencyCode = transaction.getWallet().getCurrencies().iterator().next().getCurrency();
        } else {
            this.currencyCode = null;
        }
    }

    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
