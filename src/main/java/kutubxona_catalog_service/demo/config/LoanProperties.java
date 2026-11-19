package kutubxona_catalog_service.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Ijara qoidalari sozlamalari (application.properties -> library.loan.*).
 *
 * @param maxActivePerMember bitta a'zoda bir vaqtda bo'lishi mumkin bo'lgan kitoblar soni
 * @param periodDays         kitob necha kunga beriladi
 */
@ConfigurationProperties(prefix = "library.loan")
public record LoanProperties(
        @DefaultValue("5") int maxActivePerMember,
        @DefaultValue("14") int periodDays
) {
}
