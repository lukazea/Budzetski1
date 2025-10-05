package controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dto.GoalDto;
import entity.Goal;
import entity.CreateGoalRequest;
import service.GoalService;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGoal(@RequestBody CreateGoalRequest request) {
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
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GoalDto>> getUserGoals(@PathVariable Long userId) {
        var goals = goalService.getUserGoals(userId);
        var dtos  = goals.stream().map(GoalDto::fromGoal).toList();
        return ResponseEntity.ok(dtos);
    }

    // Napredak cilja
    @GetMapping("/{goalId}/progress")
    public ResponseEntity<Map<String, Object>> getGoalProgress(@PathVariable Long goalId) {
        BigDecimal progress = goalService.calculateProgress(goalId);
        BigDecimal requiredMonthly = goalService.calculateRequiredMonthlyAmount(goalId);

        return ResponseEntity.ok(Map.of(
            "progress", progress,
            "requiredMonthlyAmount", requiredMonthly
        ));
    }
}