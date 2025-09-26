package service;

import dto.TransactionDto;
import dto.StatisticsDto;
import dto.PeriodStatDto;
import dto.TopTransactionsDto;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private CurrencyService currencyService;

    // ----- Opšte metode -----
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public List<Transaction> getAllTransactionsList() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public long countTransactionsByUser(Long userId) {
        return transactionRepository.countByUserId(userId);
    }

    public Page<Transaction> getTransactionsByUser(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    public Page<Transaction> getTransactionsByCategory(Long categoryId, Pageable pageable) {
        return transactionRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Transaction> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount, pageable);
    }

    public Page<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return transactionRepository.findByDateBetween(startDate, endDate, pageable);
    }

    public Page<Transaction> getFilteredTransactions(Long userId, Long categoryId, BigDecimal minAmount,
                                                     BigDecimal maxAmount, LocalDate startDate,
                                                     LocalDate endDate, Pageable pageable) {
        return transactionRepository.findFilteredTransactions(userId, categoryId, minAmount, maxAmount, startDate, endDate, pageable);
    }

    // ----- Kreiranje obične transakcije -----
    public TransactionDto createTransaction(TransactionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        Wallet wallet = walletRepository.findByIdAndUserId(dto.getWalletId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));

        if (category.getUser() != null && !category.getUser().getId().equals(dto.getUserId())) {
            throw new RuntimeException("Nemate pristup ovoj kategoriji");
        }

        Transaction transaction = new Transaction();
        transaction.setName(dto.getName());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setCategory(category);
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setWallet(wallet);
        transaction.setUser(user);
        transaction.setTransfer(false);

        updateWalletBalance(wallet, dto.getAmount(), dto.getType());

        transaction = transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    // ----- Kreiranje transfer transakcije -----
    public TransactionDto createTransfer(TransactionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        Wallet fromWallet = walletRepository.findByIdAndUserId(dto.getFromWalletId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Izvorni novčanik nije pronađen"));

        Wallet toWallet = walletRepository.findByIdAndUserId(dto.getToWalletId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Odredišni novčanik nije pronađen"));

        if (fromWallet.getCurrentBalance().compareTo(dto.getFromAmount()) < 0) {
            throw new RuntimeException("Nedovoljno sredstava u izvornom novčaniku");
        }

        BigDecimal toAmount;
        BigDecimal exchangeRate = BigDecimal.ONE;

        if (!haveSameCurrency(fromWallet, toWallet)) {
            exchangeRate = calculateExchangeRate(fromWallet, toWallet);
            toAmount = dto.getFromAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            toAmount = dto.getFromAmount();
        }

        Transaction transaction = new Transaction();
        transaction.setName(dto.getName() != null ? dto.getName() :
                "Transfer: " + fromWallet.getName() + " -> " + toWallet.getName());
        transaction.setFromWallet(fromWallet);
        transaction.setToWallet(toWallet);
        transaction.setFromAmount(dto.getFromAmount());
        transaction.setToAmount(toAmount);
        transaction.setExchangeRate(exchangeRate);
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setUser(user);
        transaction.setTransfer(true);
        transaction.setType(CategoryType.TROSAK);

        fromWallet.setCurrentBalance(fromWallet.getCurrentBalance().subtract(dto.getFromAmount()));
        toWallet.setCurrentBalance(toWallet.getCurrentBalance().add(toAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        transaction = transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    // ----- Pregled i filtriranje -----
    public List<TransactionDto> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getWalletTransactions(Long walletId, Long userId) {
        walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));

        return transactionRepository.findAllTransactionsByWallet(userId, walletId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getDailyTransactions(Long userId, LocalDate date) {
        return transactionRepository.findDailyTransactions(userId, date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getWeeklyTransactions(Long userId, LocalDate date) {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return transactionRepository.findWeeklyTransactions(userId, startOfWeek, endOfWeek).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getMonthlyTransactions(Long userId, int year, int month) {
        return transactionRepository.findMonthlyTransactions(userId, year, month).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getQuarterlyTransactions(Long userId, int year, int quarter) {
        if (quarter < 1 || quarter > 4) throw new IllegalArgumentException("Kvartal mora biti između 1 i 4");
        return transactionRepository.findQuarterlyTransactions(userId, year, quarter).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ----- Paginacija -----
    public Page<TransactionDto> getTransactionsPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findByUserId(userId, pageable).map(this::convertToDto);
    }

    public Page<TransactionDto> getTransactionsPaginatedByDate(Long userId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    // ----- Ažuriranje i brisanje -----
    public TransactionDto updateTransaction(Long transactionId, TransactionDto dto, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new RuntimeException("Transakcija nije pronađena"));

        if (transaction.isTransfer()) throw new RuntimeException("Transfer transakcije se ne mogu menjati");

        if (transaction.getWallet() != null && transaction.getAmount() != null) {
            reverseWalletBalance(transaction.getWallet(), transaction.getAmount(), transaction.getType());
        }

        transaction.setName(dto.getName());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTransactionDate(dto.getTransactionDate());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
            transaction.setCategory(category);
        }

        if (dto.getWalletId() != null && !dto.getWalletId().equals(transaction.getWallet().getId())) {
            Wallet newWallet = walletRepository.findByIdAndUserId(dto.getWalletId(), userId)
                    .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));
            transaction.setWallet(newWallet);
        }

        updateWalletBalance(transaction.getWallet(), dto.getAmount(), dto.getType());
        transaction = transactionRepository.save(transaction);

        return convertToDto(transaction);
    }

    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new RuntimeException("Transakcija nije pronađena"));

        if (transaction.isTransfer()) {
            if (transaction.getFromWallet() != null) {
                transaction.getFromWallet().setCurrentBalance(
                        transaction.getFromWallet().getCurrentBalance().add(transaction.getFromAmount())
                );
                walletRepository.save(transaction.getFromWallet());
            }
            if (transaction.getToWallet() != null) {
                transaction.getToWallet().setCurrentBalance(
                        transaction.getToWallet().getCurrentBalance().subtract(transaction.getToAmount())
                );
                walletRepository.save(transaction.getToWallet());
            }
        } else {
            reverseWalletBalance(transaction.getWallet(), transaction.getAmount(), transaction.getType());
        }

        transactionRepository.deleteById(transactionId);
    }
    
    // ----- STATISTIKE -----
    
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
            .map(this::convertToDto)
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
                .stream().map(this::convertToDto).collect(Collectors.toList());
        List<TransactionDto> topIncomes = getTopIncomes(userId, startDate, endDate, limit)
                .stream().map(this::convertToDto).collect(Collectors.toList());

        return new TopTransactionsDto(topExpenses, topIncomes);
    }

    // ----- Helper metode -----
    private void updateWalletBalance(Wallet wallet, BigDecimal amount, CategoryType type) {
        if (type == CategoryType.PRIHOD) {
            wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        } else {
            wallet.setCurrentBalance(wallet.getCurrentBalance().subtract(amount));
        }
        walletRepository.save(wallet);
    }

    private void reverseWalletBalance(Wallet wallet, BigDecimal amount, CategoryType type) {
        if (type == CategoryType.PRIHOD) {
            wallet.setCurrentBalance(wallet.getCurrentBalance().subtract(amount));
        } else {
            wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        }
        walletRepository.save(wallet);
    }

    private boolean haveSameCurrency(Wallet wallet1, Wallet wallet2) {
        return wallet1.getCurrencies().stream()
                .anyMatch(c1 -> wallet2.getCurrencies().stream()
                        .anyMatch(c2 -> c1.getCurrency().equals(c2.getCurrency())));
    }

    private BigDecimal calculateExchangeRate(Wallet fromWallet, Wallet toWallet) {
        Currency fromCurrency = fromWallet.getCurrencies().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Izvorni novčanik nema valutu"));
        Currency toCurrency = toWallet.getCurrencies().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Odredišni novčanik nema valutu"));

        BigDecimal fromToEur = BigDecimal.valueOf(fromCurrency.getValueToEur());
        BigDecimal toToEur = BigDecimal.valueOf(toCurrency.getValueToEur());
        return fromToEur.divide(toToEur, 8, RoundingMode.HALF_UP);
    }

    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setName(transaction.getName());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setUserId(transaction.getUser() != null ? transaction.getUser().getId() : null);
        dto.setTransfer(transaction.isTransfer());

        if (transaction.getCategory() != null) {
            dto.setCategoryId(transaction.getCategory().getId());
            dto.setCategoryName(transaction.getCategory().getName());
        }

        if (transaction.getWallet() != null) {
            dto.setWalletId(transaction.getWallet().getId());
            dto.setWalletName(transaction.getWallet().getName());
        }

        if (transaction.isTransfer()) {
            dto.setFromWalletId(transaction.getFromWallet() != null ? transaction.getFromWallet().getId() : null);
            dto.setToWalletId(transaction.getToWallet() != null ? transaction.getToWallet().getId() : null);
            dto.setFromAmount(transaction.getFromAmount());
            dto.setToAmount(transaction.getToAmount());
            dto.setExchangeRate(transaction.getExchangeRate());
        }

        if (transaction.getRecurringTemplate() != null) {
            dto.setRecurringTemplateId(transaction.getRecurringTemplate().getId());
        }

        return dto;
    }
}