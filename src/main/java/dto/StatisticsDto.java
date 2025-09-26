package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Glavni DTO za statistike
public class StatisticsDto {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private Map<String, BigDecimal> categoryStats;
    private List<PeriodStatDto> periodStats;
    private List<TransactionDto> topTransactions;
    
    public StatisticsDto() {}
    
    public StatisticsDto(BigDecimal totalIncome, BigDecimal totalExpense, 
                        Map<String, BigDecimal> categoryStats, 
                        List<PeriodStatDto> periodStats, 
                        List<TransactionDto> topTransactions) {
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        this.balance = this.totalIncome.subtract(this.totalExpense);
        this.categoryStats = categoryStats;
        this.periodStats = periodStats;
        this.topTransactions = topTransactions;
    }
    
    // Getters and Setters
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public Map<String, BigDecimal> getCategoryStats() { return categoryStats; }
    public void setCategoryStats(Map<String, BigDecimal> categoryStats) { this.categoryStats = categoryStats; }
    
    public List<PeriodStatDto> getPeriodStats() { return periodStats; }
    public void setPeriodStats(List<PeriodStatDto> periodStats) { this.periodStats = periodStats; }
    
    public List<TransactionDto> getTopTransactions() { return topTransactions; }
    public void setTopTransactions(List<TransactionDto> topTransactions) { this.topTransactions = topTransactions; }
}
