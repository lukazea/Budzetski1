package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import entity.Goal;
import entity.User;
import entity.Wallet;
import entity.Currency;
import entity.GoalStatus;
import jakarta.transaction.Transactional;
import repository.GoalRepository;
import repository.UserRepository;
import repository.WalletRepository;
import repository.CurrencyRepository;

@Service
@Transactional
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private WalletService walletService;

    public Goal createGoal(Long userId, String name, BigDecimal targetAmount, LocalDate deadline, Long walletId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));

        if (deadline.isBefore(LocalDate.now())) {
            throw new RuntimeException("Rok ne može biti u prošlosti!");
        }

        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Ciljni iznos mora biti veći od nule!");
        }

        Wallet wallet;

        if (walletId == null) {
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("Ime novog novčanika je obavezno!");
            }

            Currency defaultCurrency = currencyRepository.findByCurrency("RSD")
                .orElse(currencyRepository.findAll().get(0));

            wallet = walletService.createSavingsWallet(user, name.trim(), defaultCurrency);
        } else {
            wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));

            if (!wallet.getUser().getId().equals(userId)) {
                throw new RuntimeException("Nemate dozvolu da koristite ovaj novčanik!");
            }

            if (!wallet.isSavings()) {
                wallet.setSavings(true);
                walletRepository.save(wallet);
            }
        }

        Goal goal = new Goal();
        goal.setName(name.trim());
        goal.setTargetAmount(targetAmount);
        goal.setDeadline(deadline);
        goal.setCreatedDate(LocalDate.now());
        goal.setWallet(wallet);
        goal.setUser(user);
        goal.setStatus(GoalStatus.ACTIVE);

        return goalRepository.save(goal);
    }

    public BigDecimal calculateProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Cilj nije pronađen!"));

        BigDecimal currentAmount = goal.getWallet().getCurrentBalance();
        BigDecimal targetAmount = goal.getTargetAmount();

        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        }

        if (progressPercentage.compareTo(new BigDecimal("100")) > 0) {
            progressPercentage = new BigDecimal("100.00");
        }

        if (currentAmount.compareTo(targetAmount) >= 0 && goal.getStatus() == GoalStatus.ACTIVE) {
            goal.setStatus(GoalStatus.COMPLETED);
            goalRepository.save(goal);
        }

        return currentAmount;
    }

    // Dobijanje svih ciljeva korisnika
    public List<Goal> getUserGoals(Long userId) {
        return goalRepository.findByUserId(userId);
    }
    
    public BigDecimal calculateRequiredMonthlyAmount(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Cilj nije pronađen!"));
        
        BigDecimal remainingAmount = goal.getTargetAmount()
            .subtract(goal.getWallet().getCurrentBalance());
        
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // Cilj je već postignut
        }
        
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
        
        if (daysRemaining <= 0) {
            return remainingAmount; // Rok je istekao, trebaju svi ostali novci
        }
        
        // Računa mjesečni iznos (aproksimativno 30 dana u mesecu)
        BigDecimal monthsRemaining = new BigDecimal(daysRemaining).divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        
        if (monthsRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return remainingAmount;
        }
        
        return remainingAmount.divide(monthsRemaining, 2, RoundingMode.HALF_UP);
    }
}