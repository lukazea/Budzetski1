package service;

import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Prva funkcionalnost - broj ukupnih registrovanih korisnika
    public long getTotalUserCount() {
        return userRepository.count();
    }
}