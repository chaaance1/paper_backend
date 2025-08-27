package dddan.paper_summary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class PaperSummaryApplication {

	public static void main(String[] args) {
        SpringApplication.run(PaperSummaryApplication.class, args);
	}

}
