package dto;

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
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private int totalGenerated;
    private LocalDate lastGenerated;

    // Prazan konstruktor (neophodan za Jackson / Hibernate / Spring)
    public RecurringTransactionTemplateDto() {}

    // Konstruktor za kreiranje novog šablona (korisnički input)
    public RecurringTransactionTemplateDto(String name, BigDecimal amount, CategoryType type,
                                           Long categoryId, Long walletId, Long userId,
                                           String frequency, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.walletId = walletId;
        this.userId = userId;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;   // podrazumevano aktivno
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

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public int getTotalGenerated() { return totalGenerated; }
    public void setTotalGenerated(int totalGenerated) { this.totalGenerated = totalGenerated; }

    public LocalDate getLastGenerated() { return lastGenerated; }
    public void setLastGenerated(LocalDate lastGenerated) { this.lastGenerated = lastGenerated; }

    @Override
    public String toString() {
        return "RecurringTransactionTemplateDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", categoryId=" + categoryId +
                ", walletId=" + walletId +
                ", userId=" + userId +
                ", frequency='" + frequency + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalGenerated=" + totalGenerated +
                ", lastGenerated=" + lastGenerated +
                ", isActive=" + isActive +
                '}';
    }
}
