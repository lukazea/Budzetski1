package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;

import entity.Goal;
import entity.GoalStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GoalDto(
        Long id,
        String name,
        BigDecimal targetAmount,
        LocalDate deadline,
        LocalDate createdDate,
        Long walletId,
        String walletName,
        Long userId,
        GoalStatus status
) {
    public static GoalDto fromGoal(Goal g){
        return new GoalDto(
            g.getId(),
            g.getName(),
            g.getTargetAmount(),
            g.getDeadline(),
            g.getCreatedDate(),
            g.getWallet() != null ? g.getWallet().getId() : null,
            g.getWallet() != null ? g.getWallet().getName() : null,
            g.getUser()   != null ? g.getUser().getId()   : null,
            g.getStatus()
        );
    }
}
