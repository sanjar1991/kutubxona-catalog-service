package kutubxona_catalog_service.demo.stage8;

import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 8-bosqich (8-band): boshqa yozuvlar bog'langan narsani o'chirishga urinish
 * 500 (FK xatosi) emas, tushunarli 409 qaytarishi kerak. Ma'lumot o'zgarmaydi.
 * (11-bosqichdan keyin: o'chirish soft delete; tekshiruvlar shunga moslangan.)
 */
class DeleteConstraintsTest extends IntegrationTestBase {

    @Test
    void deleteAuthor_linkedToBook_returns409AndKeepsAuthor() throws Exception {
        Author author = saveAuthor("Oybek");
        Book book = saveBook("Navoiy", 1);
        book.getAuthors().add(author);
        bookRepository.save(book);

        mockMvc.perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Muallifni o'chirib bo'lmaydi: u 1 ta kitobga bog'langan"));
        assertThat(authorRepository.findById(author.getId()).orElseThrow().getDeletedAt()).isNull();
    }

    @Test
    void deleteAuthor_notLinked_returns204() throws Exception {
        Author author = saveAuthor("Erkin Vohidov");

        mockMvc.perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/authors/{id}", author.getId())).andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_linkedToBook_returns409() throws Exception {
        Category category = saveCategory("She'riyat");
        Book book = saveBook("Devon", 1);
        book.getCategories().add(category);
        bookRepository.save(book);

        mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Kategoriyani o'chirib bo'lmaydi: u 1 ta kitobga bog'langan"));
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().getDeletedAt()).isNull();
    }

    @Test
    void deleteBook_withActiveLoan_returns409() throws Exception {
        Book book = saveBook("Kitob", 1);
        borrow(book.getId(), saveMember("A").getId());

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Kitobni o'chirib bo'lmaydi: uning nusxasi hozir a'zolar qo'lida"));
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getDeletedAt()).isNull();
    }

    @Test
    void deleteBook_withoutLoans_returns204() throws Exception {
        Author author = saveAuthor("Muallif");
        Book book = saveBook("Kitob", 1);
        book.getAuthors().add(author);
        bookRepository.save(book);

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/books/{id}", book.getId())).andExpect(status().isNotFound());
        assertThat(authorRepository.findById(author.getId()).orElseThrow().getDeletedAt()).isNull(); // muallif o'chmaydi
    }

    @Test
    void duplicateCategoryName_returns409InsteadOf500() throws Exception {
        saveCategory("Roman");

        postJson("/api/categories", "{\"name\":\"Roman\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
