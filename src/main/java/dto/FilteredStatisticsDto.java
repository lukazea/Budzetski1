package dto;

import java.time.LocalDate;
import java.util.List;

public class FilteredStatisticsDto extends StatisticsDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType; // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    private List<CategoryStatDto> categoryBreakdown;
    
    public FilteredStatisticsDto() {}
    
    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    
    public List<CategoryStatDto> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryStatDto> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
}
