package dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PeriodStatDto {
	    private String period; // "2024-01", "2024-W03", "2024-01-15"
	    private LocalDate date;
	    private BigDecimal income;
	    private BigDecimal expense;
	    private BigDecimal balance;
	    
	    public PeriodStatDto() {}
	    
	    public PeriodStatDto(String period, LocalDate date, BigDecimal income, BigDecimal expense) {
	        this.period = period;
	        this.date = date;
	        this.income = income != null ? income : BigDecimal.ZERO;
	        this.expense = expense != null ? expense : BigDecimal.ZERO;
	        this.balance = this.income.subtract(this.expense);
	    }

	    public String getPeriod() { return period; }
	    public void setPeriod(String period) { this.period = period; }
	    
	    public LocalDate getDate() { return date; }
	    public void setDate(LocalDate date) { this.date = date; }
	    
	    public BigDecimal getIncome() { return income; }
	    public void setIncome(BigDecimal income) { this.income = income; }
	    
	    public BigDecimal getExpense() { return expense; }
	    public void setExpense(BigDecimal expense) { this.expense = expense; }
	    
	    public BigDecimal getBalance() { return balance; }
	    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
