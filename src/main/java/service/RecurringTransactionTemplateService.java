package service;

import entity.RecurringTransactionTemplate;
import entity.Transaction;
import entity.User;
import entity.Wallet;
import entity.Category;
import entity.CategoryType;
import repository.RecurringTransactionTemplateRepository;
import repository.UserRepository;
import repository.WalletRepository;
import repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecurringTransactionTemplateService {

    @Autowired
    private RecurringTransactionTemplateRepository recurringRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    // Kreiranje ponavljajuće transakcije
    public RecurringTransactionTemplate createRecurringTemplate(RecurringTransactionTemplate template) {
        // Validacija
        if (template.getUser() == null || template.getWallet() == null) {
            throw new RuntimeException("Korisnik i novčanik moraju biti specificirani!");
        }
        
        // Validacija frekvencije
        if (!isValidFrequency(template.getFrequency())) {
            throw new RuntimeException("Neispravna frekvencija! Dozvoljeno: daily, weekly, monthly, yearly");
        }
        
        if (template.getStartDate() == null) {
            template.setStartDate(LocalDate.now());
        }
        
        template.setActive(true);
        return recurringRepository.save(template);
    }

    // Kreiranje template-a sa ID parametrima
    public RecurringTransactionTemplate createRecurringTemplate(String name, 
                                                               java.math.BigDecimal amount,
                                                               CategoryType type,
                                                               Long categoryId,
                                                               Long walletId,
                                                               Long userId,
                                                               String frequency,
                                                               LocalDate startDate) {
        // Pronađi entitete
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
        
        // Provjeri da li wallet pripada korisniku
        if (!wallet.getUser().getId().equals(userId)) {
            throw new RuntimeException("Novčanik ne pripada ovom korisniku!");
        }
        
        // Kreiraj template
        RecurringTransactionTemplate template = new RecurringTransactionTemplate();
        template.setName(name);
        template.setAmount(amount);
        template.setType(type);
        template.setCategory(category);
        template.setWallet(wallet);
        template.setUser(user);
        template.setFrequency(frequency);
        template.setStartDate(startDate != null ? startDate : LocalDate.now());
        template.setActive(true);
        
        return recurringRepository.save(template);
    }

    // Ažuriranje template-a
    public RecurringTransactionTemplate updateRecurringTemplate(Long templateId, RecurringTransactionTemplate updatedTemplate) {
        RecurringTransactionTemplate template = recurringRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template nije pronađen!"));
        
        template.setName(updatedTemplate.getName());
        template.setAmount(updatedTemplate.getAmount());
        template.setType(updatedTemplate.getType());
        template.setFrequency(updatedTemplate.getFrequency());
        
        // Ažuriraj kategoriju samo ako je nova kategorija prosleđena
        if (updatedTemplate.getCategory() != null) {
            template.setCategory(updatedTemplate.getCategory());
        }
        
        return recurringRepository.save(template);
    }

    // Aktiviranje/deaktiviranje
    public void toggleRecurringTemplate(Long templateId) {
        RecurringTransactionTemplate template = recurringRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template nije pronađen!"));
        
        template.setActive(!template.isActive());
        recurringRepository.save(template);
    }

    // Aktiviranje template-a
    public void activateTemplate(Long templateId) {
        RecurringTransactionTemplate template = recurringRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template nije pronađen!"));
        
        template.setActive(true);
        recurringRepository.save(template);
    }

    // Deaktiviranje template-a
    public void deactivateTemplate(Long templateId) {
        RecurringTransactionTemplate template = recurringRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template nije pronađen!"));
        
        template.setActive(false);
        recurringRepository.save(template);
    }

    // Brisanje template-a
    public void deleteRecurringTemplate(Long templateId) {
        RecurringTransactionTemplate template = recurringRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template nije pronađen!"));
        
        recurringRepository.delete(template);
    }

    // Pronađi template-e korisnika
    public List<RecurringTransactionTemplate> getUserTemplates(Long userId) {
        return recurringRepository.findByUserId(userId);
    }

    // Pronađi aktivne template-e korisnika
    public List<RecurringTransactionTemplate> getActiveUserTemplates(Long userId) {
        return recurringRepository.findByUserIdAndIsActiveTrue(userId);
    }

    // Pronađi template-e po novčaniku
    public List<RecurringTransactionTemplate> getWalletTemplates(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Novčanik nije pronađen!"));
        return recurringRepository.findByWallet(wallet);
    }

    // Pronađi template-e po tipu
    public List<RecurringTransactionTemplate> getUserTemplatesByType(Long userId, CategoryType type) {
        return recurringRepository.findByUserIdAndType(userId, type);
    }

    // Pronađi template-e po frekvenciji
    public List<RecurringTransactionTemplate> getUserTemplatesByFrequency(Long userId, String frequency) {
        return recurringRepository.findByUserIdAndFrequency(userId, frequency);
    }

    // Pronađi template po ID
    public Optional<RecurringTransactionTemplate> findById(Long templateId) {
        return recurringRepository.findById(templateId);
    }

    // Validacija frekvencije
    private boolean isValidFrequency(String frequency) {
        return frequency != null && 
               (frequency.equalsIgnoreCase("daily") || 
                frequency.equalsIgnoreCase("weekly") || 
                frequency.equalsIgnoreCase("monthly") || 
                frequency.equalsIgnoreCase("yearly"));
    }

    // Automatska egzekucija ponavljajućih transakcija (Scheduled job)
    @Scheduled(cron = "0 0 2 * * ?") // Svaki dan u 2:00 ujutro
    public void executeRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransactionTemplate> activeTemplates = 
            recurringRepository.findActiveTemplatesForExecution(today);
        
        for (RecurringTransactionTemplate template : activeTemplates) {
            try {
                if (shouldExecuteToday(template, today)) {
                    executeRecurringTransaction(template);
                }
            } catch (Exception e) {
                // Log greška ali nastavi sa ostalim template-ovima
                System.err.println("Greška pri izvršavanju recurring transakcije za template ID: " 
                    + template.getId() + ", Error: " + e.getMessage());
            }
        }
    }

    // Proveri da li treba izvršiti transakciju danas
    private boolean shouldExecuteToday(RecurringTransactionTemplate template, LocalDate today) {
        LocalDate startDate = template.getStartDate();
        
        if (today.isBefore(startDate)) {
            return false;
        }
        
        String frequency = template.getFrequency().toLowerCase();
        
        switch (frequency) {
            case "daily":
                return true; // Izvršava se svaki dan
            case "weekly":
                return today.getDayOfWeek().equals(startDate.getDayOfWeek());
            case "monthly":
                return today.getDayOfMonth() == startDate.getDayOfMonth() ||
                       (today.getDayOfMonth() == today.lengthOfMonth() && 
                        startDate.getDayOfMonth() > today.lengthOfMonth());
            case "yearly":
                return today.getMonth().equals(startDate.getMonth()) &&
                       today.getDayOfMonth() == startDate.getDayOfMonth();
            default:
                return false;
        }
    }

    // Izvršava pojedinačnu ponavljajuću transakciju
    private void executeRecurringTransaction(RecurringTransactionTemplate template) {
        // Kreiraj novu transakciju na osnovu template-a
        Transaction transaction = new Transaction();
        transaction.setName(template.getName());
        transaction.setAmount(template.getAmount());
        transaction.setType(template.getType());
        transaction.setCategory(template.getCategory());
        transaction.setWallet(template.getWallet());
        transaction.setUser(template.getUser());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setRecurringTemplate(template);
        transaction.setTransfer(false);
        
        // Koristi TransactionService da kreira transakciju
        transactionService.createTransaction(transaction);
    }

    // Manuelna egzekucija template-a
    public Transaction executeTemplateNow(Long templateId) {
        RecurringTransactionTemplate template = recurringRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template nije pronađen!"));
        
        if (!template.isActive()) {
            throw new RuntimeException("Template nije aktivan!");
        }
        
        executeRecurringTransaction(template);
        
        // Vrati kreiraanu transakciju (potrebno je modificirati executeRecurringTransaction da vrati Transaction)
        Transaction transaction = new Transaction();
        transaction.setName(template.getName());
        transaction.setAmount(template.getAmount());
        transaction.setType(template.getType());
        transaction.setCategory(template.getCategory());
        transaction.setWallet(template.getWallet());
        transaction.setUser(template.getUser());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setRecurringTemplate(template);
        transaction.setTransfer(false);
        
        return transactionService.createTransaction(transaction);
    }

    // Admin funkcionalnosti - pregled svih template-ova
    public List<RecurringTransactionTemplate> getAllTemplates() {
        return recurringRepository.findAll();
    }

    // Pregled template-ova po datumu kreiranja
    public List<RecurringTransactionTemplate> getTemplatesByDateRange(LocalDate startDate, LocalDate endDate) {
        return recurringRepository.findByStartDateBetween(startDate, endDate);
    }

    // Statistike
    public long countActiveTemplatesForUser(Long userId) {
        return recurringRepository.findByUserIdAndIsActiveTrue(userId).size();
    }

    public long countTotalTemplatesForUser(Long userId) {
        return recurringRepository.findByUserId(userId).size();
    }
}       		