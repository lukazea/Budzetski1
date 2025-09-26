package service;

import dto.RecurringTransactionTemplateDto;
import entity.*;
import repository.RecurringTransactionTemplateRepository;
import repository.UserRepository;
import repository.WalletRepository;
import repository.CategoryRepository;
import repository.TransactionRepository;
import entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecurringTransactionTemplateService {

    @Autowired
    private RecurringTransactionTemplateRepository recurringRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Kreira novi template za ponavljajuće transakcije
     */
    public RecurringTransactionTemplateDto createRecurringTemplate(RecurringTransactionTemplateDto dto) {
        // Validacija korisnika
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Validacija novčanika
        Wallet wallet = walletRepository.findById(dto.getWalletId())
                .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));

        // Proveri da li novčanik pripada korisniku
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Novčanik ne pripada korisniku");
        }

        // Validacija kategorije
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));

        // Kreiraj novi template
        RecurringTransactionTemplate template = new RecurringTransactionTemplate();
        template.setName(dto.getName());
        template.setAmount(dto.getAmount());
        template.setType(dto.getType());
        template.setCategory(category);
        template.setWallet(wallet);
        template.setUser(user);
        template.setFrequency(dto.getFrequency());
        template.setStartDate(dto.getStartDate());
        template.setActive(true);

        RecurringTransactionTemplate saved = recurringRepository.save(template);
        return convertToDto(saved);
    }

    /**
     * Dobij sve ponavljajuće transakcije za korisnika
     */
    public List<RecurringTransactionTemplateDto> getRecurringTemplatesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        List<RecurringTransactionTemplate> templates = recurringRepository.findByUserOrderByStartDateDesc(user);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Dobij aktivne ponavljajuće transakcije za korisnika
     */
    public List<RecurringTransactionTemplateDto> getActiveRecurringTemplatesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        List<RecurringTransactionTemplate> templates = recurringRepository.findByUserAndIsActiveTrue(user);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Uređuje postojeći template
     */
    public RecurringTransactionTemplateDto updateRecurringTemplate(Long templateId, RecurringTransactionTemplateDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        RecurringTransactionTemplate template = recurringRepository.findByIdAndUser(templateId, user);
        if (template == null) {
            throw new RuntimeException("Template nije pronađen ili ne pripada korisniku");
        }

        // Validacija novčanika
        if (dto.getWalletId() != null) {
            Wallet wallet = walletRepository.findById(dto.getWalletId())
                    .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen"));
            if (!wallet.getUser().getId().equals(userId)) {
                throw new RuntimeException("Novčanik ne pripada korisniku");
            }
            template.setWallet(wallet);
        }

        // Validacija kategorije
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
            template.setCategory(category);
        }

        // Ažuriraj ostale atribute
        if (dto.getName() != null) template.setName(dto.getName());
        if (dto.getAmount() != null) template.setAmount(dto.getAmount());
        if (dto.getType() != null) template.setType(dto.getType());
        if (dto.getFrequency() != null) template.setFrequency(dto.getFrequency());
        if (dto.getStartDate() != null) template.setStartDate(dto.getStartDate());

        RecurringTransactionTemplate updated = recurringRepository.save(template);
        return convertToDto(updated);
    }

    /**
     * Aktivira/deaktivira ponavljajuću transakciju
     */
    public RecurringTransactionTemplateDto toggleRecurringTemplate(Long templateId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        RecurringTransactionTemplate template = recurringRepository.findByIdAndUser(templateId, user);
        if (template == null) {
            throw new RuntimeException("Template nije pronađen ili ne pripada korisniku");
        }

        template.setActive(!template.isActive());
        RecurringTransactionTemplate updated = recurringRepository.save(template);
        return convertToDto(updated);
    }

    /**
     * Briše ponavljajuću transakciju
     */
    public void deleteRecurringTemplate(Long templateId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        RecurringTransactionTemplate template = recurringRepository.findByIdAndUser(templateId, user);
        if (template == null) {
            throw new RuntimeException("Template nije pronađen ili ne pripada korisniku");
        }

        recurringRepository.delete(template);
    }

    /**
     * Automatsko generisanje transakcija - poziva se scheduler-om
     * Pokreće se svaki dan u 02:00
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateScheduledTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransactionTemplate> activeTemplates =
                recurringRepository.findActiveTemplatesReadyForExecution(today);

        for (RecurringTransactionTemplate template : activeTemplates) {
            if (shouldGenerateTransaction(template, today)) {
                generateTransactionFromTemplate(template, today);
            }
        }
    }

    /**
     * Manuelno pokretanje generisanja transakcija za određeni template
     */
    public void generateTransactionFromTemplate(Long templateId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        RecurringTransactionTemplate template = recurringRepository.findByIdAndUser(templateId, user);
        if (template == null) {
            throw new RuntimeException("Template nije pronađen ili ne pripada korisniku");
        }

        if (!template.isActive()) {
            throw new RuntimeException("Template nije aktivan");
        }

        generateTransactionFromTemplate(template, LocalDate.now());
    }

    /**
     * Generiše transakciju na osnovu template-a
     */
    private void generateTransactionFromTemplate(RecurringTransactionTemplate template, LocalDate transactionDate) {
        Transaction transaction = new Transaction();
        transaction.setName(template.getName());
        transaction.setAmount(template.getAmount());
        transaction.setType(template.getType());
        transaction.setCategory(template.getCategory());
        transaction.setWallet(template.getWallet());
        transaction.setUser(template.getUser());
        transaction.setTransactionDate(transactionDate);
        transaction.setRecurringTemplate(template);
        transaction.setTransfer(false);

        // Ažuriraj stanje novčanika
        Wallet wallet = template.getWallet();
        BigDecimal currentBalance = wallet.getCurrentBalance();

        if (template.getType() == CategoryType.PRIHOD) {
            wallet.setCurrentBalance(currentBalance.add(template.getAmount()));
        } else {
            wallet.setCurrentBalance(currentBalance.subtract(template.getAmount()));
        }

        walletRepository.save(wallet);
        transactionRepository.save(transaction);
    }

    /**
     * Proverava da li treba generisati transakciju za dati template
     */
    private boolean shouldGenerateTransaction(RecurringTransactionTemplate template, LocalDate currentDate) {
        // Proveri da li je danas dan za generisanje na osnovu frekvencije
        LocalDate startDate = template.getStartDate();

        if (currentDate.isBefore(startDate)) {
            return false;
        }

        String frequency = template.getFrequency().toLowerCase();
        long daysBetween = ChronoUnit.DAYS.between(startDate, currentDate);

        switch (frequency) {
            case "dnevno":
            case "daily":
                return true;
            case "nedeljno":
            case "weekly":
                return daysBetween % 7 == 0;
            case "mesečno":
            case "monthly":
                return currentDate.getDayOfMonth() == startDate.getDayOfMonth() ||
                        (currentDate.getDayOfMonth() == currentDate.lengthOfMonth() &&
                                startDate.getDayOfMonth() > currentDate.lengthOfMonth());
            case "godišnje":
            case "yearly":
                return currentDate.getMonthValue() == startDate.getMonthValue() &&
                        currentDate.getDayOfMonth() == startDate.getDayOfMonth();
            default:
                return false;
        }
    }

    /**
     * Konvertuje entity u DTO
     */
    private RecurringTransactionTemplateDto convertToDto(RecurringTransactionTemplate template) {
        RecurringTransactionTemplateDto dto = new RecurringTransactionTemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setAmount(template.getAmount());
        dto.setType(template.getType());
        dto.setCategoryId(template.getCategory().getId());
        dto.setCategoryName(template.getCategory().getName());
        dto.setWalletId(template.getWallet().getId());
        dto.setWalletName(template.getWallet().getName());
        dto.setUserId(template.getUser().getId());
        dto.setFrequency(template.getFrequency());
        dto.setStartDate(template.getStartDate());
        dto.setEndDate(template.getEndDate()); // Dodano
        dto.setActive(template.isActive());
        dto.setTotalGenerated(template.getTotalGenerated()); // Dodano
        dto.setLastGenerated(template.getLastGenerated()); // Dodano

        return dto;
    }

    /**
     * Dobij istoriju transakcija za određeni template
     */
    public List<Transaction> getTransactionHistoryForTemplate(Long templateId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        RecurringTransactionTemplate template = recurringRepository.findByIdAndUser(templateId, user);
        if (template == null) {
            throw new RuntimeException("Template nije pronađen ili ne pripada korisniku");
        }

        return recurringRepository.findTransactionsByTemplateId(templateId);
    }

    /**
     * Zaustavi ponavljajuću transakciju sa određenim datumom kraja
     */
    public RecurringTransactionTemplateDto stopRecurringTemplate(Long templateId, Long userId, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        RecurringTransactionTemplate template = recurringRepository.findByIdAndUser(templateId, user);
        if (template == null) {
            throw new RuntimeException("Template nije pronađen ili ne pripada korisniku");
        }

        template.setEndDate(endDate);

        // Ako je endDate u prošlosti, automatski deaktiviraj
        if (endDate.isBefore(LocalDate.now())) {
            template.setActive(false);
        }

        RecurringTransactionTemplate updated = recurringRepository.save(template);
        return convertToDto(updated);
    }

    /**
     * Dobij broj aktivnih ponavljajućih transakcija za korisnika
     */
    public long getActiveRecurringCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        return recurringRepository.countActiveByUser(user);
    }
}