package dto;

import entity.User;

public class UserDto {

    private Long id;
    private String userName;
    private String email;
    private boolean blocked;  // polje koje koristimo za blokiranje

    public UserDto() {}

    // Konstruktor koji prihvata entitet User
    public UserDto(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.blocked = user.isBlocked();
    }

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
