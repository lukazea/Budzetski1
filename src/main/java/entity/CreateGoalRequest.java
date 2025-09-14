package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGoalRequest {
	    private String name;
	    private Long userId;
	    
	    public Long getUserId() {
			return userId;
		}
		public void setUserId(Long userId) {
			this.userId = userId;
		}
		
		private BigDecimal targetAmount;
	    private LocalDate deadline;
	    private Long walletId; // null ako se pravi novi
	    private String newWalletName; // ako se pravi novi novčanik
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public BigDecimal getTargetAmount() {
			return targetAmount;
		}
		public void setTargetAmount(BigDecimal targetAmount) {
			this.targetAmount = targetAmount;
		}
		public LocalDate getDeadline() {
			return deadline;
		}
		public void setDeadline(LocalDate deadline) {
			this.deadline = deadline;
		}
		public Long getWalletId() {
			return walletId;
		}
		public void setWalletId(Long walletId) {
			this.walletId = walletId;
		}
		public String getNewWalletName() {
			return newWalletName;
		}
		public void setNewWalletName(String newWalletName) {
			this.newWalletName = newWalletName;
		}
}
