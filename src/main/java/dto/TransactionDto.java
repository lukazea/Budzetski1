package dto;

import entity.Transaction;
import entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {
    private Long id;
    private String name;
    private BigDecimal amount;
    private CategoryType type;
    private String categoryName;
    private Long categoryId;
    private LocalDate transactionDate;
    private Long walletId;
    private String walletName;
    private Long userId;
    private String userName;
    private boolean isTransfer;
    private Long fromWalletId;
    private String fromWalletName;
    private Long toWalletId;
    private String toWalletName;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;
    private Long recurringTemplateId;

    public TransactionDto() {}

    public TransactionDto(Transaction transaction) {
        this.id = transaction.getId();
        this.name = transaction.getName();
        this.amount = transaction.getAmount();
        this.type = transaction.getType();
        this.transactionDate = transaction.getTransactionDate();
        this.isTransfer = transaction.isTransfer();
        this.fromAmount = transaction.getFromAmount();
        this.toAmount = transaction.getToAmount();
        this.exchangeRate = transaction.getExchangeRate();
        
        if (transaction.getCategory() != null) {
            this.categoryId = transaction.getCategory().getId();
            this.categoryName = transaction.getCategory().getName();
        }
        
        if (transaction.getWallet() != null) {
            this.walletId = transaction.getWallet().getId();
            this.walletName = transaction.getWallet().getName();
        }
        
        if (transaction.getUser() != null) {
            this.userId = transaction.getUser().getId();
            this.userName = transaction.getUser().getUserName();
        }
        
        if (transaction.getFromWallet() != null) {
            this.fromWalletId = transaction.getFromWallet().getId();
            this.fromWalletName = transaction.getFromWallet().getName();
        }
        
        if (transaction.getToWallet() != null) {
            this.toWalletId = transaction.getToWallet().getId();
            this.toWalletName = transaction.getToWallet().getName();
        }
        
        if (transaction.getRecurringTemplate() != null) {
            this.recurringTemplateId = transaction.getRecurringTemplate().getId();
        }
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
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
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
    
    public boolean isTransfer() { return isTransfer; }
    public void setTransfer(boolean transfer) { isTransfer = transfer; }
    
    public Long getFromWalletId() { return fromWalletId; }
    public void setFromWalletId(Long fromWalletId) { this.fromWalletId = fromWalletId; }
    
    public String getFromWalletName() { return fromWalletName; }
    public void setFromWalletName(String fromWalletName) { this.fromWalletName = fromWalletName; }
    
    public Long getToWalletId() { return toWalletId; }
    public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }
    
    public String getToWalletName() { return toWalletName; }
    public void setToWalletName(String toWalletName) { this.toWalletName = toWalletName; }
    
    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }
    
    public BigDecimal getToAmount() { return toAmount; }
    public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }
    
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    
    public Long getRecurringTemplateId() { return recurringTemplateId; }
    public void setRecurringTemplateId(Long recurringTemplateId) { this.recurringTemplateId = recurringTemplateId; }
}