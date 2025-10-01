package dto;

public class AuthResponse {
    public String token;
    public String tokenType = "Bearer";
    public Long id;
    public String role;
    public AuthResponse(String token, Long id, String role) { this.token = token; this.id = id; this.role = role;}
}