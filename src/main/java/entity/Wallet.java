package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.math.BigDecimal;

@Entity
@Table(name = "WALLETS")
public class Wallet implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "INITIAL_BALANCE", nullable = false)
	private BigDecimal initialBalance;

	@Column(name = "CURRENT_BALANCE", nullable = false)
	private BigDecimal currentBalance;
    
	//Vise novcanika moze imati vise valuta
	@ManyToMany
	@JoinTable(
		name = "WALLET_CURRENCY",
		joinColumns = @JoinColumn(name = "WALLET_ID", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "CURRENCY_ID", referencedColumnName = "id")
		)
		private Set<Currency> currencies = new HashSet<>();
    
	@Column(name = "CREATION_DATE", nullable = false)
		private LocalDate creationDate;
	    
	//Povezanost sa Userom
	//EAGER-odmah ucitaj vezu sa objektom
	@ManyToOne(fetch = FetchType.EAGER) //necu cascade jer ako mi se obrise novcanik hocu da ostane korisnik
	@JoinColumn(name = "USER_ID", nullable = false) //U tabeli WALLETS postoji kolona USER_ID koja pokazuje na ID u tabeli USERS.
		private User user;

	@Column(name = "SAVINGS", nullable = false)
		private boolean savings;

	@Column(name = "ARCHIVED", nullable = false)
		private boolean archived;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getInitialBalance() {
		return initialBalance;
 	}

	public void setInitialBalance(BigDecimal initialBalance) {
		this.initialBalance = initialBalance;
	}

	public BigDecimal getCurrentBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(BigDecimal currentBalance) {
		this.currentBalance = currentBalance;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}
    
	//set i get kada je ManyToOne je "obican"
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isSavings() {
		return savings;
	}

	public void setSavings(boolean savings) {
		this.savings = savings;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
    
	public Set<Currency> getCurrencies() { 
		return currencies; 
	}
    
	public void setCurrencies(Set<Currency> currencies) {
		this.currencies = currencies; 
	}

	@OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Transaction> transactions = new HashSet<>();

	@Override
	public String toString() {
		return "Wallet [id=" + id + ", name=" + name + ", initialBalance=" + initialBalance + ", currentBalance="
				+ currentBalance + ", currencies=" + currencies + ", creationDate=" + creationDate + ", user=" + user
				+ ", savings=" + savings + ", archived=" + archived + "]";
	}
	
	//dodajem
	@OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Goal> goals;
}

