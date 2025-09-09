package controller;

import dto.UserDto;
import entity.User;
import service.UserService;
import service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;

    // Registracija novog korisnika
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        try {
            User user = new User();
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setUserName(userDto.getUserName());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setBirth_date(userDto.getBirthDate());
            user.setCurrency(userDto.getCurrency());
            
            User savedUser = userService.registerUser(user);
            
            // Kreiranje predefinisanih kategorija za novog korisnika
            categoryService.createDefaultCategories(savedUser);
            
            return ResponseEntity.ok(new UserDto(savedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Prijava korisnika
    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@RequestBody LoginRequest loginRequest) {
        return userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword())
            .map(user -> ResponseEntity.ok(new UserDto(user)))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null));
    }

    // Preuzmi korisnika po ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return userService.findById(userId)
            .map(user -> ResponseEntity.ok(new UserDto(user)))
            .orElse(ResponseEntity.notFound().build());
    }

    // Preuzmi korisnika po email-u
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
            .map(user -> ResponseEntity.ok(new UserDto(user)))
            .orElse(ResponseEntity.notFound().build());
    }

    // Ažuriranje profila korisnika
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUserProfile(@PathVariable Long userId, @RequestBody UserDto userDto) {
        try {
            User updatedUser = new User();
            updatedUser.setFirstName(userDto.getFirstName());
            updatedUser.setLastName(userDto.getLastName());
            updatedUser.setBirth_date(userDto.getBirthDate());
            updatedUser.setProfilePicPath(userDto.getProfilePicPath());
            updatedUser.setCurrency(userDto.getCurrency());
            
            User user = userService.updateUserProfile(userId, updatedUser);
            return ResponseEntity.ok(new UserDto(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Promena lozinke
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId, 
            @RequestBody PasswordChangeRequest passwordRequest) {
        try {
            userService.changePassword(userId, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === ADMIN ENDPOINTS ===
    
    // Svi korisnici (admin)
    @GetMapping("/admin/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
            .map(UserDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    // Blokiranje korisnika (admin)
    @PutMapping("/admin/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        try {
            userService.blockUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Deblokiranje korisnika (admin)
    @PutMapping("/admin/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        try {
            userService.unblockUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Statistike - ukupan broj korisnika
    @GetMapping("/admin/stats/total")
    public ResponseEntity<Long> getTotalUserCount() {
        return ResponseEntity.ok(userService.getTotalUserCount());
    }

    // Statistike - broj aktivnih korisnika
    @GetMapping("/admin/stats/active")
    public ResponseEntity<Long> getActiveUserCount() {
        return ResponseEntity.ok(userService.getActiveUserCount());
    }

    // DTO klasе za request body
    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}