package service;

import dto.UserDto;
import entity.User;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Korisnik nije pronađen sa ID: " + userId);
        }
        return new UserDto(userOptional.get());
    }


    public UserDto updateUserProfile(Long userId, UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Korisnik nije pronađen sa ID: " + userId);
        }

        User user = userOptional.get();

        if (userDto.getUserName() != null &&
                !userDto.getUserName().equals(user.getUserName()) &&
                userRepository.existsByUserNameAndIdNot(userDto.getUserName(), userId)) {
            throw new RuntimeException("Korisničko ime već postoji: " + userDto.getUserName());
        }

        if (userDto.getEmail() != null &&
                !userDto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
            throw new RuntimeException("Email adresa već postoji: " + userDto.getEmail());
        }

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getUserName() != null) {
            user.setUserName(userDto.getUserName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getBirthdate() != null) {
            user.setBirth_date(userDto.getBirthdate());
        }
        if (userDto.getProfilePicPath() != null) {
            user.setProfilePicPath(userDto.getProfilePicPath());
        }
        if (userDto.getCurrency() != null) {
            user.setCurrency(userDto.getCurrency());
        }

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    public UserDto partialUpdateUserProfile(Long userId, Map<String, Object> updates) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Korisnik nije pronađen sa ID: " + userId);
        }

        User user = userOptional.get();

        if (updates.containsKey("userName")) {
            String newUserName = (String) updates.get("userName");
            if (newUserName != null && !newUserName.equals(user.getUserName()) &&
                    userRepository.existsByUserNameAndIdNot(newUserName, userId)) {
                throw new RuntimeException("Korisničko ime već postoji: " + newUserName);
            }
        }

        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (newEmail != null && !newEmail.equals(user.getEmail()) &&
                    userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                throw new RuntimeException("Email adresa već postoji: " + newEmail);
            }
        }

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("userName")) {
            user.setUserName((String) updates.get("userName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("currency")) {
            user.setCurrency((String) updates.get("currency"));
        }
        if (updates.containsKey("profilePicPath")) {
            user.setProfilePicPath((String) updates.get("profilePicPath"));
        }

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
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
}