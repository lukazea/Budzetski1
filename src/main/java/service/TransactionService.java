package service;

import dto.TransactionDto;
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
import java.util.List;
import java.util.Optional;
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
