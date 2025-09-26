package controller;

import dto.CreateUserNoteDto;
import dto.TransactionDto;
import dto.UserDto;
import dto.UserNoteDto;
import entity.Transaction;
import entity.User;
import entity.UserNote;
import service.TransactionService;
import service.UserNoteService;
import service.UserService;
import service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserNoteService userNoteService;

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
            Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::new);
            return ResponseEntity.ok(transactionDtos);
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
            Page<UserNoteDto> noteDtos = notes.map(UserNoteDto::new);
            return ResponseEntity.ok(noteDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/notes")
    public ResponseEntity<UserNoteDto> createUserNote(
            @RequestBody CreateUserNoteDto createDto) {
        try {
            if (createDto.getUserId() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            if (createDto.getNote() == null || createDto.getNote().trim().isEmpty()) {
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

            String adminUsername = "admin"; // Privremeno hardkodovano
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
