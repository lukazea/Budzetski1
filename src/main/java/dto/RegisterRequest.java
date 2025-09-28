package dto;

import java.time.LocalDate;

public class RegisterRequest {
    public String firstName;
    public String lastName;
    public String userName;
    public String email;
    public String password;
    public String currency; // e.g., "RSD"
    public LocalDate birthdate; // ISO-8601 "YYYY-MM-DD"
}