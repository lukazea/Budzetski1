package controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dto.StatisticsDto;
import repository.UserRepository;
import service.TransactionService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    // Ovo je ID trenutno ulogovanog korisnika (za test/početak)
    private final Long CURRENT_USER_ID = 1L;

    @GetMapping("/complete")
    public StatisticsDto getCompleteStatistics() {
        // Uvek koristi CURRENT_USER_ID
        return transactionService.getCompleteStatistics(CURRENT_USER_ID,
                LocalDate.now().minusMonths(1), LocalDate.now());
    }

    @GetMapping("/periods")
    public StatisticsDto getStatisticsWithPeriods(@RequestParam LocalDate startDate,
                                                  @RequestParam LocalDate endDate,
                                                  @RequestParam String periodType) {
        return transactionService.getStatisticsWithPeriods(CURRENT_USER_ID, startDate, endDate, periodType);
    }
}
