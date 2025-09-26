package dto;

import java.math.BigDecimal;

public class CategoryStatDto {
	private String categoryName;
    private BigDecimal amount;
    private Double percentage;
    private String type; // "PRIHOD" ili "TROSAK"
    
    public CategoryStatDto() {}
    
    public CategoryStatDto(String categoryName, BigDecimal amount, String type) {
        this.categoryName = categoryName;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.type = type;
    }
    
    // Getters and Setters
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
