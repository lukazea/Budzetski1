package dto;

import entity.RecurringTransactionTemplate;
import entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringTransactionTemplateDto {
    private Long id;
    private String name;
    private BigDecimal amount;
    private CategoryType type;
    private Long categoryId;
    private String categoryName;
    private Long walletId;
    private String walletName;
    private Long userId;
    private String userName;
    private String frequency;
    private LocalDate startDate;
    private boolean isActive;

    public RecurringTransactionTemplateDto() {}

    public RecurringTransactionTemplateDto(RecurringTransactionTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.amount = template.getAmount();
        this.type = template.getType();
        this.frequency = template.getFrequency();
        this.startDate = template.getStartDate();
        this.isActive = template.isActive();
        
        if (template.getCategory() != null) {
            this.categoryId = template.getCategory().getId();
            this.categoryName = template.getCategory().getName();
        }
        
        if (template.getWallet() != null) {
            this.walletId = template.getWallet().getId();
            this.walletName = template.getWallet().getName();
        }
        
        if (template.getUser() != null) {
            this.userId = template.getUser().getId();
            this.userName = template.getUser().getUserName();
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
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    
    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}