package dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalProgressDTO {
    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercentage;
    private LocalDate deadline;
    private long daysRemaining;
}