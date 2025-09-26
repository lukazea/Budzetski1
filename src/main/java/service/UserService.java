package service;

import entity.User;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // --- Upravljanje korisnicima ---

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik sa ID " + userId + " ne postoji"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik sa ID " + userId + " ne postoji"));
        user.setBlocked(false);
        userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik sa ID " + userId + " ne postoji"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik sa ID " + userId + " ne postoji"));
        userRepository.delete(user);
    }

    // --- Prva funkcionalnost: broj ukupnih registrovanih korisnika ---

    public long getTotalUserCount() {
        return userRepository.count();
    }
}
