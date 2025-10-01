package dto;

import entity.Category;
import entity.CategoryType;

public class CategoryDto {
    private Long id;
    private String name;
    private CategoryType type;
    private boolean predefined;
    private Long userId;
    private String userName;
    private Boolean active;

    // --- Konstruktor bez parametara ---
    public CategoryDto() {}

    // --- Konstruktor iz entiteta Category ---
    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.type = category.getType();
        this.predefined = category.isPredefined();
        this.active = category.isActive();
        if (category.getUser() != null) {
            this.userId = category.getUser().getId();
            this.userName = category.getUser().getUserName();
        }
    }

    // --- Konstruktor sa parametrima ---
    public CategoryDto(String name, CategoryType type, boolean predefined, Long userId) {
        this.name = name;
        this.type = type;
        this.predefined = predefined;
        this.userId = userId;
    }

    // --- Getteri i setteri ---
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

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public Boolean getActive() {return active;}
}
