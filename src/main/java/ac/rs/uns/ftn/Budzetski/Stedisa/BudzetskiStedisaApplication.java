package ac.rs.uns.ftn.Budzetski.Stedisa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ComponentScan(basePackages = {
	    "ac.rs.uns.ftn.Budzetski.Stedisa", 
	    "controller",                        
	    "service",                          
	    "repository",                        
	    "dto",
	    "entity"
	})

@EnableJpaRepositories(basePackages = {"repository"}) 
@EntityScan(basePackages = {"entity"})              

public class BudzetskiStedisaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudzetskiStedisaApplication.class, args);
	}
 // ovo je komentar
//ovo je moj komentar
	// treci komentar
	
}
