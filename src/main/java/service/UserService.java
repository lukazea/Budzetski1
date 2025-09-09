package service;

import entity.User;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Registracija novog korisnika
    public User registerUser(User user) {
        // Proveri da li već postoji korisnik sa tim email-om ili username-om
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email već postoji!");
        }
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("Korisničko ime već postoji!");
        }
        
        // Hash lozinke
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Postaviti default vrednosti
        user.setRegistartion_date(LocalDate.now());
        user.setRole(User.Role.USER); // Default uloga
        user.setBlocked(false);
        
        return userRepository.save(user);
    }

    // Autentifikacija korisnika
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isBlocked() && passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // Pronađi korisnika po ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Pronađi korisnika po email-u
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Ažuriranje profila korisnika
    public User updateUserProfile(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setBirth_date(updatedUser.getBirth_date());
        user.setProfilePicPath(updatedUser.getProfilePicPath());
        user.setCurrency(updatedUser.getCurrency());
        
        return userRepository.save(user);
    }

    // Promena lozinke
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Stara lozinka nije ispravna!");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Admin funkcionalnosti
    public List<User> getAllUsers() {
        return userRepository.findAllOrderByRegistrationDateDesc();
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        user.setBlocked(false);
        userRepository.save(user);
    }

    // Statistike
    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }
}