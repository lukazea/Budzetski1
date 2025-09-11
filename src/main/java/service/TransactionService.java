package service;

import entity.Transaction;
import entity.User;
import entity.Wallet;
import entity.Category;
import entity.CategoryType;
import dto.*;
import repository.TransactionRepository;
import repository.UserRepository;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private CurrencyService currencyService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Statistike
    public BigDecimal getTotalIncomeForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.getSumByUserIdAndTypeAndDateBetween(
            userId, CategoryType.PRIHOD, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpenseForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.getSumByUserIdAndTypeAndDateBetween(
            userId, CategoryType.TROSAK, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Top transakcije
    public List<Transaction> getTopTransactions(LocalDate since, int limit) {
        return transactionRepository.findTopTransactionsSince(since, 
            PageRequest.of(0, limit));
    }
    
    // Dnevne statistike
    public BigDecimal getDailyIncome(Long userId, LocalDate date) {
        return getTotalIncomeForPeriod(userId, date, date);
    }
    
    public BigDecimal getDailyExpense(Long userId, LocalDate date) {
        return getTotalExpenseForPeriod(userId, date, date);
    }
    
    // Nedeljne statistike
    public BigDecimal getWeeklyIncome(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return getTotalIncomeForPeriod(userId, weekStart, weekEnd);
    }
    
    public BigDecimal getWeeklyExpense(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return getTotalExpenseForPeriod(userId, weekStart, weekEnd);
    }
    
    // Mesečne statistike
    public BigDecimal getMonthlyIncome(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return getTotalIncomeForPeriod(userId, startDate, endDate);
    }
    
    public BigDecimal getMonthlyExpense(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return getTotalExpenseForPeriod(userId, startDate, endDate);
    }
    
    // Godišnje statistike
    public BigDecimal getYearlyIncome(Long userId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return getTotalIncomeForPeriod(userId, startDate, endDate);
    }
    
    public BigDecimal getYearlyExpense(Long userId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return getTotalExpenseForPeriod(userId, startDate, endDate);
    }
    
    // Statistike po kategorijama
    public Map<String, BigDecimal> getCategoryStats(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.getSumByUserIdAndDateBetweenGroupByCategory(
            userId, startDate, endDate);
        
        Map<String, BigDecimal> categoryStats = new HashMap<>();
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryStats.put(categoryName, amount != null ? amount : BigDecimal.ZERO);
        }
        return categoryStats;
    }
    
    public Map<String, BigDecimal> getCategoryStatsByType(Long userId, CategoryType type, 
                                                          LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.getSumByUserIdAndTypeAndDateBetweenGroupByCategory(
            userId, type, startDate, endDate);
        
        Map<String, BigDecimal> categoryStats = new HashMap<>();
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryStats.put(categoryName, amount != null ? amount : BigDecimal.ZERO);
        }
        return categoryStats;
    }
    
    // Top troškovi
    public List<Transaction> getTopExpenses(Long userId, LocalDate startDate, LocalDate endDate, int limit) {
        return transactionRepository.findTopExpensesByUserIdAndDateBetween(
            userId, startDate, endDate, PageRequest.of(0, limit));
    }
    
    // Top prihodi
    public List<Transaction> getTopIncomes(Long userId, LocalDate startDate, LocalDate endDate, int limit) {
        return transactionRepository.findTopIncomeByUserIdAndDateBetween(
            userId, startDate, endDate, PageRequest.of(0, limit));
    }
    
    // Kompletna statistika za period
    public StatisticsDto getCompleteStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = getTotalIncomeForPeriod(userId, startDate, endDate);
        BigDecimal totalExpense = getTotalExpenseForPeriod(userId, startDate, endDate);
        Map<String, BigDecimal> categoryStats = getCategoryStats(userId, startDate, endDate);
        
        // Top transakcije (5 najvećih troškova)
        List<Transaction> topExpenseTransactions = getTopExpenses(userId, startDate, endDate, 5);
        List<TransactionDto> topTransactions = topExpenseTransactions.stream()
            .map(TransactionDto::new)
            .collect(Collectors.toList());
        
        return new StatisticsDto(totalIncome, totalExpense, categoryStats, null, topTransactions);
    }
    
    // Statistike sa periodnim podacima
    public StatisticsDto getStatisticsWithPeriods(Long userId, LocalDate startDate, LocalDate endDate, 
                                                 String periodType) {
        StatisticsDto baseStats = getCompleteStatistics(userId, startDate, endDate);
        List<PeriodStatDto> periodStats = new ArrayList<>();
        
        switch (periodType.toLowerCase()) {
            case "daily":
                periodStats = getDailyPeriodStats(userId, startDate, endDate);
                break;
            case "weekly":
                periodStats = getWeeklyPeriodStats(userId, startDate, endDate);
                break;
            case "monthly":
                periodStats = getMonthlyPeriodStats(userId, startDate, endDate);
                break;
            case "yearly":
                periodStats = getYearlyPeriodStats(userId, startDate, endDate);
                break;
        }
        
        baseStats.setPeriodStats(periodStats);
        return baseStats;
    }
    
    // Helper metodi za period statistike
    private List<PeriodStatDto> getDailyPeriodStats(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PeriodStatDto> periodStats = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BigDecimal income = getDailyIncome(userId, date);
            BigDecimal expense = getDailyExpense(userId, date);
            
            PeriodStatDto periodStat = new PeriodStatDto(
                date.toString(), date, income, expense);
            periodStats.add(periodStat);
        }
        
        return periodStats;
    }
    
    private List<PeriodStatDto> getWeeklyPeriodStats(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PeriodStatDto> periodStats = new ArrayList<>();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        
        LocalDate weekStart = startDate.with(weekFields.dayOfWeek(), 1);
        
        while (!weekStart.isAfter(endDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(endDate)) weekEnd = endDate;
            
            BigDecimal income = getTotalIncomeForPeriod(userId, weekStart, weekEnd);
            BigDecimal expense = getTotalExpenseForPeriod(userId, weekStart, weekEnd);
            
            String weekPeriod = weekStart.getYear() + "-W" + 
                               weekStart.get(weekFields.weekOfYear());
            
            PeriodStatDto periodStat = new PeriodStatDto(
                weekPeriod, weekStart, income, expense);
            periodStats.add(periodStat);
            
            weekStart = weekStart.plusWeeks(1);
        }
        
        return periodStats;
    }
    
    private List<PeriodStatDto> getMonthlyPeriodStats(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PeriodStatDto> periodStats = new ArrayList<>();
        
        LocalDate monthStart = startDate.withDayOfMonth(1);
        
        while (!monthStart.isAfter(endDate)) {
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            if (monthEnd.isAfter(endDate)) monthEnd = endDate;
            
            BigDecimal income = getTotalIncomeForPeriod(userId, monthStart, monthEnd);
            BigDecimal expense = getTotalExpenseForPeriod(userId, monthStart, monthEnd);
            
            String monthPeriod = monthStart.getYear() + "-" + 
                               String.format("%02d", monthStart.getMonthValue());

            PeriodStatDto periodStat = new PeriodStatDto(monthPeriod, monthStart, income, expense);
            periodStats.add(periodStat);

            monthStart = monthStart.plusMonths(1);
        }

        return periodStats;
    }

    private List<PeriodStatDto> getYearlyPeriodStats(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PeriodStatDto> periodStats = new ArrayList<>();

        LocalDate yearStart = LocalDate.of(startDate.getYear(), 1, 1);

        while (!yearStart.isAfter(endDate)) {
            LocalDate yearEnd = LocalDate.of(yearStart.getYear(), 12, 31);
            if (yearEnd.isAfter(endDate)) yearEnd = endDate;

            BigDecimal income = getTotalIncomeForPeriod(userId, yearStart, yearEnd);
            BigDecimal expense = getTotalExpenseForPeriod(userId, yearStart, yearEnd);

            String yearPeriod = String.valueOf(yearStart.getYear());

            PeriodStatDto periodStat = new PeriodStatDto(yearPeriod, yearStart, income, expense);
            periodStats.add(periodStat);

            yearStart = yearStart.plusYears(1);
        }

        return periodStats;
    }
    
    // Transakcije po datumu
    public List<Transaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }

    // Transakcije po kategoriji
    public List<Transaction> getTransactionsByCategory(Long walletId, Category category) {
        Wallet wallet = walletService.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet nije pronađen!"));
        return transactionRepository.findByWalletAndCategory(wallet, category);
    }

    // Transakcije po tipu (PRIHOD / TROSAK)
    public List<Transaction> getTransactionsByType(Long userId, CategoryType type) {
        return transactionRepository.findByUserIdAndType(userId, type);
    }
    
    public TopTransactionsDto getTopTransactionsForUser(Long userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<TransactionDto> topExpenses = getTopExpenses(userId, startDate, endDate, limit)
                .stream().map(TransactionDto::new).toList();
        List<TransactionDto> topIncomes = getTopIncomes(userId, startDate, endDate, limit)
                .stream().map(TransactionDto::new).toList();

        return new TopTransactionsDto(topExpenses, topIncomes);
    }
    
}