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
    private LocalDate transactionDate;
    private boolean isTransfer;

    public TransactionDto() {}

    public TransactionDto(Transaction transaction) {
        this.id = transaction.getId();
        this.name = transaction.getName();
        this.amount = transaction.getAmount();
        this.type = transaction.getType();
        this.transactionDate = transaction.getTransactionDate();
        this.isTransfer = transaction.isTransfer();
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

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public boolean isTransfer() { return isTransfer; }
    public void setTransfer(boolean transfer) { this.isTransfer = transfer; }
}