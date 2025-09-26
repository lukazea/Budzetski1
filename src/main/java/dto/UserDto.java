package dto;

import entity.User;

public class UserDto {

    // Polja iz admin_korisnici
    private Long id;
    private String userName;
    private String email;
    private boolean blocked;

    // Polje iz main
    private long totalUserCount;

    // ----- KONSTRUKTORI -----
    public UserDto() {}

    // Konstruktor koji prihvata entitet User
    public UserDto(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.blocked = user.isBlocked();
    }

    // ----- GETTERI I SETTERI -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public long getTotalUserCount() { return totalUserCount; }
    public void setTotalUserCount(long totalUserCount) { this.totalUserCount = totalUserCount; }
}
