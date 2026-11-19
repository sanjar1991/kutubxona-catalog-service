package kutubxona_catalog_service.demo.stage1;

import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 1-bosqich: JPA Auditing.
 * BaseEntity'dagi createdAt (nullable=false) avtomatik to'ldirilishi kerak,
 * aks holda Author/Category saqlanmaydi.
 */
class AuditingTest extends IntegrationTestBase {

    @Test
    void savingAuthor_fillsCreatedAtAndUpdatedAt() {
        Author author = new Author();
        author.setFullName("Abdulla Qodiriy");

        Author saved = authorRepository.save(author);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatingCategory_changesUpdatedAtButKeepsCreatedAt() throws InterruptedException {
        Category category = new Category();
        category.setName("Roman");
        Category saved = categoryRepository.saveAndFlush(category);
        var createdAt = saved.getCreatedAt();
        var firstUpdatedAt = saved.getUpdatedAt();

        Thread.sleep(10);
        saved.setName("Tarixiy roman");
        Category updated = categoryRepository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfter(firstUpdatedAt);
    }

    @Test
    void postAuthorEndpoint_returns201() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Cho'lpon\",\"biography\":\"Shoir\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }
}
