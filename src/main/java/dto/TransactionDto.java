package dto;

import entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private String categoryName;
    private Long categoryId;
    private String userName;
    private Long userId;
    private String currencyCode;
    private Long currencyId;
    private String walletName;
    private Long walletId;
    
    // TIMESTAMP POLJA - POTREBNO ZA DASHBOARD
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // PRAZAN KONSTRUKTOR
    public TransactionDto() {}

    // GLAVNI KONSTRUKTOR - OVAJ JE KLJUČAN ZA .map(TransactionDto::new)
    public TransactionDto(Transaction transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.description = transaction.getName();
        this.date = transaction.getTransactionDate();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
        
        // CATEGORY INFO
        if (transaction.getCategory() != null) {
            this.categoryName = transaction.getCategory().getName();
            this.categoryId = transaction.getCategory().getId();
        }
        
        // USER INFO
        if (transaction.getUser() != null) {
            this.userName = transaction.getUser().getUserName();
            this.userId = transaction.getUser().getId();
        }
        
        // WALLET INFO
        if (transaction.getWallet() != null) {
            this.walletName = transaction.getWallet().getName();
            this.walletId = transaction.getWallet().getId();
        }
    }

    // GETTERI I SETTERI
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    // DODATNI METODI ZA FORMATIRANJE 
    public String getFormattedAmount() {
        return amount != null ? amount.toString() : "0.00";
    }
    
    public String getFormattedAmountWithCurrency() {
        if (amount != null && currencyCode != null) {
            return amount.toString() + " " + currencyCode;
        }
        return "0.00";
    }
}