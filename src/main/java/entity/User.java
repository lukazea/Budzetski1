package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "USERS")
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	//Long je wrapper klasa (objektni tip) koja može biti i null
	
	public enum Role {
		USER,
		ADMIN
	}

	/*Korisnik 
	● Ime 
	● Prezime 
	● Korisničko ime (jedinstveno) 
	● Mejl adresa (jedinstvena) 
	● Lozinka (heširana) 
	● Datum rođenja 
	● Uloga (Korisnik, Administrator) 
	● Profilna slika (putanja do slike) 
	● Valuta (RSD, EUR, USD...) 
	● Datum registracije 
	● Blokiran (boolean) */
	
	//pravim kolonu u tabeli, za sve ocu da imaju ime, da li vrednost moze biti NULL (nullable), i da li vrednost mora biti jedinstvena (unique)
	@Column(name = "FIRST_NAME", nullable = false)
		private String firstName;
	
	@Column(name = "LAST_NAME", nullable = false)
		private String lastName;
	
	@Column(name = "USER_NAME", nullable = false, unique = true)
		private String userName;
	
	@Column(name = "EMAIL", nullable = false, unique = true)
		private String email;
	
	@Column(name = "PASSWORD", nullable = false)
		private String password;	//u service radi hesh
	
	@Column(name = "BIRTH_DATE", nullable = false)
		private LocalDate birthdate;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE", nullable = false)
		private Role role;
	
	@Column(name = "PROFIL_PIC", nullable = true)
		private String profilePicPath;
	
	@Column(name = "CURRENCY", nullable = false)
		private String currency;
	
	@Column(name = "REGISTRATION_DATE", nullable = false)
		private LocalDate registrationdate;
	
	@Column(name = "BLOCKED", nullable = false)
		private boolean blocked;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public LocalDate getBirth_date() {
		return birthdate;
	}

	public void setBirth_date(LocalDate birth_date) {
		this.birthdate = birth_date;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getProfilePicPath() {
		return profilePicPath;
	}

	public void setProfilePicPath(String profilePicPath) {
		this.profilePicPath = profilePicPath;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public LocalDate getRegistartion_date() {
		return registrationdate;
	}

	public void setRegistartion_date(LocalDate registartion_date) {
		this.registrationdate = registartion_date;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	
	/* Korisnik moze da ima vise novcanika.
	 * Prva veza je u Klasi User. Druga u Wallet.
	 * Posto User moze imati vise novcanika - OneToMany. U Wallet - ManyToOne.
	 * FetchType=LAZY-tek kad eksplicitno trazimo 
	 * CascadeType.ALL-izmena usera = izmena walleta
	 * Treba mi i pored konekcije: get(vraca kolekciju) i set(postavlja kolekciju)
	 */

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Category> categories = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Transaction> transactions = new HashSet<>();


	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Wallet> wallets = new HashSet<>();
	public Set<Wallet> getWallets() {
		return wallets;
	}
	public void setWallets(Set<Wallet> wallets) {
		this.wallets = wallets;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", userName=" + userName
				+ ", email=" + email + ", password=" + password + ", birthdate=" + birthdate + ", role=" + role
				+ ", profilePicPath=" + profilePicPath + ", currency=" + currency + ", registrationdate="
				+ registrationdate + ", blocked=" + blocked + ", wallets=" + wallets + "]";
	}
}