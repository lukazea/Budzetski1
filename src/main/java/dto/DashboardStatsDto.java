package dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private Long totalUsers;
    private Long activeUsers;
    private BigDecimal averageActiveUserBalance;
    private BigDecimal totalSystemBalance;

    public DashboardStatsDto() {}

    public DashboardStatsDto(Long totalUsers, Long activeUsers, 
                            BigDecimal averageActiveUserBalance, BigDecimal totalSystemBalance) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.averageActiveUserBalance = averageActiveUserBalance;
        this.totalSystemBalance = totalSystemBalance;
    }

    // Getteri i setteri
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public BigDecimal getAverageActiveUserBalance() {
        return averageActiveUserBalance;
    }

    public void setAverageActiveUserBalance(BigDecimal averageActiveUserBalance) {
        this.averageActiveUserBalance = averageActiveUserBalance;
    }

    public BigDecimal getTotalSystemBalance() {
        return totalSystemBalance;
    }

    public void setTotalSystemBalance(BigDecimal totalSystemBalance) {
        this.totalSystemBalance = totalSystemBalance;
    }
}