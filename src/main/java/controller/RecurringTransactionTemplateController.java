package controller;

import dto.RecurringTransactionTemplateDto;
import dto.TransactionDto;
import entity.RecurringTransactionTemplate;
import entity.Transaction;
import entity.CategoryType;
import service.RecurringTransactionTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recurring-templates")
@CrossOrigin(origins = "*")

public class RecurringTransactionTemplateController {

    @Autowired
    private RecurringTransactionTemplateService recurringTemplateService;

    // Kreiranje novog recurring template-a
    @PostMapping
    public ResponseEntity<RecurringTransactionTemplateDto> createRecurringTemplate(
            @RequestBody CreateRecurringTemplateRequest request) {
        try {
            RecurringTransactionTemplate template = recurringTemplateService.createRecurringTemplate(
                request.getName(),
                request.getAmount(),
                request.getType(),
                request.getCategoryId(),
                request.getWalletId(),
                request.getUserId(),
                request.getFrequency(),
                request.getStartDate()
            );
            return ResponseEntity.ok(new RecurringTransactionTemplateDto(template));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi template po ID
    @GetMapping("/{templateId}")
    public ResponseEntity<RecurringTransactionTemplateDto> getTemplateById(@PathVariable Long templateId) {
        return recurringTemplateService.findById(templateId)
            .map(template -> ResponseEntity.ok(new RecurringTransactionTemplateDto(template)))
            .orElse(ResponseEntity.notFound().build());
    }

    // Preuzmi template-e korisnika
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getUserTemplates(@PathVariable Long userId) {
        try {
            List<RecurringTransactionTemplate> templates = recurringTemplateService.getUserTemplates(userId);
            List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
                .map(RecurringTransactionTemplateDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(templateDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi aktivne template-e korisnika
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getActiveUserTemplates(@PathVariable Long userId) {
        try {
            List<RecurringTransactionTemplate> templates = recurringTemplateService.getActiveUserTemplates(userId);
            List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
                .map(RecurringTransactionTemplateDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(templateDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi template-e po novčaniku
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getWalletTemplates(@PathVariable Long walletId) {
        try {
            List<RecurringTransactionTemplate> templates = recurringTemplateService.getWalletTemplates(walletId);
            List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
                .map(RecurringTransactionTemplateDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(templateDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi template-e po tipu
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getUserTemplatesByType(
            @PathVariable Long userId,
            @PathVariable CategoryType type) {
        try {
            List<RecurringTransactionTemplate> templates = 
                recurringTemplateService.getUserTemplatesByType(userId, type);
            List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
                .map(RecurringTransactionTemplateDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(templateDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Preuzmi template-e po frekvenciji
    @GetMapping("/user/{userId}/frequency/{frequency}")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getUserTemplatesByFrequency(
            @PathVariable Long userId,
            @PathVariable String frequency) {
        try {
            List<RecurringTransactionTemplate> templates = 
                recurringTemplateService.getUserTemplatesByFrequency(userId, frequency);
            List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
                .map(RecurringTransactionTemplateDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(templateDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ažuriranje template-a
    @PutMapping("/{templateId}")
    public ResponseEntity<RecurringTransactionTemplateDto> updateRecurringTemplate(
            @PathVariable Long templateId,
            @RequestBody UpdateRecurringTemplateRequest request) {
        try {
            RecurringTransactionTemplate updatedTemplate = new RecurringTransactionTemplate();
            updatedTemplate.setName(request.getName());
            updatedTemplate.setAmount(request.getAmount());
            updatedTemplate.setType(request.getType());
            updatedTemplate.setFrequency(request.getFrequency());
            
            RecurringTransactionTemplate template = 
                recurringTemplateService.updateRecurringTemplate(templateId, updatedTemplate);
            return ResponseEntity.ok(new RecurringTransactionTemplateDto(template));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aktiviranje/deaktiviranje template-a (toggle)
    @PutMapping("/{templateId}/toggle")
    public ResponseEntity<Void> toggleRecurringTemplate(@PathVariable Long templateId) {
        try {
            recurringTemplateService.toggleRecurringTemplate(templateId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aktiviranje template-a
    @PutMapping("/{templateId}/activate")
    public ResponseEntity<Void> activateTemplate(@PathVariable Long templateId) {
        try {
            recurringTemplateService.activateTemplate(templateId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Deaktiviranje template-a
    @PutMapping("/{templateId}/deactivate")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long templateId) {
        try {
            recurringTemplateService.deactivateTemplate(templateId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Brisanje template-a
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteRecurringTemplate(@PathVariable Long templateId) {
        try {
            recurringTemplateService.deleteRecurringTemplate(templateId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Manualna egzekucija template-a
    @PostMapping("/{templateId}/execute")
    public ResponseEntity<TransactionDto> executeTemplateNow(@PathVariable Long templateId) {
        try {
            Transaction transaction = recurringTemplateService.executeTemplateNow(templateId);
            return ResponseEntity.ok(new TransactionDto(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Statistike - broj aktivnih template-ova za korisnika
    @GetMapping("/user/{userId}/stats/active-count")
    public ResponseEntity<Long> countActiveTemplatesForUser(@PathVariable Long userId) {
        long count = recurringTemplateService.countActiveTemplatesForUser(userId);
        return ResponseEntity.ok(count);
    }

    // Statistike - ukupan broj template-ova za korisnika
    @GetMapping("/user/{userId}/stats/total-count")
    public ResponseEntity<Long> countTotalTemplatesForUser(@PathVariable Long userId) {
        long count = recurringTemplateService.countTotalTemplatesForUser(userId);
        return ResponseEntity.ok(count);
    }

    // === ADMIN ENDPOINTS ===
    
    // Svi template-ovi (admin)
    @GetMapping("/admin/all")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getAllTemplates() {
        List<RecurringTransactionTemplate> templates = recurringTemplateService.getAllTemplates();
        List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
            .map(RecurringTransactionTemplateDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(templateDtos);
    }

    // Template-ovi po datumu kreiranja (admin)
    @GetMapping("/admin/date-range")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getTemplatesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<RecurringTransactionTemplate> templates = 
                recurringTemplateService.getTemplatesByDateRange(startDate, endDate);
            List<RecurringTransactionTemplateDto> templateDtos = templates.stream()
                .map(RecurringTransactionTemplateDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(templateDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Manuelno pokretanje automatskih transakcija (admin/testing)
    @PostMapping("/admin/execute-automatic")
    public ResponseEntity<Void> executeRecurringTransactions() {
        try {
            recurringTemplateService.executeRecurringTransactions();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO klase za request body
    public static class CreateRecurringTemplateRequest {
        private String name;
        private BigDecimal amount;
        private CategoryType type;
        private Long categoryId;
        private Long walletId;
        private Long userId;
        private String frequency;
        private LocalDate startDate;

        // Getteri i Setteri
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public CategoryType getType() { return type; }
        public void setType(CategoryType type) { this.type = type; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getWalletId() { return walletId; }
        public void setWalletId(Long walletId) { this.walletId = walletId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    }

    public static class UpdateRecurringTemplateRequest {
        private String name;
        private BigDecimal amount;
        private CategoryType type;
        private String frequency;
        private Long categoryId;

        // Getteri i Setteri
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public CategoryType getType() { return type; }
        public void setType(CategoryType type) { this.type = type; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }
}