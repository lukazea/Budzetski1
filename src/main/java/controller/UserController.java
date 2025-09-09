package controller;

import dto.UserDto;
import service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Javno dostupan endpoint za broj registrovanih korisnika (prva funkcionalnost)
    @GetMapping("/public/count")
    public ResponseEntity<UserDto> getTotalUserCount() {
        long totalUsers = userService.getTotalUserCount();
        UserDto response = new UserDto();
        response.setTotalUserCount(totalUsers);
        return ResponseEntity.ok(response);
    }
}