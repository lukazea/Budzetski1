package controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import entity.Goal;
import entity.CreateGoalRequest;
import service.GoalService;
import dto.GoalProgressDTO;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    // Kreiranje cilja
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGoal(@RequestBody CreateGoalRequest request) {
        try {
            // request u parametre
            Goal goal = goalService.createGoal(
                request.getUserId(),
                request.getName(),
                request.getTargetAmount(),
                request.getDeadline(),
                request.getWalletId()
            );

            BigDecimal progress = goalService.calculateProgress(goal.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Cilj je uspješno kreiran!",
                "goal", goal,
                "progress", progress
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Greška pri kreiranju cilja"
            ));
        }
    }

    // Dohvat svih ciljeva korisnika
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserGoals(@PathVariable Long userId) {
        try {
            List<Goal> goals = goalService.getUserGoals(userId);

            return ResponseEntity.ok(Map.of(
                "goals", goals,
                "totalGoals", goals.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Greška pri dohvaćanju ciljeva"
            ));
        }
    }

    // Napredak cilja
    @GetMapping("/{goalId}/progress")
    public ResponseEntity<Map<String, Object>> getGoalProgress(@PathVariable Long goalId) {
        try {
            BigDecimal progress = goalService.calculateProgress(goalId);
            BigDecimal requiredMonthly = goalService.calculateRequiredMonthlyAmount(goalId);

            return ResponseEntity.ok(Map.of(
                "progress", progress,
                "requiredMonthlyAmount", requiredMonthly
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Greška pri računanju napretka"
            ));
        }
    }
}