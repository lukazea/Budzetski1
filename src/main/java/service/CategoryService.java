package service;

import entity.Category;
import entity.CategoryType;
import entity.User;
import repository.CategoryRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;
    
	@Autowired
	private UserRepository userRepository;

	// Kreiranje predefinisanih kategorija (za nové korisnike)
	public void createDefaultCategories(User user) {
			// Prihodi
			createCategoryIfNotExists("Plata", CategoryType.PRIHOD, true, null);
			createCategoryIfNotExists("Bonus", CategoryType.PRIHOD, true, null);
			createCategoryIfNotExists("Investicije", CategoryType.PRIHOD, true, null);
        
			// Troškovi
			createCategoryIfNotExists("Hrana", CategoryType.TROSAK, true, null);
			createCategoryIfNotExists("Zabava", CategoryType.TROSAK, true, null);
			createCategoryIfNotExists("Domaćinstvo", CategoryType.TROSAK, true, null);
			createCategoryIfNotExists("Transport", CategoryType.TROSAK, true, null);
			createCategoryIfNotExists("Režije", CategoryType.TROSAK, true, null);
    }

	private void createCategoryIfNotExists(String name, CategoryType type, boolean predefined, User user) {
		boolean exists = predefined ? 
				categoryRepository.existsByNameAndPredefinedTrue(name) :
				categoryRepository.existsByNameAndUser(name, user);
            
		if (!exists) {
			Category category = new Category(name, type, predefined, user);
			categoryRepository.save(category);
        }
    }

	// Kreiranje custom kategorije
	public Category createUserCategory(String name, CategoryType type, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        
		// Proveri da li već postoji
		if (categoryRepository.existsByNameAndUser(name, user)) {
			throw new RuntimeException("Kategorija sa ovim imenom već postoji!");
		}
        
		Category category = new Category(name, type, false, user);
		return categoryRepository.save(category);
	}

    // Ažuriranje kategorije
    public Category updateCategory(Long categoryId, String newName) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
        
        // Ne može da se ažuriraju predefinisane kategorije
        if (category.isPredefined()) {
            throw new RuntimeException("Predefinisane kategorije ne mogu biti izmenjene!");
        }
        
        category.setName(newName);
        return categoryRepository.save(category);
    }

    // Brisanje kategorije
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena!"));
        
        // Ne može da se obrišu predefinisane kategorije
        if (category.isPredefined()) {
            throw new RuntimeException("Predefinisane kategorije ne mogu biti obrisane!");
        }
        
        // Proveri da li ima transakcije
        if (!category.getTransactions().isEmpty()) {
            throw new RuntimeException("Ne možete obrisati kategoriju koja ima transakcije!");
        }
        
        categoryRepository.delete(category);
    }

    // Pronađi dostupne kategorije za korisnika
    public List<Category> getAvailableCategories(Long userId) {
        return categoryRepository.findAvailableForUser(userId);
    }

    // Pronađi kategorije po tipu
    public List<Category> getCategoriesByType(Long userId, CategoryType type) {
        return categoryRepository.findByTypeAndAvailableForUser(type, userId);
    }

    // Pronađi korisničke kategorije
    public List<Category> getUserCategories(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen!"));
        return categoryRepository.findByUserAndPredefinedFalse(user);
    }

    // Pronađi kategoriju po ID
    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }
}