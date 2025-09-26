package dto;

import entity.CategoryType;

public class CategoryDto {
    private Long id;
    private String name;
    private CategoryType type;
    private boolean predefined;
    private Long userId;

    // Konstruktori
    public CategoryDto() {}

    public CategoryDto(String name, CategoryType type, boolean predefined, Long userId) {
        this.name = name;
        this.type = type;
        this.predefined = predefined;
        this.userId = userId;
    }

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public boolean isPredefined() { return predefined; }
    public void setPredefined(boolean predefined) { this.predefined = predefined; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}