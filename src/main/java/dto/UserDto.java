package dto;

public class UserDto {

    // Polje za broj ukupnih korisnika (prva funkcionalnost)
    private long totalUserCount;

    // Konstruktor
    public UserDto() {}

    // Getter i setter za totalUserCount
    public long getTotalUserCount() {
        return totalUserCount;
    }

    public void setTotalUserCount(long totalUserCount) {
        this.totalUserCount = totalUserCount;
    }
}