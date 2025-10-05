package controller;

import dto.RecurringTransactionTemplateDto;
import dto.TransactionDto;
import entity.Transaction;
import service.RecurringTransactionTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recurring-templates")

public class RecurringTransactionTemplateController {

    @Autowired
    private RecurringTransactionTemplateService recurringService;

    @PostMapping
    public ResponseEntity<RecurringTransactionTemplateDto> createRecurringTemplate(
            @RequestBody RecurringTransactionTemplateDto dto) {
        RecurringTransactionTemplateDto created = recurringService.createRecurringTemplate(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }

    /**
     * Dobija sve ponavljajuće transakcije za korisnika
     * GET /api/recurring-templates/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getRecurringTemplatesByUser(
            @PathVariable Long userId) {
        try {
            List<RecurringTransactionTemplateDto> templates = recurringService.getRecurringTemplatesByUser(userId);
            return new ResponseEntity<>(templates, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Dobija samo aktivne ponavljajuće transakcije za korisnika
     * GET /api/recurring-templates/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<RecurringTransactionTemplateDto>> getActiveRecurringTemplatesByUser(
            @PathVariable Long userId) {
        try {
            List<RecurringTransactionTemplateDto> templates = recurringService.getActiveRecurringTemplatesByUser(userId);
            return new ResponseEntity<>(templates, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Ažurira postojeći template
     * PUT /api/recurring-templates/{templateId}/user/{userId}
     */
    @PutMapping("/{templateId}/user/{userId}")
    public ResponseEntity<RecurringTransactionTemplateDto> updateRecurringTemplate(
            @PathVariable Long templateId,
            @PathVariable Long userId,
            @RequestBody RecurringTransactionTemplateDto dto) {
        try {
            RecurringTransactionTemplateDto updated = recurringService.updateRecurringTemplate(templateId, dto, userId);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Aktivira/deaktivira ponavljajuću transakciju
     * PATCH /api/recurring-templates/{templateId}/user/{userId}/toggle
     */
    @PatchMapping("/{templateId}/user/{userId}/toggle")
    public ResponseEntity<RecurringTransactionTemplateDto> toggleRecurringTemplate(
            @PathVariable Long templateId,
            @PathVariable Long userId) {
        try {
            RecurringTransactionTemplateDto toggled = recurringService.toggleRecurringTemplate(templateId, userId);
            return new ResponseEntity<>(toggled, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Briše ponavljajuću transakciju
     * DELETE /api/recurring-templates/{templateId}/user/{userId}
     */
    @DeleteMapping("/{templateId}/user/{userId}")
    public ResponseEntity<Void> deleteRecurringTemplate(
            @PathVariable Long templateId,
            @PathVariable Long userId) {
        try {
            recurringService.deleteRecurringTemplate(templateId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Manuelno generiše transakciju iz template-a
     * POST /api/recurring-templates/{templateId}/user/{userId}/generate
     */
    @PostMapping("/{templateId}/user/{userId}/generate")
    public ResponseEntity<String> generateTransactionFromTemplate(
            @PathVariable Long templateId,
            @PathVariable Long userId) {
        try {
            recurringService.generateTransactionFromTemplate(templateId, userId);
            return new ResponseEntity<>("Transakcija je uspešno generisana", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Dobija broj aktivnih ponavljajućih transakcija za korisnika
     * GET /api/recurring-templates/user/{userId}/count
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getActiveRecurringCount(@PathVariable Long userId) {
        try {
            long count = recurringService.getActiveRecurringCount(userId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(0L, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Health check endpoint za testiranje
     * GET /api/recurring-templates/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Recurring Templates Controller is working!", HttpStatus.OK);
    }

    /**
     * Dobija istoriju transakcija za određeni template
     * GET /api/recurring-templates/{templateId}/user/{userId}/history
     */
    @GetMapping("/{templateId}/user/{userId}/history")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(
            @PathVariable Long templateId,
            @PathVariable Long userId) {
        List<TransactionDto> history = recurringService.getTransactionHistoryForTemplate(templateId, userId).stream().map(TransactionDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    /**
     * Zaustavi ponavljajuću transakciju sa određenim datumom kraja
     * PATCH /api/recurring-templates/{templateId}/user/{userId}/stop
     */
    @PatchMapping("/{templateId}/user/{userId}/stop")
    public ResponseEntity<RecurringTransactionTemplateDto> stopRecurringTemplate(
            @PathVariable Long templateId,
            @PathVariable Long userId,
            @RequestParam("endDate") String endDateString) {
        try {
            LocalDate endDate = LocalDate.parse(endDateString);
            RecurringTransactionTemplateDto stopped = recurringService.stopRecurringTemplate(templateId, userId, endDate);
            return new ResponseEntity<>(stopped, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}