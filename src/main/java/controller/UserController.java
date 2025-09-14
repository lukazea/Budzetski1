package controller;

import dto.UserDto;
import service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            UserDto userProfile = userService.getUserProfile(userId);
            return ResponseEntity.ok(userProfile);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId,
                                               @RequestBody UserDto userDto) {
        try {
            // Osnovna validacija
            if (userDto.getFirstName() != null && userDto.getFirstName().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Ime ne može biti prazno");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (userDto.getLastName() != null && userDto.getLastName().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Prezime ne može biti prazno");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (userDto.getUserName() != null && userDto.getUserName().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Korisničko ime ne može biti prazno");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (userDto.getEmail() != null &&
                    (!userDto.getEmail().contains("@") || userDto.getEmail().trim().isEmpty())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Neispravna email adresa");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            UserDto updatedUser = userService.updateUserProfile(userId, userDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil je uspešno ažuriran");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            if (e.getMessage().contains("nije pronađen")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (e.getMessage().contains("već postoji")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }
    }


    @GetMapping("/{userId}/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(
            @PathVariable Long userId,
            @RequestParam String userName) {

        boolean available = userService.isUserNameAvailableForUpdate(userName, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{userId}/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @PathVariable Long userId,
            @RequestParam String email) {

        boolean available = userService.isEmailAvailableForUpdate(email, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{userId}/profile")
    public ResponseEntity<?> partialUpdateUserProfile(@PathVariable Long userId,
                                                      @RequestBody Map<String, Object> updates) {
        try {
            UserDto updatedUser = userService.partialUpdateUserProfile(userId, updates);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil je uspešno ažuriran");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            if (e.getMessage().contains("nije pronađen")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (e.getMessage().contains("već postoji")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }
    }
}