package kutubxona_catalog_service.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

    /** Bitta so'rovda 100 tadan ortiq yozuv so'rab bo'lmaydi (bazani himoya qiladi). */
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableLimits() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}
