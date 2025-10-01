package controller;

import dto.*;
import entity.User;
import entity.User.Role;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import repository.UserRepository;
import utils.JwtUtil;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwt;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder,
                          AuthenticationManager authManager, JwtUtil jwt) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUserName(req.userName))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already exists"));
        if (userRepo.existsByEmail(req.email))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already exists"));

        User u = new User();
        u.setFirstName(req.firstName);
        u.setLastName(req.lastName);
        u.setUserName(req.userName);
        u.setEmail(req.email);
        u.setPassword(encoder.encode(req.password));
        u.setCurrency(req.currency != null ? req.currency : "RSD");
        u.setBirth_date(req.birthdate);
        u.setRole(Role.USER);
        u.setRegistartion_date(LocalDate.now());
        u.setBlocked(false);

        userRepo.save(u);

        String token = jwt.generateToken(u.getUserName(), Map.of("role", "ROLE_" + u.getRole().name(), "uid", u.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, u.getId(), u.getRole().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password));

        var principal = auth.getName(); // username
        var user = userRepo.findByUserName(principal).orElseThrow();
        String token = jwt.generateToken(
                user.getUserName(),
                Map.of("role", "ROLE_" + user.getRole().name(), "uid", user.getId())
        );
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getRole().toString()));
    }
}
