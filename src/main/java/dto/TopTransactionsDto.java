package dto;

import java.math.BigDecimal;
import java.util.List;

public class TopTransactionsDto {
	    private List<TransactionDto> topExpenses;
	    private List<TransactionDto> topIncomes;
	    private BigDecimal totalTopExpenses;
	    private BigDecimal totalTopIncomes;
	    
	    public TopTransactionsDto() {}
	    
	    public TopTransactionsDto(List<TransactionDto> topExpenses, List<TransactionDto> topIncomes) {
	        this.topExpenses = topExpenses;
	        this.topIncomes = topIncomes;
	        
	        this.totalTopExpenses = topExpenses.stream()
	            .map(TransactionDto::getAmount)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);
	            
	        this.totalTopIncomes = topIncomes.stream()
	            .map(TransactionDto::getAmount)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);
	    }
	   
	    public List<TransactionDto> getTopExpenses() { return topExpenses; }
	    public void setTopExpenses(List<TransactionDto> topExpenses) { this.topExpenses = topExpenses; }
	    
	    public List<TransactionDto> getTopIncomes() { return topIncomes; }
	    public void setTopIncomes(List<TransactionDto> topIncomes) { this.topIncomes = topIncomes; }
	    
	    public BigDecimal getTotalTopExpenses() { return totalTopExpenses; }
	    public void setTotalTopExpenses(BigDecimal totalTopExpenses) { this.totalTopExpenses = totalTopExpenses; }
	    
	    public BigDecimal getTotalTopIncomes() { return totalTopIncomes; }
	    public void setTotalTopIncomes(BigDecimal totalTopIncomes) { this.totalTopIncomes = totalTopIncomes; }
}
