package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TRANSACTIONS")
public class Transaction implements Serializable {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "recurring_template_id")
    private RecurringTransactionTemplate recurringTemplate;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



    // Dodao za transakcije sa novcanika na novcanik
    @ManyToOne
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @Column(name = "FROM_AMOUNT", precision = 19, scale = 2)
    private BigDecimal fromAmount;

    @Column(name = "TO_AMOUNT", precision = 19, scale = 2)
    private BigDecimal toAmount;

    // dodato ukoliko nisu ista valute

    @Column(name = "EXCHANGE_RATE", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "IS_TRANSFER", nullable = false)
    private boolean isTransfer = false;

    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public RecurringTransactionTemplate getRecurringTemplate() { return recurringTemplate; }
    public void setRecurringTemplate(RecurringTransactionTemplate recurringTemplate) {
        this.recurringTemplate = recurringTemplate;
    }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Wallet getFromWallet() { return fromWallet; }
    public void setFromWallet(Wallet fromWallet) { this.fromWallet = fromWallet; }

    public Wallet getToWallet() { return toWallet; }
    public void setToWallet(Wallet toWallet) { this.toWallet = toWallet; }

    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }

    public BigDecimal getToAmount() { return toAmount; }
    public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }

    // novi geteri i seteri

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public boolean isTransfer() { return isTransfer; }
    public void setTransfer(boolean transfer) { this.isTransfer = transfer; }

    @Override
    public String toString() {
        return "Transaction [id=" + id
                + ", name=" + name
                + ", amount=" + amount
                + ", type=" + type
                + ", category=" + (category != null ? category.getId() : "null")
                + ", recurringTemplate=" + (recurringTemplate != null ? recurringTemplate.getId() : "null")
                + ", transactionDate=" + transactionDate
                + ", wallet=" + (wallet != null ? wallet.getId() : "null")
                + ", user=" + (user != null ? user.getId() : "null")
                + "]";
    }



}
