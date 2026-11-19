package kutubxona_catalog_service.demo.stage15;

import com.jayway.jsonpath.JsonPath;
import kutubxona_catalog_service.demo.repository.*;
import kutubxona_catalog_service.demo.service.LoanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 15-bosqich (17 va 19-band): ilova REAL PostgreSQL'da, asosiy konfiguratsiya bilan ishlaydi.
 * - sxemani Flyway yaratadi, Hibernate faqat tekshiradi (ddl-auto=validate);
 * - asosiy so'rovlar (null filtrlar, FOR UPDATE, bulk update) Postgres'da ham ishlaydi.
 * "test" profili ishlatilmaydi — bu production konfiguratsiyasining o'zi.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired LoanService loanService;
    @Autowired LoanRepository loanRepository;
    @Autowired BookRepository bookRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired AuthorRepository authorRepository;
    @Autowired CategoryRepository categoryRepository;

    @AfterEach
    void cleanUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Long postAndGetId(String url, String json) throws Exception {
        String body = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void schemaIsManagedByFlyway_andHibernateValidatesIt() {
        assertThat(jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success and version = '1'", Integer.class))
                .isEqualTo(1);
        // Kontekst ko'tarilgani = ddl-auto=validate entity'lar va jadvallar mosligini tasdiqladi
    }

    @Test
    void fullLibraryFlow_onPostgres() throws Exception {
        Long authorId = postAndGetId("/api/authors", "{\"fullName\":\"Abdulla Qodiriy\"}");
        Long categoryId = postAndGetId("/api/categories", "{\"name\":\"Roman\"}");
        Long bookId = postAndGetId("/api/books", """
                {"title":"O'tkan kunlar","totalCopies":2,"price":45000.50,
                 "authorIds":[%d],"categoryIds":[%d]}""".formatted(authorId, categoryId));
        Long memberId = postAndGetId("/api/members", "{\"fullName\":\"Ali\",\"email\":\"ali@example.com\"}");

        // Kitob olish (SELECT ... FOR UPDATE Postgres'da)
        Long loanId = postAndGetId("/api/loans/borrow",
                "{\"bookId\":" + bookId + ",\"memberId\":" + memberId + "}");

        // Filtrlar: null parametrlar Postgres'da muammo tug'dirmasligi kerak
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mockMvc.perform(get("/api/loans").param("memberId", memberId.toString()).param("status", "ACTIVE"))
                .andExpect(jsonPath("$.content[0].bookTitle").value("O'tkan kunlar"));

        // Muddati o'tganlarni belgilash (bulk UPDATE)
        assertThat(loanService.markOverdueLoans(LocalDate.now().plusDays(30))).isEqualTo(1);
        mockMvc.perform(get("/api/loans/{id}", loanId)).andExpect(jsonPath("$.status").value("OVERDUE"));

        // Qaytarish va soft delete
        mockMvc.perform(post("/api/loans/{id}/return", loanId)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/books/{id}", bookId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/books/{id}", bookId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/loans/{id}", loanId))
                .andExpect(jsonPath("$.bookTitle").value("O'tkan kunlar"));   // tarix saqlangan
    }

    @Test
    void uniqueConstraint_returns409_onPostgres() throws Exception {
        postAndGetId("/api/categories", "{\"name\":\"Fantastika\"}");
        mockMvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fantastika\"}"))
                .andExpect(status().isConflict());
    }
}
