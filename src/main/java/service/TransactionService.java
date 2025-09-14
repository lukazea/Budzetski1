package service;

import dto.TransactionDto;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
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

    // Kreiranje obične transakcije
    public TransactionDto createTransaction(TransactionDto dto) {
        // Validacija korisnika
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Validacija novčanika
        Wallet wallet = walletRepository.findByIdAndUserId(dto.getWalletId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));

        // Validacija kategorije
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));

        // Provera da li kategorija pripada korisniku ili je predefinisana
        if (category.getUser() != null && !category.getUser().getId().equals(dto.getUserId())) {
            throw new RuntimeException("Nemate pristup ovoj kategoriji");
        }

        // Kreiranje transakcije
        Transaction transaction = new Transaction();
        transaction.setName(dto.getName());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setCategory(category);
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setWallet(wallet);
        transaction.setUser(user);
        transaction.setTransfer(false);

        // Ažuriranje balansa novčanika
        updateWalletBalance(wallet, dto.getAmount(), dto.getType());

        // Čuvanje transakcije
        transaction = transactionRepository.save(transaction);

        return convertToDto(transaction);
    }

    // Kreiranje transfer transakcije između novčanika
    public TransactionDto createTransfer(TransactionDto dto) {
        // Validacija korisnika
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Validacija novčanika
        Wallet fromWallet = walletRepository.findByIdAndUserId(dto.getFromWalletId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Izvorni novčanik nije pronađen"));

        Wallet toWallet = walletRepository.findByIdAndUserId(dto.getToWalletId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Odredišni novčanik nije pronađen"));

        // Provera da li ima dovoljno sredstava
        if (fromWallet.getCurrentBalance().compareTo(dto.getFromAmount()) < 0) {
            throw new RuntimeException("Nedovoljno sredstava u izvornom novčaniku");
        }

        // Izračunavanje konverzije ako su različite valute
        BigDecimal toAmount = dto.getToAmount();
        BigDecimal exchangeRate = BigDecimal.ONE;

        if (!haveSameCurrency(fromWallet, toWallet)) {
            exchangeRate = calculateExchangeRate(fromWallet, toWallet);
            toAmount = dto.getFromAmount().multiply(exchangeRate)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            toAmount = dto.getFromAmount();
        }

        // Kreiranje transfer transakcije
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
        transaction.setType(CategoryType.TROSAK); // Transfer se tretira kao trošak

        // Ažuriranje balansa oba novčanika
        fromWallet.setCurrentBalance(
                fromWallet.getCurrentBalance().subtract(dto.getFromAmount())
        );
        toWallet.setCurrentBalance(
                toWallet.getCurrentBalance().add(toAmount)
        );

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Čuvanje transakcije
        transaction = transactionRepository.save(transaction);

        return convertToDto(transaction);
    }

    // Pregled transakcija korisnika
    public List<TransactionDto> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled transakcija po novčaniku
    public List<TransactionDto> getWalletTransactions(Long walletId, Long userId) {
        // Provera da li novčanik pripada korisniku
        walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));

        List<Transaction> transactions = transactionRepository
                .findAllTransactionsByWallet(userId, walletId);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled po danima
    public List<TransactionDto> getDailyTransactions(Long userId, LocalDate date) {
        List<Transaction> transactions = transactionRepository
                .findDailyTransactions(userId, date);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled po nedeljama
    public List<TransactionDto> getWeeklyTransactions(Long userId, LocalDate date) {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Transaction> transactions = transactionRepository
                .findWeeklyTransactions(userId, startOfWeek, endOfWeek);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled po mesecima
    public List<TransactionDto> getMonthlyTransactions(Long userId, int year, int month) {
        List<Transaction> transactions = transactionRepository
                .findMonthlyTransactions(userId, year, month);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled po kvartalima
    public List<TransactionDto> getQuarterlyTransactions(Long userId, int year, int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Kvartal mora biti između 1 i 4");
        }

        List<Transaction> transactions = transactionRepository
                .findQuarterlyTransactions(userId, year, quarter);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Pregled sa paginacijom
    public Page<TransactionDto> getTransactionsPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactionPage = transactionRepository.findByUserId(userId, pageable);
        return transactionPage.map(this::convertToDto);
    }

    // Pregled sa paginacijom i filterom po datumu
    public Page<TransactionDto> getTransactionsPaginatedByDate(
            Long userId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactionPage = transactionRepository
                .findByUserIdAndTransactionDateBetween(userId, startDate, endDate, pageable);
        return transactionPage.map(this::convertToDto);
    }

    // Ažuriranje transakcije
    public TransactionDto updateTransaction(Long transactionId, TransactionDto dto, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new RuntimeException("Transakcija nije pronađena"));

        // Ako je transfer, ne dozvoljavamo menjanje
        if (transaction.isTransfer()) {
            throw new RuntimeException("Transfer transakcije se ne mogu menjati");
        }

        // Vraćanje starog balansa
        if (transaction.getWallet() != null && transaction.getAmount() != null) {
            reverseWalletBalance(transaction.getWallet(), transaction.getAmount(), transaction.getType());
        }

        // Ažuriranje podataka
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

        // Ažuriranje novog balansa
        updateWalletBalance(transaction.getWallet(), dto.getAmount(), dto.getType());

        transaction = transactionRepository.save(transaction);
        return convertToDto(transaction);
    }

    // Brisanje transakcije
    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new RuntimeException("Transakcija nije pronađena"));

        // Vraćanje balansa
        if (transaction.isTransfer()) {
            // Vraćanje balansa za transfer
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
            // Vraćanje balansa za običnu transakciju
            reverseWalletBalance(transaction.getWallet(), transaction.getAmount(), transaction.getType());
        }

        transactionRepository.deleteById(transactionId);
    }

    // Pomoćne metode
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
        // Proveravamo da li oba novčanika imaju istu valutu
        return wallet1.getCurrencies().stream()
                .anyMatch(c1 -> wallet2.getCurrencies().stream()
                        .anyMatch(c2 -> c1.getCurrency().equals(c2.getCurrency())));
    }

    private BigDecimal calculateExchangeRate(Wallet fromWallet, Wallet toWallet) {
        // Uzimamo prvu valutu iz svakog novčanika za konverziju
        Currency fromCurrency = fromWallet.getCurrencies().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Izvorni novčanik nema valutu"));

        Currency toCurrency = toWallet.getCurrencies().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Odredišni novčanik nema valutu"));

        // Konverzija preko EUR
        BigDecimal fromToEur = BigDecimal.valueOf(fromCurrency.getValueToEur());
        BigDecimal toToEur = BigDecimal.valueOf(toCurrency.getValueToEur());

        // exchangeRate = fromToEur / toToEur
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
            dto.setFromWalletId(transaction.getFromWallet() != null ?
                    transaction.getFromWallet().getId() : null);
            dto.setToWalletId(transaction.getToWallet() != null ?
                    transaction.getToWallet().getId() : null);
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