package kutubxona_catalog_service.demo.stage14;

import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 14-bosqich (16-band): konfiguratsiya xavfsizligi.
 * 1) Asosiy application.properties'da parol yoki uning standart qiymati yozilmagan.
 * 2) DB manzili ham muhit o'zgaruvchisidan olinadi.
 * 3) Testlar asosiy konfiguratsiyani to'smaydi — faqat "test" profili bilan ustidan yozadi.
 */
class ConfigurationTest extends IntegrationTestBase {

    @Autowired Environment environment;

    private static Properties mainProperties() throws Exception {
        return PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
    }

    @Test
    void dbPassword_comesOnlyFromEnvironment_withoutDefault() throws Exception {
        String password = mainProperties().getProperty("spring.datasource.password");

        assertThat(password)
                .as("Parol faqat DB_PASSWORD muhit o'zgaruvchisidan olinishi, standart qiymatsiz")
                .isEqualTo("${DB_PASSWORD}");
    }

    @Test
    void dbUrl_canBeOverriddenFromEnvironment() throws Exception {
        assertThat(mainProperties().getProperty("spring.datasource.url")).startsWith("${DB_URL:");
    }

    @Test
    void noHardcodedSecretsInMainConfig() throws Exception {
        String all = mainProperties().toString().toLowerCase();
        assertThat(all).doesNotContain("parol");
    }

    @Test
    void localEnvFile_isImportedOptionally() throws Exception {
        // .env bo'lsa — undagi DB_PASSWORD ishlatiladi; bo'lmasa (CI) — xato bermaydi
        assertThat(mainProperties().getProperty("spring.config.import"))
                .isEqualTo("optional:file:.env[.properties]");
    }

    @Test
    void mainConfig_isLoadedInTests_andTestProfileOverridesDatabase() {
        // Asosiy fayldagi qiymat testlarda ham ko'rinadi (avval test fayli uni to'sib qo'ygan edi)
        assertThat(environment.getProperty("library.loan.max-active-per-member")).isEqualTo("5");
        // DB esa test profilidan — H2
        assertThat(environment.getProperty("spring.datasource.url")).startsWith("jdbc:h2:mem:");
        assertThat(environment.getActiveProfiles()).contains("test");
    }
}
