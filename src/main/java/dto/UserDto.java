package dto;

import entity.User;
import java.time.LocalDate;

public class UserDto {
    // ----- PROFILE POLJA -----
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private LocalDate birthdate;
    private User.Role role;
    private String profilePicPath;
    private String currency;
    private LocalDate registrationdate;
    private boolean blocked;

    // ----- ADMIN / JAVNI POLJA -----
    private long totalUserCount;

    // ----- KONSTRUKTORI -----
    public UserDto() {}

    // Konstruktor iz entiteta User
    public UserDto(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.birthdate = user.getBirth_date();
        this.role = user.getRole();
        this.profilePicPath = user.getProfilePicPath();
        this.currency = user.getCurrency();
        this.registrationdate = user.getRegistartion_date();
        this.blocked = user.isBlocked();
    }

    // ----- GETTERI I SETTERI -----
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

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }

    public String getProfilePicPath() { return profilePicPath; }
    public void setProfilePicPath(String profilePicPath) { this.profilePicPath = profilePicPath; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getRegistrationdate() { return registrationdate; }
    public void setRegistrationdate(LocalDate registrationdate) { this.registrationdate = registrationdate; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public long getTotalUserCount() { return totalUserCount; }
    public void setTotalUserCount(long totalUserCount) { this.totalUserCount = totalUserCount; }

    @Override
    public String toString() {
        return "UserDto [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName
                + ", userName=" + userName + ", email=" + email + ", birthdate=" + birthdate
                + ", role=" + role + ", profilePicPath=" + profilePicPath + ", currency=" + currency
                + ", registrationdate=" + registrationdate + ", blocked=" + blocked
                + ", totalUserCount=" + totalUserCount + "]";
    }
}
