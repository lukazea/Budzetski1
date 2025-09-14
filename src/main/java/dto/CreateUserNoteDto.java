package dto;

public class CreateUserNoteDto {

    private Long userId;
    private String note;

    public CreateUserNoteDto() {}

    public CreateUserNoteDto(Long userId, String note) {
        this.userId = userId;
        this.note = note;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("User ID je obavezan");
        }
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Beleška ne može biti prazna");
        }
    }
}
