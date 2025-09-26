package service;

import dto.CreateUserNoteDto;
import entity.User;
import entity.UserNote;
import repository.UserNoteRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserNoteService {

    @Autowired
    private UserNoteRepository userNoteRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Kreiranje nove beleške
    public UserNote createNote(CreateUserNoteDto createDto, String adminUsername) {
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        
        UserNote note = new UserNote(user, createDto.getNote(), adminUsername);
        return userNoteRepository.save(note);
    }

    // Ažuriranje beleške
    public UserNote updateNote(Long noteId, String newNote, String adminUsername) {
        UserNote note = userNoteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Beleška nije pronađena!"));
        
        note.setNote(newNote);
        note.setUpdatedDate(LocalDateTime.now());
        note.setUpdatedBy(adminUsername);
        
        return userNoteRepository.save(note);
    }

    // Brisanje beleške
    public void deleteNote(Long noteId) {
        if (!userNoteRepository.existsById(noteId)) {
            throw new RuntimeException("Beleška nije pronađena!");
        }
        userNoteRepository.deleteById(noteId);
    }

    // Sve beleške za korisnika (paginated)
    public Page<UserNote> getNotesByUser(Long userId, Pageable pageable) {
        return userNoteRepository.findByUserIdOrderByCreatedDateDesc(userId, pageable);
    }

    // Sve beleške za korisnika (lista)
    public List<UserNote> getNotesByUser(Long userId) {
        return userNoteRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    // Jedna beleška
    public UserNote getNote(Long noteId) {
        return userNoteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Beleška nije pronađena!"));
    }

    // Broj beleški za korisnika
    public Long countNotesByUser(Long userId) {
        return userNoteRepository.countByUserId(userId);
    }
}
