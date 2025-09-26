package dto;

import entity.Transaction;
import entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionDto {
    private Long id;
    private String name;                // opis transakcije
    private BigDecimal amount;
    private CategoryType type;
    private Long categoryId;
    private String categoryName;
    private LocalDate transactionDate;  // datum transakcije
    private Long walletId;
    private String walletName;
    private Long userId;
    private String userName;
    private String currencyCode;
    private Long currencyId;

    // Za transfer između novčanika
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;
    private boolean isTransfer;

    // Za recurring template
    private Long recurringTemplateId;

    // TIMESTAMP POLJA - potrebna za dashboard
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ----- KONSTRUKTORI -----
    public TransactionDto() {}

    public TransactionDto(Transaction transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.name = transaction.getName();
        this.transactionDate = transaction.getTransactionDate();
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

            if (transaction.getWallet().getCurrencies() != null &&
                !transaction.getWallet().getCurrencies().isEmpty()) {
                this.currencyCode = transaction.getWallet().getCurrencies().iterator().next().getCurrency();
            }
        }

        // Transfer info
        if (transaction.isTransfer()) {
            this.fromWalletId = transaction.getFromWallet() != null ? transaction.getFromWallet().getId() : null;
            this.toWalletId = transaction.getToWallet() != null ? transaction.getToWallet().getId() : null;
            this.fromAmount = transaction.getFromAmount();
            this.toAmount = transaction.getToAmount();
            this.exchangeRate = transaction.getExchangeRate();
            this.isTransfer = true;
        }

        // Recurring template
        if (transaction.getRecurringTemplate() != null) {
            this.recurringTemplateId = transaction.getRecurringTemplate().getId();
        }
    }

    // ----- GETTERI I SETTERI -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId) { this.currencyId = currencyId; }

    public Long getFromWalletId() { return fromWalletId; }
    public void setFromWalletId(Long fromWalletId) { this.fromWalletId = fromWalletId; }

    public Long getToWalletId() { return toWalletId; }
    public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }

    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }

    public BigDecimal getToAmount() { return toAmount; }
    public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public boolean isTransfer() { return isTransfer; }
    public void setTransfer(boolean transfer) { this.isTransfer = transfer; }

    public Long getRecurringTemplateId() { return recurringTemplateId; }
    public void setRecurringTemplateId(Long recurringTemplateId) { this.recurringTemplateId = recurringTemplateId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ----- HELPER METODE -----
    public String getFormattedAmount() {
        return amount != null ? amount.toString() : "0.00";
    }

    public String getFormattedAmountWithCurrency() {
        return amount != null && currencyCode != null ? amount.toString() + " " + currencyCode : "0.00";
    }
}
