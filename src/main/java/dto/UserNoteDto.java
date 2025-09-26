package dto;
import java.time.LocalDateTime;

import entity.UserNote;

public class UserNoteDto {
    
    private Long id;
    private Long userId;
    private String userName;
    private String note;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;

    // Konstruktori
    public UserNoteDto() {}

    public UserNoteDto(UserNote userNote) {
        this.id = userNote.getId();
        this.userId = userNote.getUser().getId();
        this.userName = userNote.getUser().getUserName();
        this.note = userNote.getNote();
        this.createdDate = userNote.getCreatedDate();
        this.createdBy = userNote.getCreatedBy();
        this.updatedDate = userNote.getUpdatedDate();
        this.updatedBy = userNote.getUpdatedBy();
    }

    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
