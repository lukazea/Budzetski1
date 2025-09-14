package service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import entity.Currency;
import entity.User;
import entity.Wallet;
import repository.GoalRepository;
import repository.WalletRepository;

public class WalletService {
    @Autowired
    private WalletRepository walletRepository;
	
	public Wallet createSavingsWallet(User user, String name, Currency currency) {
	    Wallet wallet = new Wallet();
	    wallet.setUser(user);
	    wallet.setName(name);
	    
	    Set<Currency> currencySet = new HashSet<>();
        currencySet.add(currency);
        wallet.setCurrencies(currencySet);
        
	    wallet.setSavings(true);
	    wallet.setCurrentBalance(BigDecimal.ZERO);
	    wallet.setCreationDate(LocalDate.now());
	    return walletRepository.save(wallet);
	}
}
