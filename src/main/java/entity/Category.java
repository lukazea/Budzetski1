package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name ="CATEGORIES")
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)   // Ili prihod ili trosak
    @Column(name = "TYPE", nullable = false)
    private CategoryType type;

    @Column(name = "PREDEFINED", nullable = false)
    private boolean predefined;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = true)
    private User user;

    @OneToMany(mappedBy = "category",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

    public Category() {}

    public Category(String name, CategoryType type, boolean predefined, User user) {
        this.name = name;
        this.type = type;
        this.predefined = predefined;
        this.user = user
        ;
    }

    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public boolean isPredefined() { return predefined; }
    public void setPredefined(boolean predefined) { this.predefined = predefined; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<Transaction> getTransactions() { return transactions; }
    public void setTransactions(Set<Transaction> transactions) { this.transactions = transactions; }

    @Override
    public String toString() {
        return "Category [id=" + id
                + ", name=" + name
                + ", type=" + type
                + ", predefined=" + predefined //
                + ", user=" + (user != null ? user.getId() : "null")
                + ", transactions=" + transactions.size()
                + "]";
    }


}
