package dto;

public class AuthResponse {
    public String token;
    public String tokenType = "Bearer";
    public Long id;
    public AuthResponse(String token, Long id) { this.token = token; this.id = id;}
}