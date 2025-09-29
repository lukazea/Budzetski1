package entity.seeders;

import entity.Currency;
import entity.User;
import entity.User.Role;
import repository.CurrencyRepository;
import repository.UserRepository;

import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUserName("admin").isEmpty()) {
                User admin = new User();
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setUserName("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setBlocked(false);
                admin.setBirth_date(LocalDate.of(1999,1,1));
                admin.setCurrency("RSD");
                admin.setRegistartion_date(LocalDate.of(1999,1,1));

                userRepository.save(admin);
                System.out.println("✅ Admin user created: username=admin, password=admin123");
            } else {
                System.out.println("ℹ️ Admin user already exists.");
            }
        };
    }

    @Bean
    CommandLineRunner seedCurrencies(CurrencyRepository currencyRepository) {
        return args -> {
            if (currencyRepository.count() == 0) {
                Currency eur = new Currency("EUR", 1.0f);
                Currency rsd = new Currency("RSD", 117.5f);
                Currency usd = new Currency("USD", 1.2f);

                currencyRepository.saveAll(Arrays.asList(eur, rsd, usd));
                System.out.println("✅ Currencies seeded: EUR, RSD, USD");
            } else {
                System.out.println("ℹ️ Currencies already seeded.");
            }
        };
    }
}
