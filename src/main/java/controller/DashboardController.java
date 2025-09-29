package controller;

import dto.DashboardStatsDto;
import dto.TransactionDto;
import service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")

public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;

    // Glavne statistike za dashboard
    @GetMapping("/admin/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // Top 10 transakcija u poslednjih 30 dana
    @GetMapping("/admin/top-transactions/month")
    public ResponseEntity<List<TransactionDto>> getTopTransactionsMonth() {
        List<TransactionDto> transactions = dashboardService.getTopTransactionsLastMonth();
        return ResponseEntity.ok(transactions);
    }

    // Top 10 transakcija u poslednja 2 minuta (testiranje)
    @GetMapping("/admin/top-transactions/recent")
    public ResponseEntity<List<TransactionDto>> getTopTransactionsRecent() {
        List<TransactionDto> transactions = dashboardService.getTopTransactionsLastTwoMinutes();
        return ResponseEntity.ok(transactions);
    }
}