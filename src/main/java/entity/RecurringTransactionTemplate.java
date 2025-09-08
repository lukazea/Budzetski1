package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "RECURRING_TRANSACTION_TEMPLATES")
public class RecurringTransactionTemplate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "FREQUENCY", nullable = false)
    private String frequency;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive = true;

    // Geteri i Seteri
    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    // za ponavljajuce transakcije

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return "RecurringTransactionTemplate [id=" + id
                + ", name=" + name
                + ", amount=" + amount
                + ", frequency=" + frequency
                + ", isActive=" + isActive
                + "]";
    }
}