package dto;

import entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {
    private Long id;
    private String name;
    private BigDecimal amount;
    private CategoryType type;
    private Long categoryId;
    private String categoryName;
    private LocalDate transactionDate;
    private Long walletId;
    private String walletName;
    private Long userId;

    // Za transfer između novčanika
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;
    private boolean isTransfer;

    // Za recurring template
    private Long recurringTemplateId;

    // Konstruktori
    public TransactionDto() {}

    // Konstruktor za običnu transakciju
    public TransactionDto(String name, BigDecimal amount, CategoryType type,
                          Long categoryId, LocalDate transactionDate, Long walletId, Long userId) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.transactionDate = transactionDate;
        this.walletId = walletId;
        this.userId = userId;
        this.isTransfer = false;
    }

    // Konstruktor za transfer
    public TransactionDto(String name, Long fromWalletId, Long toWalletId,
                          BigDecimal fromAmount, BigDecimal toAmount,
                          LocalDate transactionDate, Long userId) {
        this.name = name;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.transactionDate = transactionDate;
        this.userId = userId;
        this.isTransfer = true;
    }

    // Getteri i setteri
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
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
    public void setRecurringTemplateId(Long recurringTemplateId) {
        this.recurringTemplateId = recurringTemplateId;
    }
}