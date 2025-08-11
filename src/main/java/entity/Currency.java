package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CURRENCIES")
public class Currency implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Naziv valute
	@Column(name = "CURRENCY", nullable = false)
	private String currency;

	// Vrednost u odnosu na EUR (RSD = 117.5)
	@Column(name = "VALUE_TO_EURO", nullable = false)
	private float valueToEur;
    
	public Currency() {} //za euro ako je vec euro

	public Currency(String currency, float valueToEur) { //za ostale valute 
		this.currency = currency;
		this.valueToEur = valueToEur;
	}
    
	//Vise valuta, vise novcanika
	@ManyToMany(mappedBy = "currencies")
	private Set<Wallet> wallets = new HashSet<>(); //hash ne list da se izbegne NullPointer

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public float getValueToEur() {
		return valueToEur;
	}

	public void setValueToEur(float valueToEur) {
		this.valueToEur = valueToEur;
	}
	
	public Set<Wallet> getWallets() { 
		return wallets; 
	}
    
	public void setWallets(Set<Wallet> wallets) {
		this.wallets = wallets; 
	}

	@Override
	public String toString() {
		return "Currency [id=" + id + ", currency=" + currency + ", valueToEur=" + valueToEur + ", wallets=" + wallets
				+ "]";
	}
}
