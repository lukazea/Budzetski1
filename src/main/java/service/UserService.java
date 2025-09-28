package service;

import dto.UserDto;
import entity.User;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    // ----- KORISNIČKI PROFIL -----

    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen sa ID: " + userId));
        return new UserDto(user);
    }

    public UserDto updateUserProfile(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen sa ID: " + userId));

        if (userDto.getUserName() != null && !userDto.getUserName().equals(user.getUserName())
                && userRepository.existsByUserNameAndIdNot(userDto.getUserName(), userId)) {
            throw new RuntimeException("Korisničko ime već postoji: " + userDto.getUserName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
            throw new RuntimeException("Email adresa već postoji: " + userDto.getEmail());
        }

        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());
        if (userDto.getUserName() != null) user.setUserName(userDto.getUserName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getBirthdate() != null) user.setBirth_date(userDto.getBirthdate());
        if (userDto.getProfilePicPath() != null) user.setProfilePicPath(userDto.getProfilePicPath());
        if (userDto.getCurrency() != null) user.setCurrency(userDto.getCurrency());

        return new UserDto(userRepository.save(user));
    }

    public UserDto partialUpdateUserProfile(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen sa ID: " + userId));

        if (updates.containsKey("userName")) {
            String newUserName = (String) updates.get("userName");
            if (newUserName != null && !newUserName.equals(user.getUserName())
                    && userRepository.existsByUserNameAndIdNot(newUserName, userId)) {
                throw new RuntimeException("Korisničko ime već postoji: " + newUserName);
            }
            user.setUserName(newUserName);
        }

        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (newEmail != null && !newEmail.equals(user.getEmail())
                    && userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                throw new RuntimeException("Email adresa već postoji: " + newEmail);
            }
            user.setEmail(newEmail);
        }

        if (updates.containsKey("firstName")) user.setFirstName((String) updates.get("firstName"));
        if (updates.containsKey("lastName")) user.setLastName((String) updates.get("lastName"));
        if (updates.containsKey("currency")) user.setCurrency((String) updates.get("currency"));
        if (updates.containsKey("profilePicPath")) user.setProfilePicPath((String) updates.get("profilePicPath"));

        return new UserDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Transactional(readOnly = true)
    public boolean isUserNameAvailableForUpdate(String userName, Long currentUserId) {
        return !userRepository.existsByUserNameAndIdNot(userName, currentUserId);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailableForUpdate(String email, Long currentUserId) {
        return !userRepository.existsByEmailAndIdNot(email, currentUserId);
    }

    // ----- ADMIN FUNKCIONALNOST -----

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

    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUserName(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return u;
    }
}
