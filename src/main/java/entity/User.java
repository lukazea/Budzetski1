package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "USERS")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Long je wrapper klasa (objektni tip) koja može biti i null

    public enum Role {
        USER,
        ADMIN
    }

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // u service radi hesh

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "profile_pic", nullable = true)
    private String profilePicPath;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationdate;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    // --- Relacije ---
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Wallet> wallets = new HashSet<>();

    // --- Getteri/Setteri ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getBirth_date() { return birthdate; }
    public void setBirth_date(LocalDate birth_date) { this.birthdate = birth_date; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getProfilePicPath() { return profilePicPath; }
    public void setProfilePicPath(String profilePicPath) { this.profilePicPath = profilePicPath; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getRegistartion_date() { return registrationdate; }
    public void setRegistartion_date(LocalDate registartion_date) { this.registrationdate = registartion_date; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public Set<Wallet> getWallets() { return wallets; }
    public void setWallets(Set<Wallet> wallets) { this.wallets = wallets; }

    // --- UserDetails implementacija ---

    @Override
    public String getUsername() { // mapira se na userName kolonu
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !this.blocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return !this.blocked; }

    @Override
    public String toString() {
        return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", userName=" + userName
                + ", email=" + email + ", password=" + password + ", birthdate=" + birthdate + ", role=" + role
                + ", profilePicPath=" + profilePicPath + ", currency=" + currency + ", registrationdate="
                + registrationdate + ", blocked=" + blocked + ", wallets=" + wallets + "]";
    }
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role.name() : "USER")));
	}
}