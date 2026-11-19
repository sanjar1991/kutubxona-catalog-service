package kutubxona_catalog_service.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * BaseEntity'dagi @CreatedDate / @LastModifiedDate / @CreatedBy / @LastModifiedBy
 * maydonlarini avtomatik to'ldiradi.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * Hozircha autentifikatsiya yo'q, shuning uchun foydalanuvchi aniqlanmaydi
     * (created_by / updated_by = null). Login qo'shilganda shu yerda
     * SecurityContext'dan foydalanuvchi id'si olinadi.
     */
    @Bean
    public AuditorAware<Long> auditorAware() {
        return Optional::empty;
    }
}
