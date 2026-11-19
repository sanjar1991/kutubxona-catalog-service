package kutubxona_catalog_service.demo.stage6;

import com.jayway.jsonpath.JsonPath;
import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 6-bosqich (10-band): so'rovlar DTO orqali qabul qilinadi va validatsiya qilinadi.
 * Noto'g'ri ma'lumot -> 400 va qaysi maydon xato ekanligi "errors" ichida.
 */
class ValidationAndDtoTest extends IntegrationTestBase {

    // ---------- Validatsiya ----------

    @Test
    void createBook_blankTitle_returns400WithFieldError() throws Exception {
        postJson("/api/books", "{\"title\":\"   \",\"totalCopies\":1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
        assertThat(bookRepository.count()).isZero();
    }

    @Test
    void createBook_negativeCopies_returns400() throws Exception {
        postJson("/api/books", "{\"title\":\"Ikki eshik orasi\",\"totalCopies\":-3}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.totalCopies").exists());
    }

    @Test
    void updateBook_blankTitle_returns400() throws Exception {
        Book book = saveBook("Asl", 1);
        putJson("/api/books/" + book.getId(), "{\"title\":\"\",\"totalCopies\":1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createAuthor_blankName_returns400() throws Exception {
        postJson("/api/authors", "{\"fullName\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        postJson("/api/categories", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createMember_invalidEmail_returns400() throws Exception {
        postJson("/api/members", "{\"fullName\":\"Ali\",\"email\":\"bu-email-emas\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void borrow_missingIds_returns400() throws Exception {
        postJson("/api/loans/borrow", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bookId").exists())
                .andExpect(jsonPath("$.errors.memberId").exists());
    }

    // ---------- DTO: mass assignment himoyasi ----------

    @Test
    void createBook_withExistingIdInBody_doesNotOverwriteExistingBook() throws Exception {
        Book existing = saveBook("Asl nom", 1);

        String body = postJson("/api/books",
                "{\"id\":" + existing.getId() + ",\"title\":\"Buzilgan\",\"totalCopies\":1}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long newId = ((Number) JsonPath.read(body, "$.id")).longValue();
        assertThat(newId).isNotEqualTo(existing.getId());
        assertThat(bookRepository.findById(existing.getId()).orElseThrow().getTitle())
                .isEqualTo("Asl nom");
    }

    // ---------- DTO: bog'lanishlar id orqali ----------

    @Test
    void createBook_withAuthorAndCategoryIds_returnsTheirNames() throws Exception {
        Author author = saveAuthor("Abdulla Qahhor");
        Category category = saveCategory("Qissa");

        postJson("/api/books", """
                {"title":"Sinchalak","totalCopies":2,
                 "authorIds":[%d],"categoryIds":[%d]}
                """.formatted(author.getId(), category.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authors[0].id").value(author.getId()))
                .andExpect(jsonPath("$.authors[0].name").value("Abdulla Qahhor"))
                .andExpect(jsonPath("$.categories[0].name").value("Qissa"));
    }

    @Test
    void createBook_unknownAuthorId_returns404() throws Exception {
        postJson("/api/books", "{\"title\":\"X\",\"totalCopies\":1,\"authorIds\":[999999]}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Muallif topilmadi: id=999999"));
        assertThat(bookRepository.count()).isZero();
    }
}
