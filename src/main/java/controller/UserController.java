package controller;

import dto.*;
import entity.Transaction;
import entity.User;
import entity.UserNote;
import service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserNoteService userNoteService;

    // ----- PROFILE ENDPOINTI -----
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            UserDto userProfile = userService.getUserProfile(userId);
            return ResponseEntity.ok(userProfile);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId,
                                               @RequestBody UserDto userDto) {
        try {
            UserDto updatedUser = userService.updateUserProfile(userId, userDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil je uspešno ažuriran");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            if (e.getMessage().contains("nije pronađen")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (e.getMessage().contains("već postoji")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }
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
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            if (e.getMessage().contains("nije pronađen")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (e.getMessage().contains("već postoji")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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

    // ----- ADMIN ENDPOINTI -----
    @GetMapping("/admin/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PutMapping("/admin/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        try {
            userService.blockUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/admin/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        try {
            userService.unblockUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/{userId}/transactions")
    public ResponseEntity<Page<TransactionDto>> getUserTransactions(
            @PathVariable Long userId,
            Pageable pageable) {
        try {
            Page<Transaction> transactions = transactionService.getTransactionsByUser(userId, pageable);
            return ResponseEntity.ok(transactions.map(TransactionDto::new));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/{userId}/transactions/count")
    public ResponseEntity<Long> getUserTransactionCount(@PathVariable Long userId) {
        try {
            Long count = transactionService.countTransactionsByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/{userId}/notes")
    public ResponseEntity<Page<UserNoteDto>> getUserNotes(
            @PathVariable Long userId,
            Pageable pageable) {
        try {
            Page<UserNote> notes = userNoteService.getNotesByUser(userId, pageable);
            return ResponseEntity.ok(notes.map(UserNoteDto::new));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/notes")
    public ResponseEntity<UserNoteDto> createUserNote(@RequestBody CreateUserNoteDto createDto) {
        try {
            if (createDto.getUserId() == null || createDto.getNote() == null || createDto.getNote().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            String adminUsername = "admin"; // Privremeno hardkodovano
            UserNote note = userNoteService.createNote(createDto, adminUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UserNoteDto(note));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/admin/notes/{noteId}")
    public ResponseEntity<UserNoteDto> updateUserNote(
            @PathVariable Long noteId,
            @RequestBody String newNote) {
        try {
            if (newNote == null || newNote.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            String adminUsername = "admin";
            UserNote note = userNoteService.updateNote(noteId, newNote, adminUsername);
            return ResponseEntity.ok(new UserNoteDto(note));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/admin/notes/{noteId}")
    public ResponseEntity<Void> deleteUserNote(@PathVariable Long noteId) {
        try {
            userNoteService.deleteNote(noteId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/notes/{noteId}")
    public ResponseEntity<UserNoteDto> getUserNote(@PathVariable Long noteId) {
        try {
            UserNote note = userNoteService.getNote(noteId);
            return ResponseEntity.ok(new UserNoteDto(note));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/{userId}/notes/count")
    public ResponseEntity<Long> getUserNotesCount(@PathVariable Long userId) {
        try {
            Long count = userNoteService.countNotesByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ----- JAVNI ENDPOINT -----
    @GetMapping("/public/count")
    public ResponseEntity<UserDto> getTotalUserCount() {
        long totalUsers = userService.getTotalUserCount();
        UserDto response = new UserDto();
        response.setTotalUserCount(totalUsers);
        return ResponseEntity.ok(response);
    }
}
